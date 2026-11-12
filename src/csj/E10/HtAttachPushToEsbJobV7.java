package csj.E10;

import csj.utils.Console;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import weaver.conn.RecordSet;
import weaver.file.ImageFileManager;
import weaver.general.Util;
import weaver.interfaces.schedule.BaseCronJob;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @PackageName: csj.E10
 * @ClassName: HtAttachPushToEsbJobV7
 * @Description: 合同（uf_ht）附件推送 ESB 定时任务。
 * <p>
 * 对接流程（2026-11-12 新版接口文档 + 本地实测通过）：
 * <pre>
 * 1. 上传文件：POST /papi/openapi/api/file/v2/common/upload?access_token=xxx
 *    form-data: module=document, name=文件名, file=二进制, userid=固定值
 *    返回：{"message":{"errcode":"0"},"data":{"fileid":"..."}}
 * 2. 更新合同：POST /papi/openapi/api/open-esb/server/webhook/trigger/xxx?access_token=xxx
 *    body: {"e9id":"<uf_ht.id>","fileids":["<fileid>", ...]}
 * </pre>
 * <p>
 * ★ 为什么不再直接传 base64：
 * <ul>
 *   <li>旧报文 {@code file:[{base64,fileName}]} 一个合同 11 个附件达 33MB，
 *       被 ESB 拒绝（{@code webhook请求数据大小超过指定阈值:2048kb}）；</li>
 *   <li>新版接口改为先上传拿 fileid 再更新合同，webhook 报文只有几十字节，
 *       彻底绕开体积限制。</li>
 * </ul>
 * <p>
 * 数据来源与分支（按需求给定）：
 * <ol>
 *   <li>只处理 {@code uf_ht} 中 {@code needPushZ = '1'} 的记录；</li>
 *   <li><b>fd_id 有值</b>：fd_id 通过 {@code vw_ekp_file_link.llssid} 关联出文档 id（wdid），
 *       wdid 是 {@code docimagefile.docid}，再查 {@code imagefileid} 取附件；</li>
 *   <li><b>fd_id 无值但 xglc 有值</b>：从 {@code uf_ht.fj} 与 {@code uf_ht.yyhwj}
 *       取附件 docid（多选、逗号分隔），合并去重；</li>
 *   <li>两者都无值：跳过。</li>
 * </ol>
 * <p>
 * 两个分支取到 docid 后走同一套「附件 → 上传 → 更新合同」逻辑
 * （{@link #pushByDocIds}），e9id 固定取 {@code uf_ht.id}。
 * <p>
 * 不依赖任何自建表：只读 {@code uf_ht} / {@code docimagefile} / {@code imagefile} /
 * {@code vw_ekp_file_link}，不写日志表。
 *
 * @Version 7.0
 */
public class HtAttachPushToEsbJobV7 extends BaseCronJob {

    // ==================================================================
    // 可配置项
    // ==================================================================

    /** 合同更新 WebHook 触发路径（新版接口，报文为 e9id + fileids） */
    private static final String WEBHOOK_PATH =
            "/papi/openapi/api/open-esb/server/webhook/trigger/NTU3MTcxNWQ3NTYzNDQzNDhhZGEwYzlkYmUwN2RjZDY=";

    /** 文件上传接口路径（相对 E10_URL） */
    private static final String UPLOAD_PATH = "/papi/openapi/api/file/v2/common/upload";

    /** 上传接口的 module 参数，文档模块固定 document */
    private static final String UPLOAD_MODULE = "document";

    /** 上传接口的 userid 参数（需求确认的固定值，对方系统中显示为 sysadmin） */
    private static final String UPLOAD_USERID = "1278914795512094724";

    /**
     * 上传时是否生成文档。
     * <p>
     * 需求确认要生成文档，因此固定传 {@code true}。
     * 文档说明：{@code createDocument=true} 且 {@code module=document} 时
     * <b>folderId 必传</b>，否则文件落不到指定目录。
     */
    private static final String UPLOAD_CREATE_DOCUMENT = "true";

    /**
     * 生成文档的目标文件夹 id（需求提供）。
     * <p>
     * 实测该 id 对应「合同文件 / 合同附件」目录；上传成功后响应里会回带
     * {@code otherParams.folder.id/name} 便于核对是否落在预期目录。
     */
    private static final String UPLOAD_FOLDER_ID = "8138946747098921222";

    /** 合同建模表 */
    private static final String MAIN_TABLE = "uf_ht";

    /** 合同主键列（同时作为报文 e9id） */
    private static final String COL_ID = "id";

    /** 外部文件标识列（有值时走 LostHt 文件获取逻辑） */
    private static final String COL_FD_ID = "fd_id";

    /** 流程 id 列（实际列名 xglc，无下划线；fd_id 无值时走该分支） */
    private static final String COL_XGLC = "xglc";

    /** xglc 分支附件字段一（多选，逗号分隔 docid） */
    private static final String COL_FJ = "fj";

    /** xglc 分支附件字段二（多选，逗号分隔 docid） */
    private static final String COL_YYHWJ = "yyhwj";

    /** 是否需要推送的标记列 */
    private static final String COL_NEED_PUSH = "needPushZ";

    /** 合同名称列（仅日志用） */
    private static final String COL_WF = "wf";

    /** 需要推送的标记值 */
    private static final String PUSH_FLAG_VALUE = "1";

    /** 关闭过滤用的标记值（传这个值即不过滤，便于全量跑批） */
    public static final String PUSH_FLAG_VALUE_OFF = "0";

    /**
     * E10 配置前缀，对应 {@code WEB-INF/prop/E10Config.properties} 里
     * {@code code + 属性名} 的写法。
     * <p>
     * ★ 必须用合同平台的 code：{@code Contract_}。
     * 空串会退到 {@code E10Config} 默认值（长三角 e10），打到另一套系统。
     */
    private static final String E10_CODE = "Contract_";

    /** 单次任务最多推送的附件数（0 = 不限制） */
    private static final int MAX_PUSH_PER_RUN = 0;

    /** 连接超时（秒） */
    private static final int CONNECT_TIMEOUT_SEC = 10;

    /** 读写超时（秒）：上传大附件需要时间，留宽一些 */
    private static final int READ_TIMEOUT_SEC = 120;

    // ==================================================================
    // SQL
    // ==================================================================

    /** 不过滤版本（调试/全量跑批用） */
    private static final String MAIN_SQL_ALL = "SELECT * FROM " + MAIN_TABLE;

    /**
     * 主数据来源：{@code uf_ht} 中标记为需要推送的记录。
     * <p>
     * 标记值用字符串字面量 {@code '1'}：该列无论是 varchar 还是 integer，
     * PostgreSQL 都能正确比较。
     */
    private static final String MAIN_SQL = buildMainSql(PUSH_FLAG_VALUE);

    /** 按标记值拼主查询 */
    private static String buildMainSql(String flag) {
        if (PUSH_FLAG_VALUE_OFF.equals(flag)) {
            return MAIN_SQL_ALL;
        }
        return "SELECT * FROM " + MAIN_TABLE + " WHERE " + COL_NEED_PUSH + " = '" + flag + "'";
    }

    /** fd_id → 文档 id（wdid） */
    private static final String FILE_LINK_SQL =
            "SELECT v.wdid FROM " + MAIN_TABLE + " c "
                    + "  JOIN vw_ekp_file_link v ON v.llssid = c." + COL_FD_ID + " "
                    + " WHERE c." + COL_FD_ID + " = ?";

    /** fd_id 关联的 CAST 兜底（PostgreSQL 类型不一致时用） */
    private static final String FILE_LINK_CAST_SQL =
            "SELECT v.wdid FROM " + MAIN_TABLE + " c "
                    + "  JOIN vw_ekp_file_link v ON v.llssid::text = c." + COL_FD_ID + "::text "
                    + " WHERE c." + COL_FD_ID + "::text = ?";

    /** 文档 id → 该文档下的附件 */
    private static final String DOC_IMAGE_SQL =
            "SELECT imagefileid, imagefilename FROM docimagefile WHERE docid = ?";

    /** docimagefile.imagefilename 为空时的兜底 */
    private static final String IMAGE_FILE_SQL =
            "SELECT imagefilename FROM imagefile WHERE imagefileid = ?";

    // ==================================================================
    // 入口
    // ==================================================================

    @Override
    public void execute() {
        executeJob();
    }

    /** 任务主入口，也可被页面/其它类手工调用 */
    public void executeJob() {
        Console.log("====HtAttachPushToEsbJobV7 Start====");
        Console.log("MAIN_SQL=>" + MAIN_SQL);
        Console.log("UPLOAD_PATH=>" + UPLOAD_PATH + ", WEBHOOK_PATH=>" + WEBHOOK_PATH);
        Console.log("E10_CODE=>" + E10_CODE + ", UPLOAD_USERID=>" + UPLOAD_USERID);
        Console.log("createDocument=>" + UPLOAD_CREATE_DOCUMENT
                + ", folderId=>" + UPLOAD_FOLDER_ID);

        List<HtRow> rows = queryHtRows();
        Console.log("uf_ht 读取记录数=>" + rows.size());
        if (rows.isEmpty()) {
            Console.log("====HtAttachPushToEsbJobV7 Finish（uf_ht 无 " + COL_NEED_PUSH
                    + "=" + PUSH_FLAG_VALUE + " 的数据）====");
            return;
        }

        int pushedHt = 0;
        int failHt = 0;
        int skipNoFile = 0;
        int xglcBranch = 0;
        int pushedFile = 0;
        int failFile = 0;

        for (HtRow row : rows) {
            // 分支1：fd_id 有值 → 走 LostHt 文件获取逻辑
            if (!row.fdId.isEmpty()) {
                try {
                    HtResult r = pushByFdId(row);
                    if (r.pushed > 0) {
                        pushedHt++;
                    } else if (r.failed > 0) {
                        failHt++;
                    } else {
                        skipNoFile++;
                    }
                    pushedFile += r.pushed;
                    failFile += r.failed;
                } catch (Exception e) {
                    failHt++;
                    Console.log("推送异常 id=" + row.id + ", fd_id=" + row.fdId
                            + ", error=" + e.getClass().getName() + ": " + e.getMessage());
                } catch (Throwable t) {
                    failHt++;
                    Console.log("推送致命错误 id=" + row.id + ", fd_id=" + row.fdId
                            + ", error=" + t.getClass().getName() + ": " + t.getMessage());
                }
                continue;
            }

            // 分支2：fd_id 无值但 xglc 有值 → 从 fj / yyhwj 取附件 docid
            if (!row.xglc.isEmpty()) {
                xglcBranch++;
                try {
                    HtResult r = pushByXglc(row);
                    if (r.pushed > 0) {
                        pushedHt++;
                    } else if (r.failed > 0) {
                        failHt++;
                    } else {
                        skipNoFile++;
                    }
                    pushedFile += r.pushed;
                    failFile += r.failed;
                } catch (Exception e) {
                    failHt++;
                    Console.log("推送异常 id=" + row.id + ", xglc=" + row.xglc
                            + ", error=" + e.getClass().getName() + ": " + e.getMessage());
                } catch (Throwable t) {
                    failHt++;
                    Console.log("推送致命错误 id=" + row.id + ", xglc=" + row.xglc
                            + ", error=" + t.getClass().getName() + ": " + t.getMessage());
                }
                continue;
            }

            // 两者都无值 → 跳过
            skipNoFile++;
            Console.log("跳过：fd_id 与 xglc 均为空, id=" + row.id + ", wf=" + row.wf);
        }

        Console.log("====HtAttachPushToEsbJobV7 Finish===="
                + " 合同推送成功=" + pushedHt + ", 合同失败=" + failHt
                + ", 合同无附件跳过=" + skipNoFile + ", 走xglc分支=" + xglcBranch
                + " | 附件上传成功=" + pushedFile + ", 附件失败=" + failFile
                + ", uf_ht 合计=" + rows.size());
    }

    // ==================================================================
    // 取数
    // ==================================================================

    /**
     * 取 {@code uf_ht} 中 {@code needPushZ = 1} 的记录。
     * <p>
     * 按<b>列名</b>取值：{@link RecordSet#getString(String)} 内部用
     * {@code equalsIgnoreCase} 找列并自行转 1 基，列不存在返回空串。
     */
    private List<HtRow> queryHtRows() {
        List<HtRow> rows = new ArrayList<>();
        RecordSet rs = new RecordSet();
        try {
            if (!rs.executeQuery(MAIN_SQL)) {
                Console.log("查询 uf_ht 失败: " + MAIN_SQL);
                return rows;
            }
            while (rs.next()) {
                HtRow row = new HtRow();
                row.id = col(rs, COL_ID);
                row.fdId = col(rs, COL_FD_ID);
                row.xglc = col(rs, COL_XGLC);
                row.fj = col(rs, COL_FJ);
                row.yyhwj = col(rs, COL_YYHWJ);
                row.wf = col(rs, COL_WF);
                if (row.id.isEmpty() && row.fdId.isEmpty() && row.xglc.isEmpty()) {
                    continue;
                }
                rows.add(row);
            }
        } catch (Exception e) {
            Console.log("查询 uf_ht 异常, sql=" + MAIN_SQL
                    + ", error=" + e.getClass().getName() + ": " + e.getMessage());
        }
        return rows;
    }

    /**
     * 按列名取字符串值（trim + 去 {@code .0} 尾巴）。
     * <p>
     * PostgreSQL 数值列经 {@code getString()} 可能返回 {@code "123.0"}，
     * 直接当 e9id 推出去会对不上，这里只处理「纯整数 + .0」这一种形态。
     */
    private String col(RecordSet rs, String columnName) {
        try {
            String s = Util.null2String(rs.getString(columnName)).trim();
            if (s.isEmpty()) {
                return "";
            }
            if (s.matches("^-?\\d+\\.0+$")) {
                return s.substring(0, s.indexOf('.'));
            }
            return s;
        } catch (Exception e) {
            return "";
        }
    }

    /** fd_id → 文档 id 列表（沿用 LostHtFileExportJob 的关联方式，含 CAST 兜底） */
    private List<String> queryDocIdsByFdId(HtRow row) {
        List<String> docIds = new ArrayList<>();
        RecordSet rs = new RecordSet();
        boolean ok = false;
        try {
            // 1. 先按字符串绑
            ok = rs.executeQuery(FILE_LINK_SQL, row.fdId);
            // 2. 失败再按数字绑
            if (!ok) {
                int fdIdInt = Util.getIntValue(row.fdId);
                if (fdIdInt > 0) {
                    rs = new RecordSet();
                    ok = rs.executeQuery(FILE_LINK_SQL, fdIdInt);
                }
            }
            // 3. 仍失败用 CAST 兜底
            if (!ok) {
                rs = new RecordSet();
                ok = rs.executeQuery(FILE_LINK_CAST_SQL, row.fdId);
            }
            if (!ok) {
                Console.log("查询文档id失败, id=" + row.id + ", fd_id=" + row.fdId);
                return docIds;
            }
            Set<String> seen = new LinkedHashSet<>();
            while (rs.next()) {
                String wdidRaw = Util.null2String(rs.getString("wdid")).trim();
                if (wdidRaw.isEmpty()) {
                    continue;
                }
                for (String one : wdidRaw.split(",")) {
                    String docId = one.trim();
                    if (!docId.isEmpty() && seen.add(docId)) {
                        docIds.add(docId);
                    }
                }
            }
        } catch (Exception e) {
            Console.log("查询文档id异常, id=" + row.id + ", fd_id=" + row.fdId
                    + ", error=" + e.getClass().getName() + ": " + e.getMessage());
        }
        if (docIds.isEmpty()) {
            Console.log("fd_id 关联不到文档, id=" + row.id + ", fd_id=" + row.fdId);
        }
        return docIds;
    }

    /** 文档 id → 附件列表（同一 imagefileid 只保留一次） */
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

    // ==================================================================
    // 两条分支
    // ==================================================================

    /** fd_id 分支：fd_id → docid 列表 → 上传并更新合同 */
    private HtResult pushByFdId(HtRow row) {
        List<String> docIds = queryDocIdsByFdId(row);
        if (docIds.isEmpty()) {
            return new HtResult();
        }
        return pushByDocIds(row, docIds, "fd_id");
    }

    /** xglc 分支：从 fj / yyhwj 取 docid */
    private HtResult pushByXglc(HtRow row) {
        List<String> docIds = mergeDocIds(row.fj, row.yyhwj);
        if (docIds.isEmpty()) {
            Console.log("xglc 分支：fj 与 yyhwj 都没有有效 docid, id=" + row.id
                    + ", xglc=" + row.xglc
                    + ", fj=[" + row.fj + "], yyhwj=[" + row.yyhwj + "]");
            return new HtResult();
        }
        Console.log("xglc 分支：id=" + row.id + ", xglc=" + row.xglc
                + ", 合并去重后 docid 数=" + docIds.size() + " -> " + docIds);
        return pushByDocIds(row, docIds, "xglc");
    }

    /** 合并两个多选字段的 docid，按出现顺序去重 */
    private List<String> mergeDocIds(String fj, String yyhwj) {
        Set<String> seen = new LinkedHashSet<>();
        collectDocIds(fj, COL_FJ, seen);
        collectDocIds(yyhwj, COL_YYHWJ, seen);
        return new ArrayList<>(seen);
    }

    /** 解析单个多选字段并收进 seen（只保留正整数） */
    private void collectDocIds(String raw, String fieldName, Set<String> seen) {
        String value = Util.null2String(raw).trim();
        if (value.isEmpty()) {
            return;
        }
        // 去掉多选/数组常见外壳
        value = value.replace("[", "").replace("]", "")
                .replace("\"", "").replace("'", "");
        for (String one : value.split(",")) {
            String docId = one.trim();
            if (docId.isEmpty()) {
                continue;
            }
            if (Util.getIntValue(docId) <= 0) {
                Console.log("忽略非 docid 值, 字段=" + fieldName + ", 值=[" + docId + "]");
                continue;
            }
            seen.add(docId);
        }
    }

    // ==================================================================
    // 核心：docid → 附件 → 上传 → 更新合同
    // ==================================================================

    /**
     * docid 列表 → 附件 → <b>逐个上传</b> → 汇总 fileid → 更新合同。
     * <p>
     * 流程（本地已实测通过）：
     * <ol>
     *   <li>每个附件用 {@link ImageFileManager#getInputStreamById(int)} 取流；</li>
     *   <li>multipart 上传到 {@link #UPLOAD_PATH}，拿 {@code data.fileid}；</li>
     *   <li>把该合同下所有成功的 fileid 汇总，调用一次
     *       {@code {"e9id":..., "fileids":[...]}} 更新合同。</li>
     * </ol>
     * 上传失败或拿不到 fileid 的附件跳过并记日志，不影响其它附件。
     *
     * @param source 来源标识（fd_id / xglc），仅用于日志区分
     */
    private HtResult pushByDocIds(HtRow row, List<String> docIds, String source) {
        HtResult result = new HtResult();

        // e9id 固定取 uf_ht.id
        String e9id = row.id;
        if (e9id.isEmpty()) {
            Console.log("跳过推送：uf_ht." + COL_ID + " 为空，无法作为 e9id, source=" + source
                    + ", fd_id=" + row.fdId + ", wf=" + row.wf);
            result.failed++;
            return result;
        }

        List<String> fileIds = new ArrayList<>();
        int pushedCount = 0;

        for (String docId : docIds) {
            Console.log("开始处理 docId=" + docId + ", source=" + source + ", id=" + row.id);
            List<DocAttach> atts = queryAttachByDocId(docId);
            Console.log("docId=" + docId + " 查到附件数=" + atts.size());
            for (DocAttach att : atts) {
                if (MAX_PUSH_PER_RUN > 0 && pushedCount >= MAX_PUSH_PER_RUN) {
                    Console.log("已达单次上限 MAX_PUSH_PER_RUN=" + MAX_PUSH_PER_RUN
                            + "，剩余附件留待下次, id=" + row.id);
                    break;
                }

                String fileName = resolveOriginalName(att);
                Console.log("开始上传 imagefileid=" + att.imagefileId + ", fileName=" + fileName);
                String fileId = uploadOne(att.imagefileId, fileName);
                if (fileId == null || fileId.isEmpty()) {
                    result.failed++;
                    Console.log("上传失败，跳过该附件, imagefileid=" + att.imagefileId
                            + ", fileName=" + fileName);
                    continue;
                }
                fileIds.add(fileId);
                pushedCount++;
                Console.log("上传成功: imagefileid=" + att.imagefileId
                        + ", fileName=" + fileName + ", fileid=" + fileId);
            }
        }

        if (fileIds.isEmpty()) {
            Console.log("无可上传附件（文档下无附件或全部上传失败）, source=" + source
                    + ", id=" + row.id + ", fd_id=" + row.fdId);
            return result;
        }

        // 汇总 fileid 更新合同
        JSONObject resp = updateContract(e9id, fileIds);
        if (isSuccess(resp)) {
            result.pushed += fileIds.size();
            Console.log(source + " 更新合同成功: id=" + row.id + ", e9id=" + e9id
                    + ", fileid数=" + fileIds.size() + ", 响应=" + resp);
        } else {
            result.failed += fileIds.size();
            Console.log(source + " 更新合同失败: id=" + row.id + ", e9id=" + e9id
                    + ", fileid数=" + fileIds.size()
                    + ", 响应=" + (resp == null ? "null" : resp.toString())
                    + ", 失败原因=" + errorText(resp));
        }
        return result;
    }

    /**
     * 上传单个附件，返回 fileid；失败返回 null。
     * <p>
     * 接口：{@code POST /papi/openapi/api/file/v2/common/upload?access_token=xxx}
     * form-data 必填：{@code module=document}、{@code name=文件名}、
     * {@code file=二进制}、{@code userid=固定值}。
     */
    private String uploadOne(int imagefileId, String fileName) {
        // 1. 取附件流（E9 原生，兼容 FTP/OSS/加密存储）
        InputStream in;
        try {
            in = ImageFileManager.getInputStreamById(imagefileId);
        } catch (Exception e) {
            Console.log("取附件流出错, imagefileid=" + imagefileId
                    + ", error=" + e.getClass().getName() + ": " + e.getMessage());
            return null;
        }
        if (in == null) {
            Console.log("附件流为空, imagefileid=" + imagefileId);
            return null;
        }

        File tmp = null;
        try {
            // 2. 先落临时文件：multipart 上传需要真实文件体
            tmp = File.createTempFile("htupload_", ".bin");
            FileUtils.copyInputStreamToFile(in, tmp);

            E10Utils utils = E10Utils.getInstance(E10_CODE);
            String token = utils.getAccessToken();
            if (Util.null2String(token).isEmpty()) {
                Console.log("获取 access_token 失败，无法上传, imagefileid=" + imagefileId);
                return null;
            }
            String url = E10Config.getInstance(E10_CODE).getE10Url() + UPLOAD_PATH
                    + "?access_token=" + token;

            RequestBody fileBody = RequestBody.create(MediaType.parse("application/octet-stream"), tmp);
            // filename 用原始文件名，服务端会据此设置 name 与扩展名
            // createDocument=true 时会在 UPLOAD_FOLDER_ID 对应目录下生成文档
            MultipartBody body = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("module", UPLOAD_MODULE)
                    .addFormDataPart("name", fileName)
                    .addFormDataPart("userid", UPLOAD_USERID)
                    .addFormDataPart("createDocument", UPLOAD_CREATE_DOCUMENT)
                    .addFormDataPart("folderId", UPLOAD_FOLDER_ID)
                    .addFormDataPart("file", fileName, fileBody)
                    .build();

            OkHttpClient client = new OkHttpClient().newBuilder()
                    .connectTimeout(CONNECT_TIMEOUT_SEC, TimeUnit.SECONDS)
                    .readTimeout(READ_TIMEOUT_SEC, TimeUnit.SECONDS)
                    .writeTimeout(READ_TIMEOUT_SEC, TimeUnit.SECONDS)
                    .build();
            Request request = new Request.Builder().url(url).post(body).build();
            Response response = client.newCall(request).execute();
            String respBody = response.body() == null ? "" : response.body().string();
            Console.log("上传 HTTP=" + response.code() + ", 响应=" + respBody);

            JSONObject json = new JSONObject(respBody);
            if (!isSuccess(json)) {
                return null;
            }
            JSONObject data = json.optJSONObject("data");
            if (data == null) {
                Console.log("上传响应缺少 data 节点");
                return null;
            }
            return Util.null2String(data.optString("fileid")).trim();
        } catch (Exception e) {
            Console.log("上传异常, imagefileid=" + imagefileId
                    + ", error=" + e.getClass().getName() + ": " + e.getMessage());
            return null;
        } finally {
            closeQuietly(in);
            if (tmp != null && tmp.exists() && !tmp.delete()) {
                Console.log("临时文件删除失败, path=" + tmp.getAbsolutePath());
            }
        }
    }

    /**
     * 调用合同更新 WebHook：{@code {"e9id":..., "fileids":[...]}}。
     * <p>
     * 复用 {@link E10Utils#callWebhook}（已内置 access_token query 拼接）。
     */
    private JSONObject updateContract(String e9id, List<String> fileIds) {
        try {
            JSONArray arr = new JSONArray();
            for (String id : fileIds) {
                arr.put(id);
            }
            JSONObject param = new JSONObject();
            param.put("e9id", e9id);
            param.put("fileids", arr);
            Console.log("更新合同请求: e9id=" + e9id + ", fileids=" + arr);

            E10Utils utils = E10Utils.getInstance(E10_CODE);
            return utils.callWebhook(param, WEBHOOK_PATH);
        } catch (Exception e) {
            Console.log("更新合同异常, e9id=" + e9id
                    + ", error=" + e.getClass().getName() + ": " + e.getMessage());
            return null;
        }
    }

    /** 安静关闭流 */
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

    /**
     * 取「原始文件名」。
     * <p>
     * 优先 {@code docimagefile.imagefilename}；为空或无扩展名时回退
     * {@code imagefile.imagefilename}；仍取不到则用 {@code 附件_{imagefileid}}。
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

    /** 清理文件名：去掉路径分隔符等非法字符，保留中文与原扩展名 */
    private String sanitizeFileName(String raw) {
        String s = Util.null2String(raw).trim();
        s = s.replaceAll("[\\\\/:*?\"<>|\\r\\n\\t]", "_");
        while (s.contains("..")) {
            s = s.replace("..", "_");
        }
        s = s.replaceAll("^[.\\s]+", "");
        return s.isEmpty() ? "未命名附件" : s;
    }

    // ==================================================================
    // 响应判定
    // ==================================================================

    /**
     * 成功判定。
     * <p>
     * 三种返回结构都要认：
     * <ul>
     *   <li>上传接口：{@code {"message":{"errcode":"0","errmsg":"success"}, "data":{...}}}
     *       —— errcode 为 <b>0</b> 表示成功；</li>
     *   <li>合同更新失败：{@code {"message":{"errcode":"500","errmsg":"..."}}} 或
     *       {@code {"errcode":"200007","errmsg":"..."}}；</li>
     *   <li><b>合同更新成功</b>：实测返回 {@code {"e9id":"","fileName":"","fileBase64":""}}
     *       —— <b>既没有 code 也没有 errcode</b>，是更新接口固定回显的占位结构。
     *       不能因为没有成功码就判失败，否则上传全成功也会被记成失败。</li>
     * </ul>
     * 因此规则是：<b>只要没有明确的失败标志，就判成功</b>。
     */
    private boolean isSuccess(JSONObject resp) {
        if (resp == null) {
            return false;
        }
        // 1. 有明确错误码 → 按错误码判
        JSONObject message = resp.optJSONObject("message");
        if (message != null) {
            String code = Util.null2String(message.optString("errcode")).trim();
            if (!code.isEmpty()) {
                // 上传接口成功码是 0；部分接口用 200
                return "0".equals(code) || "200".equals(code);
            }
            // message 里没有 errcode 但有 errmsg → 视为失败信息
            return Util.null2String(message.optString("errmsg")).trim().isEmpty();
        }
        String code = firstNonEmpty(resp.optString("code"), resp.optString("errcode"));
        if (!code.isEmpty()) {
            return "0".equals(code) || "200".equals(code);
        }
        // 2. 没有 code/errcode：只有 errmsg 且非空才判失败
        String errmsg = Util.null2String(resp.optString("errmsg")).trim();
        if (!errmsg.isEmpty()) {
            return false;
        }
        // 3. 合同更新成功就是 {"e9id":"","fileName":"","fileBase64":""} 这类占位回显
        return true;
    }

    private String firstNonEmpty(String a, String b) {
        if (!Util.null2String(a).trim().isEmpty()) {
            return a.trim();
        }
        return Util.null2String(b).trim();
    }

    /** 提取失败原因（兼容嵌套 message 与平铺两种结构） */
    private String errorText(JSONObject resp) {
        if (resp == null) {
            return "无响应或调用异常";
        }
        JSONObject message = resp.optJSONObject("message");
        if (message != null) {
            String errmsg = Util.null2String(message.optString("errmsg"));
            if (!errmsg.isEmpty()) {
                return errmsg;
            }
        }
        String errmsg = Util.null2String(resp.optString("errmsg"));
        String errcode = Util.null2String(resp.optString("errcode"));
        if (!errmsg.isEmpty()) {
            return errmsg + (errcode.isEmpty() ? "" : "(errcode=" + errcode + ")");
        }
        if (!errcode.isEmpty()) {
            return "errcode=" + errcode;
        }
        return resp.toString();
    }

    // ==================================================================
    // 内部类型
    // ==================================================================

    /** uf_ht 的一行 */
    private static class HtRow {
        /** 合同主键（uf_ht.id），同时作为报文 e9id */
        String id = "";
        /** 外部文件标识（uf_ht.fd_id） */
        String fdId = "";
        /** 流程 id（uf_ht.xglc） */
        String xglc = "";
        /** 附件字段一（uf_ht.fj）：多选，逗号分隔 docid */
        String fj = "";
        /** 附件字段二（uf_ht.yyhwj）：多选，逗号分隔 docid */
        String yyhwj = "";
        /** 合同名称（uf_ht.wf），仅日志用 */
        String wf = "";
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

    /** 单份合同的结果 */
    private static class HtResult {
        int pushed;
        int failed;
    }
}
