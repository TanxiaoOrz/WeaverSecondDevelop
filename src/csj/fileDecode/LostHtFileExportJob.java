package csj.fileDecode;

import org.apache.commons.io.FileUtils;
import weaver.conn.RecordSet;
import weaver.file.ImageFileManager;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.interfaces.schedule.BaseCronJob;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @PackageName: csj.fileDecode
 * @ClassName: LostHtFileExportJob
 * @Description: 合同附件按原始名称导出定时任务。
 * <p>
 * 取数 SQL（需求给定；此处仅把 wf 限定为 c.wf，见 {@link #QUERY_SQL} 注释）：
 * <pre>
 * SELECT wdid, c.wf
 *   FROM uf_ht c
 *   LEFT JOIN vw_ekp_file_link v ON v.llssid = c.fd_id
 *  WHERE c.fd_id IS NOT NULL
 *    AND (c.wf LIKE '%西岑%' OR c.wf LIKE '%信苑%')
 * </pre>
 * <p>
 * 处理逻辑：
 * <ol>
 *   <li>执行上述 SQL（E9 主数据源），得到每一行的 (wdid 附件id, wf 业务标识)；</li>
 *   <li>按 wdid（= E9 的 {@code imagefileid}）取附件流与原始文件名；</li>
 *   <li>用【原始文件名】复制到 {@code /home/losthtfj/{wf}/} 目录下；</li>
 *   <li>同一 {wf} 目录内重名时，按 {@code 原名(1).ext}、{@code 原名(2).ext} 递增，不覆盖。</li>
 * </ol>
 * <p>
 * ★ 附件取流走 E9 原生 {@link ImageFileManager}（已按实际环境确认：附件实体在 E9，
 * wdid 即 imagefileid），不直读 {@code filerealpath}，以兼容 FTP / OSS / 加密存储。
 * 原始文件名取自 {@code imagefile.imagefilename}。
 *
 * @Version 1.0
 */
public class LostHtFileExportJob extends BaseCronJob {

    /** 导出目标根目录，其下按 wf 分文件夹 */
    private static final String TARGET_ROOT = "/home/losthtfj";

    /** 同目录内重名时最多尝试的序号，防止死循环 */
    private static final int MAX_NAME_TRY = 1000;

    /**
     * 取数 SQL。
     * <p>
     * 与需求给定语句一致，仅把 {@code wf} 限定为 {@code c.wf}（已确认 wf 来自 uf_ht 合同表）。
     * 加表别名的原因：视图 {@code vw_ekp_file_link} 里若也有同名/同义列，裸写 wf 会产生
     * 「列名不明确(ambiguous)」错误或取错列，限定归属可避免。
     */
    private static final String QUERY_SQL =
            "SELECT wdid, c.wf "
                    + "  FROM uf_ht c "
                    + "  LEFT JOIN vw_ekp_file_link v ON v.llssid = c.fd_id "
                    + " WHERE c.fd_id IS NOT NULL "
                    + "   AND (c.wf LIKE '%西岑%' OR c.wf LIKE '%信苑%')";

    private final BaseBean bs = new BaseBean();

    @Override
    public void execute() {
        executeJob();
    }

    /**
     * 任务主入口，也可在页面/其它类中手工调用。
     */
    public void executeJob() {
        bs.writeLog("====LostHtFileExportJob Start====");
        bs.writeLog("QUERY_SQL=>" + QUERY_SQL);
        bs.writeLog("TARGET_ROOT=>" + TARGET_ROOT);

        // 1. 查出待导出的附件
        List<HtAttachRow> rows = queryAttachments();
        bs.writeLog("查询到待处理记录数=>" + rows.size());
        if (rows.isEmpty()) {
            bs.writeLog("====LostHtFileExportJob Finish（无待处理记录）====");
            return;
        }

        // 2. 逐条导出
        int successCount = 0;
        int failCount = 0;
        for (HtAttachRow row : rows) {
            try {
                if (exportOne(row) == ExportResult.SUCCESS) {
                    successCount++;
                } else {
                    failCount++;
                }
            } catch (Exception e) {
                failCount++;
                // 单条失败不中断整体任务
                bs.writeLog("导出异常 wdid=" + row.wdid + ", wf=" + row.wf
                        + ", error=" + e.getClass().getName() + ": " + e.getMessage(), e);
            }
        }

        bs.writeLog("====LostHtFileExportJob Finish==== 成功=" + successCount
                + ", 失败=" + failCount + ", 合计=" + rows.size());
    }

    // ------------------------------------------------------------------
    // 取数
    // ------------------------------------------------------------------

    /**
     * 执行需求给定的 SQL，返回待处理行。
     * <p>
     * wdid 允许是逗号分隔的多个附件 id，这里展开成多条，便于逐条处理。
     */
    private List<HtAttachRow> queryAttachments() {
        List<HtAttachRow> rows = new ArrayList<>();
        RecordSet rs = new RecordSet();
        try {
            rs.executeQuery(QUERY_SQL);
            while (rs.next()) {
                String wf = Util.null2String(rs.getString("wf")).trim();
                String wdidRaw = Util.null2String(rs.getString("wdid")).trim();

                if (wdidRaw.isEmpty()) {
                    // LEFT JOIN 未命中时 wdid 为 NULL，无附件可导
                    bs.writeLog("跳过：wdid 为空, wf=" + wf);
                    continue;
                }
                if (wf.isEmpty()) {
                    // wf 是目标子目录名，为空则无法确定落点
                    bs.writeLog("跳过：wf 为空（无法确定导出目录）, wdid=" + wdidRaw);
                    continue;
                }

                for (String one : wdidRaw.split(",")) {
                    String attachId = one.trim();
                    if (attachId.isEmpty()) {
                        continue;
                    }
                    rows.add(new HtAttachRow(attachId, wf));
                }
            }
        } catch (Exception e) {
            bs.writeLog("查询待导出附件失败, sql=" + QUERY_SQL, e);
        }
        return rows;
    }

    // ------------------------------------------------------------------
    // 单条导出
    // ------------------------------------------------------------------

    private ExportResult exportOne(HtAttachRow row) {
        // 1. 解析出附件流与原始文件名
        ResolvedFile resolved = resolveByImagefileId(row.wdid);
        if (resolved == null) {
            bs.writeLog("失败：解析不到附件, wdid=" + row.wdid + ", wf=" + row.wf);
            return ExportResult.FAILED;
        }

        File targetFile = null;
        try {
            // 2. 准备目标目录 /home/losthtfj/{wf}
            File targetDir = new File(TARGET_ROOT, sanitizeDirName(row.wf));
            if (!targetDir.exists() && !targetDir.mkdirs()) {
                bs.writeLog("失败：创建目标目录不成功, dir=" + targetDir.getAbsolutePath());
                return ExportResult.FAILED;
            }

            // 3. 用原始文件名占位（原子占名，重名自动加序号）
            String originalName = resolveOriginalName(resolved, row.wdid);
            targetFile = reserveTargetFile(targetDir, originalName);
            if (targetFile == null) {
                bs.writeLog("失败：同目录重名尝试次数超限, dir=" + targetDir.getAbsolutePath()
                        + ", name=" + originalName);
                return ExportResult.FAILED;
            }

            // 4. 流式写出（不整体读入内存，大附件也不会撑爆堆）
            FileUtils.copyInputStreamToFile(resolved.inputStream, targetFile);
            bs.writeLog("成功：imagefileid=" + row.wdid + ", name=" + originalName
                    + " -> " + targetFile.getAbsolutePath());
            return ExportResult.SUCCESS;
        } catch (Exception e) {
            // 中途失败会留下 0 字节占位文件，删掉，避免下次运行误判为"已导出"
            if (targetFile != null && targetFile.exists() && targetFile.length() == 0) {
                if (targetFile.delete()) {
                    bs.writeLog("已清理空占位文件, file=" + targetFile.getAbsolutePath());
                }
            }
            bs.writeLog("失败：写出文件异常, wdid=" + row.wdid + ", wf=" + row.wf, e);
            return ExportResult.FAILED;
        } finally {
            closeQuietly(resolved.inputStream);
        }
    }

    /** 安静关闭流，失败只记日志不抛 */
    private void closeQuietly(InputStream in) {
        if (in == null) {
            return;
        }
        try {
            in.close();
        } catch (Exception e) {
            bs.writeLog("关闭附件流失败", e);
        }
    }

    // ------------------------------------------------------------------
    // ★ 附件取流：本类的核心分叉点
    // ------------------------------------------------------------------

    /**
     * 按附件 id（E9 的 {@code imagefileid}）解析出附件信息：输入流 + 原始文件名。
     * <p>
     * ★ 走 E9 原生附件抽象 {@link ImageFileManager}，**不要直读** {@code imagefile.filerealpath}：
     * 只有走 ImageFileManager 才能自动兼容客户端的「本地磁盘 / FTP / 阿里云 OSS / AES 加密附件」
     * 等不同存储形态（直读物理路径在开了 FTP/OSS/加密的环境下会取不到文件）。
     * <p>
     * 原始文件名取自 E9 的 {@code imagefile.imagefilename} 列（含后缀名）；
     * 不取物理文件名，因为实际落盘文件名可能是哈希串。
     *
     * @return 解析结果；附件不存在或取流失败返回 null
     */
    private ResolvedFile resolveByImagefileId(String attachId) {
        int imagefileId = Util.getIntValue(attachId);
        if (imagefileId <= 0) {
            bs.writeLog("附件id非数字或无效, wdid=" + attachId);
            return null;
        }

        // 1. 取原始文件名（imagefile.imagefilename 即用户上传时的文件名，含后缀）
        String dbFileName = "";
        RecordSet rs = new RecordSet();
        try {
            rs.executeQuery("select imagefilename from imagefile where imagefileid = ?", imagefileId);
            if (rs.next()) {
                dbFileName = Util.null2String(rs.getString("imagefilename")).trim();
            } else {
                bs.writeLog("imagefile 表无此记录, imagefileid=" + imagefileId);
                return null;
            }
        } catch (Exception e) {
            bs.writeLog("查询 imagefile 失败, imagefileid=" + imagefileId, e);
            return null;
        }

        // 2. 取流（E9 原生静态方法，自动适配 FTP / OSS / 加密存储）
        try {
            InputStream in = ImageFileManager.getInputStreamById(imagefileId);
            if (in == null) {
                bs.writeLog("附件流为空, imagefileid=" + imagefileId);
                return null;
            }
            return new ResolvedFile(in, dbFileName);
        } catch (Exception e) {
            bs.writeLog("取附件流出错, imagefileid=" + imagefileId, e);
            return null;
        }
    }

    // ------------------------------------------------------------------
    // 文件名处理
    // ------------------------------------------------------------------

    /**
     * 取「原始文件名」。
     * <p>
     * 取自 E9 的 {@code imagefile.imagefilename}（用户上传时的文件名，含后缀）。
     * 取不到时退回 {@code 附件_{wdid}}，保证不产生空名/怪名。
     * <p>
     * 注意：**不能**用物理落盘文件名 —— 实际存储文件名可能是哈希串。
     */
    private String resolveOriginalName(ResolvedFile resolved, String attachId) {
        String name = Util.null2String(resolved.dbFileName).trim();
        if (name.isEmpty() || !name.contains(".")) {
            name = "附件_" + attachId;
        }
        return sanitizeFileName(name);
    }

    /**
     * 在目标目录里原子地占一个文件名，重名时按 {@code 原名(1).ext} 递增。
     * <p>
     * 用 {@code createNewFile()} 的返回值占名，而不是先 {@code exists()} 再写，
     * 避免并发下两个线程选中同一个名字。
     *
     * @return 已占好的空文件；尝试次数用尽返回 null
     */
    private File reserveTargetFile(File dir, String originalName) throws IOException {
        String baseName = originalName;
        String ext = "";
        int dot = originalName.lastIndexOf('.');
        if (dot > 0) {
            baseName = originalName.substring(0, dot);
            ext = originalName.substring(dot);
        }

        for (int i = 0; i < MAX_NAME_TRY; i++) {
            String candidate = (i == 0) ? originalName : (baseName + "(" + i + ")" + ext);
            File f = new File(dir, candidate);
            if (f.createNewFile()) {
                return f;
            }
        }
        return null;
    }

    /**
     * 清理目录名：wf 会直接作为 /home/losthtfj 下的子目录名，
     * 必须去掉路径分隔符与非法字符，防止目录穿越。
     */
    private String sanitizeDirName(String raw) {
        String s = sanitize(raw, "未命名");
        // 去掉首尾的点与空白（避免隐藏目录、避免以空格结尾的目录）
        s = s.replaceAll("^[.\\s]+", "").replaceAll("[.\\s]+$", "");
        // 若只剩分隔符残留（如输入全是 "../.."），回退默认名
        if (s.replace("_", "").trim().isEmpty()) {
            return "未命名";
        }
        return s;
    }

    /** 清理文件名：去掉路径分隔符等非法字符，保留中文与原扩展名 */
    private String sanitizeFileName(String raw) {
        return sanitize(raw, "未命名附件");
    }

    /**
     * 通用清洗：把路径分隔符与非法字符换成下划线，阻断 ../ 穿越，
     * 去掉开头的点与空白；清空后返回 {@code fallback}。
     */
    private String sanitize(String raw, String fallback) {
        String s = Util.null2String(raw).trim();
        // 路径分隔符、Windows 非法字符、换行制表一律换下划线
        s = s.replaceAll("[\\\\/:*?\"<>|\\r\\n\\t]", "_");
        // 阻断 ../ 目录穿越（循环替换，防 "..././" 这类叠加）
        while (s.contains("..")) {
            s = s.replace("..", "_");
        }
        // 去掉开头的点与空白（隐藏文件、以空格开头的怪名字）
        s = s.replaceAll("^[.\\s]+", "");
        return s.isEmpty() ? fallback : s;
    }

    // ------------------------------------------------------------------
    // 内部类型
    // ------------------------------------------------------------------

    /** 单条待导出记录 */
    private static class HtAttachRow {
        /** 附件 id（SQL 里的 wdid） */
        final String wdid;
        /** 业务标识，同时作为导出子目录名（SQL 里的 wf） */
        final String wf;

        HtAttachRow(String wdid, String wf) {
            this.wdid = wdid;
            this.wf = wf;
        }
    }

    /** 单条处理结果 */
    private enum ExportResult {
        SUCCESS, FAILED
    }

    /** 附件解析结果：附件输入流 + 库里的原始文件名 */
    private static class ResolvedFile {
        final InputStream inputStream;
        final String dbFileName;

        ResolvedFile(InputStream inputStream, String dbFileName) {
            this.inputStream = inputStream;
            this.dbFileName = dbFileName;
        }
    }
}
