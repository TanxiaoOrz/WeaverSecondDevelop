package csj.fileDecode;

import csj.utils.Console;
import org.apache.commons.io.FileUtils;
import weaver.conn.RecordSet;
import weaver.file.ImageFileManager;
import weaver.general.Util;
import weaver.interfaces.schedule.BaseCronJob;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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
 *   <li>执行上述 SQL（E9 主数据源），得到每一行的 (wdid, wf 业务标识)；</li>
 *   <li>★ wdid 是 <b>文档 id（{@code docimagefile.docid}）</b>，不是附件 id：
 *       先用 {@code docimagefile} 查出该文档下的 {@code imagefileid}（一个文档可能挂多个附件）；</li>
 *   <li>按 imagefileid 取附件流与原始文件名；</li>
 *   <li>用【原始文件名】复制到 {@code /home/losthtfj/{wf}/} 目录下；</li>
 *   <li>同一 {wf} 目录内重名时，按 {@code 原名(1).ext}、{@code 原名(2).ext} 递增，不覆盖。</li>
 * </ol>
 * <p>
 * ★ 附件取流走 E9 原生 {@link ImageFileManager}（已按实际环境确认：附件实体在 E9），
 * 不直读 {@code filerealpath}，以兼容 FTP / OSS / 加密存储。
 * 原始文件名优先取 {@code docimagefile.imagefilename}，取不到回退 {@code imagefile.imagefilename}。
 *
 * @Version 1.1
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
     * <p>
     * ★ {@code wdid} 的语义：<b>{@code docimagefile.docid}</b>（文档 id），不是 imagefileid。
     * 需要通过 {@code docimagefile} 二次转换为 imagefileid 才能取流。
     */
    private static final String QUERY_SQL =
            "SELECT wdid, c.wf "
                    + "  FROM uf_ht c "
                    + "  LEFT JOIN vw_ekp_file_link v ON v.llssid = c.fd_id "
                    + " WHERE c.fd_id IS NOT NULL "
                    + "   AND (c.wf LIKE '%西岑%' OR c.wf LIKE '%信苑%')";

    /** 由 docid 查该文档下所有附件（一个文档可挂多个附件） */
    private static final String DOC_IMAGE_SQL =
            "SELECT imagefileid, imagefilename FROM docimagefile WHERE docid = ?";

    /** docimagefile.imagefilename 为空时的兜底查询 */
    private static final String IMAGE_FILE_SQL =
            "SELECT imagefilename FROM imagefile WHERE imagefileid = ?";

    @Override
    public void execute() {
        executeJob();
    }

    /**
     * 任务主入口，也可在页面/其它类中手工调用。
     */
    public void executeJob() {
        Console.log("====LostHtFileExportJob Start====");
        Console.log("QUERY_SQL=>" + QUERY_SQL);
        Console.log("TARGET_ROOT=>" + TARGET_ROOT);

        // 1. 查出待导出的文档
        List<HtAttachRow> rows = queryAttachments();
        Console.log("查询到待处理文档数=>" + rows.size());
        if (rows.isEmpty()) {
            Console.log("====LostHtFileExportJob Finish（无待处理记录）====");
            return;
        }

        // 2. 逐条导出（一条 = 一个 docid，可能含多个附件）
        int successDoc = 0;
        int failDoc = 0;
        int successFile = 0;
        int failFile = 0;
        for (HtAttachRow row : rows) {
            try {
                ExportResult r = exportOne(row);
                if (r.success) {
                    successDoc++;
                } else {
                    failDoc++;
                }
                successFile += r.successFiles;
                failFile += r.failFiles;
            } catch (Exception e) {
                failDoc++;
                // 单条失败不中断整体任务
                Console.log("导出异常 docid=" + row.wdid + ", wf=" + row.wf
                        + ", error=" + e.getClass().getName() + ": " + e.getMessage());
            }
        }

        Console.log("====LostHtFileExportJob Finish==== 文档成功=" + successDoc
                + ", 文档失败=" + failDoc + ", 文档合计=" + rows.size()
                + " | 附件成功=" + successFile + ", 附件失败=" + failFile);
    }

    // ------------------------------------------------------------------
    // 取数
    // ------------------------------------------------------------------

    /**
     * 执行需求给定的 SQL，返回待处理行。
     * <p>
     * wdid 允许是逗号分隔的多个文档 id，这里展开成多条，便于逐条处理。
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
                    Console.log("跳过：wdid 为空, wf=" + wf);
                    continue;
                }
                if (wf.isEmpty()) {
                    // wf 是目标子目录名，为空则无法确定落点
                    Console.log("跳过：wf 为空（无法确定导出目录）, wdid=" + wdidRaw);
                    continue;
                }

                for (String one : wdidRaw.split(",")) {
                    String docId = one.trim();
                    if (docId.isEmpty()) {
                        continue;
                    }
                    rows.add(new HtAttachRow(docId, wf));
                }
            }
        } catch (Exception e) {
            Console.log("查询待导出文档失败, sql=" + QUERY_SQL
                    + ", error=" + e.getClass().getName() + ": " + e.getMessage());
        }
        return rows;
    }

    /**
     * ★ 关键转换：由文档 id（{@code docimagefile.docid}）查出该文档下的全部附件。
     * <p>
     * 一个 docid 可能对应多条 docimagefile 记录（附件/正文图/版本），全部导出；
     * 同一 imagefileid 只保留一次，避免重复占名产生多余的「名称(1)」副本。
     */
    private List<DocAttach> queryAttachByDocId(String docId) {
        List<DocAttach> list = new ArrayList<>();
        int docidValue = Util.getIntValue(docId);
        if (docidValue <= 0) {
            Console.log("文档id非数字或无效, wdid=" + docId);
            return list;
        }
        RecordSet rs = new RecordSet();
        Set<Integer> seen = new LinkedHashSet<>();
        try {
            rs.executeQuery(DOC_IMAGE_SQL, docidValue);
            while (rs.next()) {
                int imagefileId = Util.getIntValue(rs.getString("imagefileid"));
                if (imagefileId <= 0 || !seen.add(imagefileId)) {
                    continue;
                }
                String fileName = Util.null2String(rs.getString("imagefilename")).trim();
                list.add(new DocAttach(imagefileId, fileName));
            }
        } catch (Exception e) {
            Console.log("查询 docimagefile 失败, docid=" + docId
                    + ", error=" + e.getClass().getName() + ": " + e.getMessage());
        }
        if (list.isEmpty()) {
            Console.log("docimagefile 无此文档记录或文档下无附件, docid=" + docId);
        }
        return list;
    }

    // ------------------------------------------------------------------
    // 单条导出
    // ------------------------------------------------------------------

    private ExportResult exportOne(HtAttachRow row) {
        // 1. 文档 id -> 附件列表（imagefileid）
        List<DocAttach> atts = queryAttachByDocId(row.wdid);
        if (atts.isEmpty()) {
            Console.log("失败：文档下解析不到附件, docid=" + row.wdid + ", wf=" + row.wf);
            return ExportResult.failed(1);
        }

        int ok = 0;
        int fail = 0;
        for (DocAttach att : atts) {
            boolean one = exportAttachment(row, att);
            if (one) {
                ok++;
            } else {
                fail++;
            }
        }
        Console.log("文档处理完成 docid=" + row.wdid + ", 附件成功=" + ok + ", 附件失败=" + fail);
        return ExportResult.of(ok, fail);
    }

    /** 导出单个附件：占名 + 取流写出；返回该附件是否成功 */
    private boolean exportAttachment(HtAttachRow row, DocAttach att) {
        // 1. 取流（E9 原生静态方法，自动适配 FTP / OSS / 加密存储）
        InputStream in;
        try {
            in = ImageFileManager.getInputStreamById(att.imagefileId);
        } catch (Exception e) {
            Console.log("取附件流出错, imagefileid=" + att.imagefileId + ", docid=" + row.wdid
                    + ", error=" + e.getClass().getName() + ": " + e.getMessage());
            return false;
        }
        if (in == null) {
            Console.log("附件流为空, imagefileid=" + att.imagefileId + ", docid=" + row.wdid);
            return false;
        }

        File targetFile = null;
        try {
            // 2. 准备目标目录 /home/losthtfj/{wf}
            File targetDir = new File(TARGET_ROOT, sanitizeDirName(row.wf));
            if (!targetDir.exists() && !targetDir.mkdirs()) {
                Console.log("失败：创建目标目录不成功, dir=" + targetDir.getAbsolutePath());
                return false;
            }

            // 3. 用原始文件名占位（原子占名，重名自动加序号）
            String originalName = resolveOriginalName(att);
            targetFile = reserveTargetFile(targetDir, originalName);
            if (targetFile == null) {
                Console.log("失败：同目录重名尝试次数超限, dir=" + targetDir.getAbsolutePath()
                        + ", name=" + originalName);
                return false;
            }

            // 4. 流式写出（不整体读入内存，大附件也不会撑爆堆）
            FileUtils.copyInputStreamToFile(in, targetFile);
            Console.log("成功：docid=" + row.wdid + ", imagefileid=" + att.imagefileId
                    + ", name=" + originalName + " -> " + targetFile.getAbsolutePath());
            return true;
        } catch (Exception e) {
            // 中途失败会留下 0 字节占位文件，删掉，避免下次运行误判为"已导出"
            if (targetFile != null && targetFile.exists() && targetFile.length() == 0) {
                if (targetFile.delete()) {
                    Console.log("已清理空占位文件, file=" + targetFile.getAbsolutePath());
                }
            }
            Console.log("失败：写出文件异常, docid=" + row.wdid + ", imagefileid=" + att.imagefileId
                    + ", wf=" + row.wf + ", error=" + e.getClass().getName() + ": " + e.getMessage());
            return false;
        } finally {
            closeQuietly(in);
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
            Console.log("关闭附件流失败, error=" + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // 文件名处理
    // ------------------------------------------------------------------

    /**
     * 取「原始文件名」。
     * <p>
     * 优先取 {@code docimagefile.imagefilename}；为空时回退 {@code imagefile.imagefilename}；
     * 仍取不到则用 {@code 附件_{imagefileid}}，保证不产生空名/怪名。
     * <p>
     * 注意：**不能**用物理落盘文件名 —— 实际存储文件名可能是哈希串。
     */
    private String resolveOriginalName(DocAttach att) {
        String name = Util.null2String(att.fileName).trim();
        if (name.isEmpty() || !name.contains(".")) {
            String dbName = queryImageFileName(att.imagefileId);
            if (!dbName.isEmpty() && dbName.contains(".")) {
                name = dbName;
            }
        }
        if (name.isEmpty() || !name.contains(".")) {
            name = "附件_" + att.imagefileId;
        }
        return sanitizeFileName(name);
    }

    /** 回退取 imagefile.imagefilename */
    private String queryImageFileName(int imagefileId) {
        RecordSet rs = new RecordSet();
        try {
            rs.executeQuery(IMAGE_FILE_SQL, imagefileId);
            if (rs.next()) {
                return Util.null2String(rs.getString("imagefilename")).trim();
            }
        } catch (Exception e) {
            Console.log("查询 imagefile 失败, imagefileid=" + imagefileId
                    + ", error=" + e.getClass().getName() + ": " + e.getMessage());
        }
        return "";
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

    /** 单条待导出记录：一个文档 + 它所属的公司目录 */
    private static class HtAttachRow {
        /** 文档 id（SQL 里的 wdid = docimagefile.docid） */
        final String wdid;
        /** 业务标识，同时作为导出子目录名（SQL 里的 wf） */
        final String wf;

        HtAttachRow(String wdid, String wf) {
            this.wdid = wdid;
            this.wf = wf;
        }
    }

    /** docimagefile 里的一行：附件 id + 文件名 */
    private static class DocAttach {
        final int imagefileId;
        final String fileName;

        DocAttach(int imagefileId, String fileName) {
            this.imagefileId = imagefileId;
            this.fileName = fileName;
        }
    }

    /** 单条处理结果 + 附件级计数 */
    private static class ExportResult {
        /** 该文档是否全部附件都成功 */
        final boolean success;
        /** 该文档下成功导出的附件数 */
        final int successFiles;
        /** 该文档下失败的附件数 */
        final int failFiles;

        private ExportResult(boolean success, int successFiles, int failFiles) {
            this.success = success;
            this.successFiles = successFiles;
            this.failFiles = failFiles;
        }

        static ExportResult of(int successFiles, int failFiles) {
            return new ExportResult(failFiles == 0, successFiles, failFiles);
        }

        /** 附件级失败（文档无附件、流取不到等），单独构造用 */
        static ExportResult failed(int failFiles) {
            return new ExportResult(false, 0, failFiles);
        }
    }
}
