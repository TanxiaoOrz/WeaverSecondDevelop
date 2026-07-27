package csj.E10;

import csj.utils.Console;
import org.json.JSONObject;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.RequestInfo;

/**
 * @Author: 张骏山
 * @Date: 2026/7/23
 * @PackageName: csj.E10
 * @ClassName: MeetingToProjectAction
 * @Description: e9会议流程结束后 新建项目 ESB-WebHook调用
 * @Version: 1.0
 */
public class MeetingToProjectAction implements Action {

    // WebHook触发路径
    private static final String WEBHOOK_PATH = "/papi/openapi/api/open-esb/server/webhook/trigger/NWU2NTIwOWE5YTVlNDkwZTg1NTU3ODUwNjUyOGE1NGE=";
    //
    public String e10Code;

    @Override
    public String execute(RequestInfo requestInfo) {
        String requestid = String.valueOf(requestInfo.getRequestManager().getRequestid());
        Console.log("MeetingToProjectAction execute: requestid=" + requestid);
        JSONObject result = callWebhook(requestid);
        Console.log("MeetingToProjectAction execute: requestid=" + requestid + ", result=" + result);

        // 解析返回结果: {"code":"200"} 表示成功
        if (result != null && result.has("code") && "200".equals(result.optString("code"))) {
            return Action.SUCCESS;
        }

        // 提取失败信息: {"message":{"errcode":"500","errmsg":"..."}}
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
     * 调用ESB-WebHook新建项目接口
     * @param requestid e9流程id
     * @return 响应JSON, 成功: {"code":"200"}, 失败: {"message":{"errcode":"500","errmsg":"..."}}
     */
    public JSONObject callWebhook(String requestid) {
        try {
            E10Utils utils = E10Utils.getInstance(e10Code);
            JSONObject param = new JSONObject();
            param.put("access_token", utils.getAccessToken());
            param.put("requestid", requestid);
            return utils.callWebhook(param, WEBHOOK_PATH);
        } catch (Exception e) {
            Console.log("callWebhook异常: requestid=" + requestid + ", error=" + e.getMessage());
            return null;
        }
    }

    /**
     * 独立测试方法
     * @param args 命令行参数, args[0]为requestid, 默认使用326190
     */
    public static void main(String[] args) {
        MeetingToProjectAction meeting = new MeetingToProjectAction();
        String requestid = args.length > 0 ? args[0] : "326190";
        System.out.println("requestid = " + requestid);
        JSONObject result = meeting.callWebhook(requestid);
        System.out.println("result = " + result);
        if (result != null && result.has("code") && "200".equals(result.optString("code"))) {
            System.out.println("调用成功");
        } else if (result != null && result.has("message")) {
            JSONObject message = result.optJSONObject("message");
            System.out.println("调用失败: " + (message != null ? message.optString("errmsg") : "未知错误"));
        } else {
            System.out.println("调用失败: 未知错误");
        }
    }

}