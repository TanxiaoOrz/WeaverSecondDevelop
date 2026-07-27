package csj.E10;

import csj.utils.Console;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import weaver.conn.RecordSet;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.Property;
import weaver.soa.workflow.request.RequestInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Base64;

/**
 * @Author: 张骏山
 * @Date: 2026/7/27
 * @PackageName: csj.E10
 * @ClassName: MeetingArchiveToProjectAction
 * @Description: e9会议纪要流程结束后 归档更新项目 ESB-WebHook调用
 * @Version: 1.0
 */
public class MeetingArchiveToProjectAction implements Action {

    // WebHook触发路径
    private static final String WEBHOOK_PATH = "/papi/openapi/api/open-esb/server/webhook/trigger/NzdiYzczNjM1ODMzNGYyMGFiZjhiMTI4OWIwMTRmNmI=";

    public String e10Code;

    /**
     * 主表字段名称，逗号分割传入，用于获取fileIds
     * 按顺序匹配，取第一个匹配到的非空字段值
     */
    public String mainfileIdsColumnNames;

    @Override
    public String execute(RequestInfo requestInfo) {
        String requestid = String.valueOf(requestInfo.getRequestManager().getRequestid());
        Console.log("MeetingArchiveToProjectAction execute: requestid=" + requestid);

        // 1. 获取主表字段值(fileIds)
        String fileIds = getFileIdsFromMainEntries(requestInfo);
        if (fileIds == null || fileIds.isEmpty()) {
            Console.log("MeetingArchiveToProjectAction: mainfileIdsColumnNames未找到对应字段值");
            requestInfo.getRequestManager().setMessagecontent("未找到文件ID字段值");
            return Action.FAILURE_AND_CONTINUE;
        }
        Console.log("MeetingArchiveToProjectAction: fileIds=" + fileIds);

        // 2. 通过fileIds查询文件信息并转换为Base64
        JSONArray fileArray = buildFileArray(fileIds);
        if (fileArray == null || fileArray.length() == 0) {
            Console.log("MeetingArchiveToProjectAction: 未查询到有效文件");
            requestInfo.getRequestManager().setMessagecontent("未查询到有效文件");
            return Action.FAILURE_AND_CONTINUE;
        }
        Console.log("MeetingArchiveToProjectAction: fileArray length=" + fileArray.length());

        // 3. 调用WebHook
        JSONObject result = callWebhook(requestid, fileArray);
        Console.log("MeetingArchiveToProjectAction execute: requestid=" + requestid + ", result=" + result);

        // 解析返回结果: {"code":"200"} 表示成功
        if (result != null && result.has("code") && "200".equals(result.optString("code"))) {
            return Action.SUCCESS;
        }

        // 提取失败信息
        String errmsg = "调用WebHook失败";
        if (result != null && result.has("message")) {
            JSONObject message = result.optJSONObject("message");
            if (message != null && message.has("errmsg")) {
                errmsg = message.optString("errmsg");
            } else if (message != null && message.has("errcode")) {
                errmsg = "错误码: " + message.optString("errcode");
            }
        }
        requestInfo.getRequestManager().setMessagecontent(errmsg);
        return Action.FAILURE_AND_CONTINUE;
    }

    /**
     * 从主表字段中获取fileIds
     * mainfileIdsColumnNames逗号分割，按顺序取第一个匹配到的非空字段值
     *
     * @param requestInfo 流程请求信息
     * @return fileIds字符串(逗号分割)，未找到返回null
     */
    private String getFileIdsFromMainEntries(RequestInfo requestInfo) {
        if (mainfileIdsColumnNames == null || mainfileIdsColumnNames.isEmpty()) {
            Console.log("getFileIdsFromMainEntries: mainfileIdsColumnNames为空");
            return null;
        }
        Property[] mainEntries = requestInfo.getMainTableInfo().getProperty();
        String[] columnNames = mainfileIdsColumnNames.split(",");
        for (String columnName : columnNames) {
            String trimmedName = columnName.trim();
            if (trimmedName.isEmpty()) {
                continue;
            }
            try {
                String value = Arrays.stream(mainEntries)
                        .filter(mainEntry -> mainEntry.getName().equals(trimmedName))
                        .findFirst()
                        .get()
                        .getValue();
                if (value != null && !value.isEmpty()) {
                    Console.log("getFileIdsFromMainEntries: 字段" + trimmedName + "=" + value);
                    return value;
                }
            } catch (Exception e) {
                Console.log("getFileIdsFromMainEntries: 字段" + trimmedName + "未找到, error=" + e.getMessage());
            }
        }
        return null;
    }

    /**
     * 根据fileIds(逗号分割)查询imagefile表，获取文件名和路径，转换为Base64
     *
     * @param fileIds 逗号分割的文件ID
     * @return JSONArray [{"fileName":"...", "fileBase64":"..."}]
     */
    private JSONArray buildFileArray(String fileIds) {
        JSONArray fileArray = new JSONArray();
        String[] ids = fileIds.split(",");
        RecordSet rs = new RecordSet();

        for (String id : ids) {
            String trimmedId = id.trim();
            if (trimmedId.isEmpty()) {
                continue;
            }
            try {
                String sql = "SELECT imagefilename, filerealpath FROM imagefile WHERE imagefileid = " + trimmedId;
                Console.log("buildFileArray sql: " + sql);
                rs.executeQuery(sql);
                if (rs.next()) {
                    String fileName = rs.getString("imagefilename");
                    String filePath = rs.getString("filerealpath");
                    Console.log("buildFileArray: imagefileid=" + trimmedId + ", fileName=" + fileName + ", filePath=" + filePath);

                    if (filePath != null && !filePath.isEmpty()) {
                        String base64 = fileToBase64(filePath);
                        if (base64 != null) {
                            JSONObject fileObj = new JSONObject();
                            fileObj.put("fileName", fileName != null ? fileName : "");
                            fileObj.put("fileBase64", base64);
                            fileArray.put(fileObj);
                        }
                    }
                } else {
                    Console.log("buildFileArray: imagefileid=" + trimmedId + " 未找到记录");
                }
            } catch (Exception e) {
                Console.log("buildFileArray异常: imagefileid=" + trimmedId + ", error=" + e.getMessage());
            }
        }
        return fileArray;
    }

    /**
     * 读取文件并转换为Base64字符串
     *
     * @param filePath 文件路径
     * @return Base64编码字符串，失败返回null
     */
    private String fileToBase64(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            Console.log("fileToBase64: 文件不存在, path=" + filePath);
            return null;
        }
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] bytes = new byte[(int) file.length()];
            int read = fis.read(bytes);
            Console.log("fileToBase64: path=" + filePath + ", size=" + read + " bytes");
            return Base64.getEncoder().encodeToString(bytes);
        } catch (IOException e) {
            Console.log("fileToBase64异常: path=" + filePath + ", error=" + e.getMessage());
            return null;
        }
    }

    /**
     * 调用ESB-WebHook会议纪要归档更新项目接口
     *
     * @param requestid e9流程id
     * @param fileArray 文件数组 [{"fileName":"...", "fileBase64":"..."}]
     * @return 响应JSON, 成功: {"code":"200"}, 失败: {"message":{"errcode":"500","errmsg":"..."}}
     */
    public JSONObject callWebhook(String requestid, JSONArray fileArray) {
        try {
            E10Utils utils = E10Utils.getInstance(e10Code);
            String apiUrl = E10Config.getInstance(e10Code).getE10Url() + WEBHOOK_PATH
                    + "?access_token=" + utils.getAccessToken()
                    + "&requestid=" + requestid;

            Console.log("callWebhook url: " + apiUrl);

            OkHttpClient client = new OkHttpClient().newBuilder().build();
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, fileArray.toString());
            Request request = new Request.Builder()
                    .url(apiUrl)
                    .method("POST", body)
                    .addHeader("Content-Type", "application/json")
                    .build();
            Response response = client.newCall(request).execute();
            JSONObject rtnJson = new JSONObject(response.body().string());
            Console.log("callWebhook成功: requestid=" + requestid + ", response=" + rtnJson.toString());
            return rtnJson;
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw, true));
            Console.log("callWebhook异常: requestid=" + requestid
                    + ", error=" + e.getMessage() + "\n" + sw.toString());
            return null;
        }
    }

    /**
     * 独立测试方法
     *
     * @param args 命令行参数, args[0]为requestid, args[1]为fileIds(逗号分割), 默认使用326190
     */
    public static void main(String[] args) {
        MeetingArchiveToProjectAction action = new MeetingArchiveToProjectAction();
        String requestid = args.length > 0 ? args[0] : "326190";
        String fileIds = args.length > 1 ? args[1] : "";
        System.out.println("requestid = " + requestid);
        System.out.println("fileIds = " + fileIds);

        if (!fileIds.isEmpty()) {
            JSONArray fileArray = action.buildFileArray(fileIds);
            System.out.println("fileArray = " + fileArray.toString());
            JSONObject result = action.callWebhook(requestid, fileArray);
            System.out.println("result = " + result);
            if (result != null && result.has("code") && "200".equals(result.optString("code"))) {
                System.out.println("调用成功");
            } else if (result != null && result.has("message")) {
                JSONObject message = result.optJSONObject("message");
                System.out.println("调用失败: " + (message != null ? message.optString("errmsg") : "未知错误"));
            } else {
                System.out.println("调用失败: 未知错误");
            }
        } else {
            System.out.println("请传入fileIds参数");
        }
    }

}