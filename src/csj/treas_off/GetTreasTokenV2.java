package csj.treas_off;

import csj.utils.Console;
import okhttp3.*;
import org.json.JSONObject;

/**
 * @Author: 付金明
 * @Date: 2026/5/6 11:46
 * @PackageName: csj.treas_off
 * @ClassName: GetTreasTokenV2
 * @Description: 司库OSS
 * @Version: 1.0
 */
public class GetTreasTokenV2 {

    /**
     * 获取司库 token 工号传入形式
     * @param workCode 工号
     * @return token
     */
    public String getToken(String workCode) {
        try {
//            Console.log ("workCode=>"+workCode);
            //接口请求参数
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("cifSeq", TreasConfig.getCifSeq());
            jsonObject.put("userDomain", workCode);
//            Console.log ("jsonObject=>"+jsonObject.toString());
            //新版接口地址拼接
            String apiUrl = TreasConfig.getUrl() + "/api/EIB.ATP.advcommon/t0/generateTokenBySAI";

            //发送POST请求，保留原有的HTTP客户端逻辑
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

            JSONObject rtnJson = new JSONObject(response.body().string());

            //对返回报文解析：接口返回状态
//            JSONObject head = rtnJson.getJSONObject("head");
//            if (head == null || !"000000000".equals(head.optString("rtnCode"))) {
//                // 接口返回失败，记录错误信息
//                String errorMsg = head == null ? "接口无返回头信息" : head.optString("rtnMsg", "未知错误");
//                System.out.println("获取token失败，接口返回错误：" + errorMsg);
//                return "";
//            }

            Console.log ("rtnJson=>"+rtnJson);
            //读取token字段
            String token = rtnJson.optString("token");
            if (token == null || token.isEmpty()) {
                Console.log("获取token失败，接口返回token为空");
                return null;
            }
            return token;
        }catch (Exception e){
            Console.log ("获取token失败,验证异常:"+e.getMessage());
            e.printStackTrace();
            return "";
        }
    }

    public static void main(String[] args) {
//        TreasConfig.properties.init();
        GetTreasTokenV2 getTreasToken = new GetTreasTokenV2();
        String token = getTreasToken.getToken("CSJTZ051");
        System.out.println("token = " + token);
    }
}
