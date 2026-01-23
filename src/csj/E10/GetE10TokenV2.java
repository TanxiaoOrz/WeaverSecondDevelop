package csj.E10;

import okhttp3.*;
import org.json.JSONObject;

/**
 * @Author: 张骏山
 * @Date: 2025/11/25 11:46
 * @PackageName: csj.E10
 * @ClassName: GetE10TokenV2
 * @Description: 改造成workcode传入形式
 * @Version: 1.0
 */
public class GetE10TokenV2 {

    /**
     * 获取e10 token 工号传入形式
     * @param workCode 工号
     * @return token
     */
    public String getToken(String workCode) {
        try {
//            Console.log ("workCode=>"+workCode);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("app_key", E10Config.getE10AppKey());
            jsonObject.put("app_security", E10Config.getE10AppSecurity());
            jsonObject.put("authType", "JOB_NUM");
            jsonObject.put("account", workCode);
//            Console.log ("jsonObject=>"+jsonObject.toString());
            String apiUrl = E10Config.getE10Url() + "/papi/openapi/oauth2/get_logintoken";
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, jsonObject.toString());
            Request request = new Request.Builder()
                    .url(apiUrl)
                    .method("POST", body)
                    .addHeader("Content-Type", "application/json")
                    .build();
            Response response = client.newCall(request).execute();
            JSONObject rtnJson = new JSONObject(response.body().string());
//            Console.log ("rtnJson=>"+rtnJson);
            String token = rtnJson.getString("etLoginToken");
//            Console.log ("token=>"+token);
            return token;
        }catch (Exception e){
//            Console.log ("获取token失败,验证异常:"+e.getMessage());
            return "";
        }
    }

    public static void main(String[] args) {
//        E10Config.properties.init();
        GetE10TokenV2 getE10Token = new GetE10TokenV2();
        String token = getE10Token.getToken("xingwb");
        System.out.println("token = " + token);
    }
}
