package ISS.loginE10;

import okhttp3.*;
import org.json.JSONObject;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import ISS.util.Console;

/**
 * @Author: 张骏山
 * @Date: 2025/5/9 17:12
 * @PackageName: ISS.loginE10
 * @ClassName: GetE10Token
 * @Description: 单点登录E10
 * @Version: 1.0
 */
public class GetE10Token {
    String app_key = "7d325e52b768ae679a1975c40a1df434";
    String app_security = "8cc08a3cf225e3bb87c9e105d66b011";
    String authType = "loginID";

    String url = "https://oasandbox.oneiss.cn";

    public GetE10Token(String app_key, String app_security, String authType, String url) {
        this.app_key = app_key;
        this.app_security = app_security;
        this.authType = authType;
        this.url = url;
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
    }

    public GetE10Token() {

    }

    public String getToken(String loginID) {
        try {
            Console.log ("loginID=>"+loginID);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("app_key", app_key);
            jsonObject.put("app_security", app_security);
            jsonObject.put("authType", authType);
            jsonObject.put("account", loginID);
            Console.log ("jsonObject=>"+jsonObject.toString());
            String apiUrl = url + "/papi/openapi/oauth2/get_logintoken";
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
            Console.log ("rtnJson=>"+rtnJson);
            String token = rtnJson.getString("etLoginToken");
            Console.log ("token=>"+token);
            return token;
        }catch (Exception e){
            Console.log ("获取token失败,验证异常:"+e.getMessage());
            return "";
        }
    }
    public static void main(String[] args) {
        GetE10Token getE10Token = new GetE10Token();
        String token = getE10Token.getToken("haochuan.xu");
        Console.log ("token = " + token);
    }
}
