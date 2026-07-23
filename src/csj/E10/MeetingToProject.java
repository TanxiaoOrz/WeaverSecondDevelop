package csj.E10;

import ISS.util.Console;
import okhttp3.*;
import org.json.JSONObject;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.RequestInfo;

/**
 * @Author: 张骏山
 * @Date: 2026/7/23
 * @PackageName: csj.E10
 * @ClassName: MeetingToProject
 * @Description: e9会议流程结束后 新建项目 ESB-WebHook调用
 * @Version: 1.0
 */
public class MeetingToProject implements Action {

    // WebHook触发路径
    private static final String WEBHOOK_PATH = "/papi/openapi/api/open-esb/server/webhook/trigger/NWU2NTIwOWE5YTVlNDkwZTg1NTU3ODUwNjUyOGE1NGE=";

    @Override
    public String execute(RequestInfo requestInfo) {
        String requestid = String.valueOf(requestInfo.getRequestManager().getRequestid());
        Console.log("MeetingToProject execute: requestid=" + requestid);
        String result = callWebhook(requestid);
        Console.log("MeetingToProject execute: requestid=" + requestid + ", result=" + result);
        return result;
    }

    /**
     * 调用ESB-WebHook新建项目接口
     * @param requestid e9流程id
     * @return 响应code, "200"表示成功, 其余失败
     */
    public String callWebhook(String requestid) {
        try {
            E10Config config = E10Config.getInstance();
            JSONObject param = new JSONObject();
            param.put("requestid", requestid);

            String apiUrl = config.getE10Url() + WEBHOOK_PATH;

            OkHttpClient client = new OkHttpClient().newBuilder().build();
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, param.toString());
            Request request = new Request.Builder()
                    .url(apiUrl)
                    .method("POST", body)
                    .addHeader("Content-Type", "application/json")
                    .build();
            Response response = client.newCall(request).execute();
            JSONObject rtnJson = new JSONObject(response.body().string());
            return rtnJson.getString("code");
        } catch (Exception e) {
            java.io.StringWriter sw = new java.io.StringWriter();
            e.printStackTrace(new java.io.PrintWriter(sw, true));
            Console.log("callWebhook异常: requestid=" + requestid + ", error=" + e.getMessage() + "\n" + sw.toString());
            return "";
        }
    }

    /**
     * 独立测试方法
     * @param args 命令行参数, args[0]为requestid, 默认使用326190
     */
    public static void main(String[] args) {
        MeetingToProject meeting = new MeetingToProject();
        String requestid = args.length > 0 ? args[0] : "326190";
        System.out.println("requestid = " + requestid);
        String result = meeting.callWebhook(requestid);
        System.out.println("result = " + result);
        System.out.println("success".equals(result) ? "调用成功" : "调用失败");
    }

}