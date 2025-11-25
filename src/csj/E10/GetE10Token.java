package csj.E10;

import csj.utils.Console;
import okhttp3.*;
import org.json.JSONObject;

/**
 * @Author: 张骏山
 * @Date: 2025/11/25 11:46
 * @PackageName: csj.E10
 * @ClassName: GetE10Token
 * @Description: TODO
 * @Version: 1.0
 */
public class GetE10Token {

    public String getToken(String loginID) {
        try {
//            Console.log ("loginID=>"+loginID);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("app_key", E10Config.getE10AppKey());
            jsonObject.put("app_security", E10Config.getE10AppSecurity());
            jsonObject.put("authType", E10Config.getAuthType());
            jsonObject.put("account", loginID);
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
        GetE10Token getE10Token = new GetE10Token();
        String token = getE10Token.getToken("xingwb");
        System.out.println("token = " + token);
    }
}
