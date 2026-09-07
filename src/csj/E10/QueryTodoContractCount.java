package csj.E10;

import csj.utils.Console;
import org.json.JSONObject;

/**
 * @Author: 张骏山
 * @Date: 2026/09/07
 * @PackageName: csj.E10
 * @ClassName: QueryTodoContractCount
 * @Description: 根据工号查询待履约合同数量 ESB-WebHook调用
 * @Version: 1.0
 */
public class QueryTodoContractCount {

    // WebHook触发路径
    private static final String WEBHOOK_PATH = "/papi/openapi/api/open-esb/server/webhook/trigger/YzIwZTJmZmFmZDc1NDRmMWE5NjMwMTc4NDA4NjFlNmU=";

    public String e10Code;

    /**
     * 根据工号查询待履约合同数量
     * @param workcode 员工工号
     * @return 待履约数量, 调用失败或响应缺少todo_count字段时返回null
     */
    public String queryTodoCount(String workcode) {
        JSONObject result = callWebhook(workcode);
        if (result == null) {
            Console.log("queryTodoCount: workcode=" + workcode + ", 调用WebHook返回null");
            return null;
        }
        // 响应格式: {"todo_count":"#string", "esb_paramsetting_showName":{"todo_count":"待履约数量"}}
        if (result.has("todo_count")) {
            String todoCount = result.optString("todo_count");
            Console.log("queryTodoCount: workcode=" + workcode + ", todo_count=" + todoCount);
            return todoCount;
        }
        Console.log("queryTodoCount: workcode=" + workcode + ", 响应缺少todo_count字段, response=" + result);
        return null;
    }

    /**
     * 调用ESB-WebHook根据工号查询待履约合同数量接口
     * @param workcode 员工工号
     * @return 响应JSON, 成功: {"todo_count":"...", ...}
     */
    public JSONObject callWebhook(String workcode) {
        try {
            E10Utils utils = E10Utils.getInstance(e10Code);
            JSONObject param = new JSONObject();
            param.put("access_token", utils.getAccessToken());
            param.put("workcode", workcode);
            return utils.callWebhook(param, WEBHOOK_PATH);
        } catch (Exception e) {
            Console.log("callWebhook异常: workcode=" + workcode + ", error=" + e.getMessage());
            return null;
        }
    }

    /**
     * 独立测试方法
     * @param args 命令行参数, args[0]为workcode, 默认使用xingwb
     */
    public static void main(String[] args) {
        QueryTodoContractCount query = new QueryTodoContractCount();
        String workcode = args.length > 0 ? args[0] : "xingwb";
        System.out.println("workcode = " + workcode);
        String todoCount = query.queryTodoCount(workcode);
        System.out.println("todo_count = " + todoCount);
    }

}