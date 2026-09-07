package csj.treas_off;

import csj.utils.Console;
import okhttp3.*;
import org.json.JSONObject;

/**
 * @Author: 张骏山
 * @Date: 2026/9/7 11:46
 * @PackageName: csj.treas_off
 * @ClassName: GetTreasTodoSize
 * @Description: 司库单点登录后获取待办总条数
 * @Version: 1.0
 */
public class GetTreasTodoSize {

    /**
     * 获取司库待办总条数 工号传入形式
     * 1. 调用 generateTokenBySAI 获取单点登录token
     * 2. 调用 loginWithTokenForTodoBySAI 获取待办总条数
     *
     * @param workCode 工号
     * @return 待办总条数
     */
    public String getTodoSize(String workCode) {
        try {
            // 1. 获取司库单点登录token
            GetTreasTokenV4 getTreasToken = new GetTreasTokenV4();
            String token = getTreasToken.getToken(workCode);
            Console.log("token => " + token);
            if (token == null || token.isEmpty()) {
                Console.log("获取token失败,无法查询待办总条数");
                return null;
            }

            // 2. 调用待办总条数接口
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("token", token);

            // 新版接口地址拼接,与generateTokenBySAI保持一致
            String apiUrl = TreasConfig.getUrl() + "/api/EIB.ATP.advcommon/t0/loginWithTokenForTodoBySAI";

            // 发送POST请求,保留原有的HTTP客户端逻辑
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, jsonObject.toString());
            Request request = new Request.Builder()
                    .url(apiUrl)
                    .method("POST", body)
                    .addHeader("Content-Type", "application/json")
                    .build();
            Console.log("request = " + request);
            Console.log("jsonObject = " + jsonObject);

            Response response = client.newCall(request).execute();
            Console.log("code => " + response.code());
            JSONObject rtnJson = new JSONObject(response.body().string());
            Console.log("rtnJson => " + rtnJson);

            // 3. 读取待办总条数字段
            String todoSize = rtnJson.optString("todoSize");
            if (todoSize == null || todoSize.isEmpty()) {
                Console.log("获取待办总条数失败,接口返回todoSize为空");
                return null;
            }
            return todoSize;
        } catch (Exception e) {
            Console.log("获取待办总条数失败,验证异常:" + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
//        TreasConfig.init();
        GetTreasTodoSize getTreasTodoSize = new GetTreasTodoSize();
        String workCode = args.length > 0 ? args[0] : "CSJTZ051";
        String todoSize = getTreasTodoSize.getTodoSize(workCode);
        System.out.println("todoSize = " + todoSize);
    }
}
