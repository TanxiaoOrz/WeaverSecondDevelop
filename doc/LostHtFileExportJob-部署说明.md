# LostHtFileExportJob 部署说明

## 1. 交付物

| 文件 | 说明 |
|---|---|
| `src/csj/fileDecode/LostHtFileExportJob.java` | 源码（384 行，1 个类 + 3 个私有内部类型） |
| `LostHtFileExportJob.class` | 主类 |
| `LostHtFileExportJob$ExportResult.class` | 内部枚举 |
| `LostHtFileExportJob$HtAttachRow.class` | 内部类：一行待处理记录 |
| `LostHtFileExportJob$ResolvedFile.class` | 内部类：附件流 + 原始文件名 |

**类文件数 = 1 个 java**（编译产出 4 个 class，含 3 个内部类），符合通用后端形态上限。

## 2. 编译验证结果

- 用技能自带 `templates/e9-min-stubs` 的 90 个桩类 + `commons-io` 离线编译：**通过**
- class 文件 major version = **52（JDK8）**，符合 E9 要求
- 纯逻辑单元测试（目录名清洗 / 文件名清洗 / 重名去重 / 原始名回退）：**22 项全通过**

> E9 运行时相关部分（`RecordSet` 查库、`ImageFileManager` 取流）**未实测**，本机无 E9 环境。

## 3. 部署步骤

### 3.1 放 class

```
<ecology>/classbean/csj/fileDecode/LostHtFileExportJob.class
<ecology>/classbean/csj/fileDecode/LostHtFileExportJob$ExportResult.class
<ecology>/classbean/csj/fileDecode/LostHtFileExportJob$HtAttachRow.class
<ecology>/classbean/csj/fileDecode/LostHtFileExportJob$ResolvedFile.class
```

> 内部类（`$` 号文件）必须与主类同目录一起放，漏了会 `NoClassDefFoundError`。

### 3.2 注册定时任务

E9 后台 → 集成中心 → 计划任务 → 新建：

| 项 | 值 |
|---|---|
| 任务名称 | 合同附件按原名导出 |
| 执行类 | `csj.fileDecode.LostHtFileExportJob` |
| 执行频率 | 按需（一次性导出任务，跑完可停用） |

> 类继承 `weaver.interfaces.schedule.BaseCronJob`，`execute()` 已实现，
> 同时提供 `executeJob()` 供手工调用。

### 3.3 目录与权限

```bash
mkdir -p /home/losthtfj
chown -R <运行ecology的用户>:<同组> /home/losthtfj
```

E9 运行账号必须对 `/home/losthtfj` 有**写权限**。无权限时任务日志会出现
`创建目标目录不成功`。

### 3.4 日志

统一走 `weaver.general.BaseBean#writeLog`，输出到 E9 标准日志：
```
<ecology>/log/ecology_<日期>.log
```

关键字检索：`LostHtFileExportJob`

## 4. 执行逻辑

```
1. 执行 SQL（E9 主数据源）→ 得到 (wdid=imagefileid, wf=合同名称)
2. 逐条：
   a. 查 imagefile.imagefilename 取原始文件名（含后缀）
   b. ImageFileManager.getInputStreamById(imagefileid) 取流
   c. wf 清洗后作为子目录名 → /home/losthtfj/{wf}/
   d. 原子占名（重名自动 名称(1).ext、名称(2).ext）→ 流式写出
3. 单条失败不中断，最后汇总 成功/失败 计数
```

### SQL（实际使用）

```sql
SELECT wdid, c.wf
  FROM uf_ht c
  LEFT JOIN vw_ekp_file_link v ON v.llssid = c.fd_id
 WHERE c.fd_id IS NOT NULL
   AND (c.wf LIKE '%西岑%' OR c.wf LIKE '%信苑%')
```

> 与原需求语句唯一差异：`wf` 加了 `c.` 限定。已确认 wf 来自 `uf_ht`，
> 加限定可避免视图里同名列导致的「列名不明确」错误。

## 5. 关键实现取舍（为什么要这么写）

| 决策 | 原因 |
|---|---|
| 用 `ImageFileManager.getInputStreamById()` 取流 | 兼容本地 / FTP / OSS / AES 加密等存储形态。**不要**直读 `imagefile.filerealpath`：开了 FTP/OSS/加密的环境下物理路径取不到文件 |
| 原始文件名取自 `imagefile.imagefilename` | 实际落盘文件名可能是哈希串，不能当文件名用 |
| `createNewFile()` 返回值占名，而非 `exists()` 判断 | 原子操作，并发下不会两个线程抢同一个名字；也不会像 024 补丁包那样**静默覆盖丢文件** |
| `copyInputStreamToFile` 流式写出 | 大附件不整体读入内存，不撑爆堆 |
| 占名后写出失败则删除 0 字节占位文件 | 避免下次运行误判为「已导出」 |
| `wf` 与文件名都做清洗 | 阻断 `../` 目录穿越；去掉 `/ \ : * ? " < > \|` 等非法字符 |
| 单条失败不中断 | 一次几千条时，个别坏附件不应阻断整体导出 |
| 日志用 `BaseBean#writeLog` | 符合 E9 开发规范，禁 slf4j / `printStackTrace()` |

## 6. 已知限制

1. **`vw_ekp_file_link` 的视图定义未提供**，`wdid` 是否恒为单值未确认。
   代码已按「可能逗号分隔多个」做兼容展开，单值场景行为一致。
2. **`uf_ht.c.fd_id` 与 `uf_ht.id` 的关系未确认** —— SQL 按需求原样使用，未做改动。
3. **无幂等标记**：同一批数据重复执行会产生 `名称(1).ext`、`名称(2).ext` 副本，
   不会覆盖但会累积。若需要「只导一次」，建议加一张记录表或用目标目录存在性判断。
4. 未做「附件数量 / 单文件大小」上限控制，极端大数据量场景需自行评估磁盘空间。
5. **未在 E9 实测**：取流与查库行为需在测试环境跑一次确认。
