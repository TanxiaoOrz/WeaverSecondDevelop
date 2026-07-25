package csj.E10;

import csj.utils.Console;
import okhttp3.*;
import org.json.JSONException;
import org.json.JSONObject;
import weaver.general.Util;

import java.util.Date;
import java.util.Map;
import java.util.HashMap;

/**
 * @Author: 张骏山
 * @Date: 2025/5/10 16:20
 * @PackageName: csj.E10
 * @ClassName: E10Utils
 * @Description: E10工具类,合并AccessTokenUtil和CodeUtil, Map模式按code维护实例
 * @Version: 1.0
 */
public class E10Utils {

    // Map模式 按code维护不同实例
    private static Map<String, E10Utils> instanceMap = new HashMap<>();

    private String code;

    // 携带私有E10Config对象,code逻辑一致
    private E10Config config;

    // ===== AccessTokenUtil 字段 =====
    private String access_token;
    private String refresh_token;
    private Date occurTime;
    private long expireTime = 1000 * 60 * 60 * 2;

    // ===== 构造函数 =====

    private E10Utils(String code) {
        this.code = (code != null) ? code : "";
        this.config = E10Config.getInstance(this.code);
    }

    // ===== Map模式实例获取 =====

    public static E10Utils getInstance(String code) {
        if (code == null) {
            code = "";
        }
        if (!instanceMap.containsKey(code)) {
            instanceMap.put(code, new E10Utils(code));
        }
        return instanceMap.get(code);
    }

    // 无参获取兼容为空字符串
    public static E10Utils getInstance() {
        return getInstance("");
    }

    // ===== AccessTokenUtil 方法 =====

    public String getAccessToken() {
        if (isExpired()) {
            try {
                Date occurTime = new Date();
                JSONObject param = new JSONObject();
                param.put("app_key", this.config.getE10AppKey());
                param.put("app_secret", this.config.getE10AppSecurity());
                param.put("grant_type", "authorization_code");
                param.put("code", getCode());
                OkHttpClient client = new OkHttpClient().newBuilder()
                        .build();
                MediaType mediaType = MediaType.parse("application/json");
                RequestBody body = RequestBody.create(mediaType, param.toString());
                Request request = new Request.Builder()
                        .url(this.config.getE10Url() + "/papi/openapi/oauth2/access_token")
                        .method("POST", body)
                        .addHeader("Content-Type", "application/json")
                        .build();
                Response response = client.newCall(request).execute();
                JSONObject rtnJson = new JSONObject(response.body().string());
                saveToken(occurTime, rtnJson);
            } catch (Exception e) {
                e.printStackTrace();
                Console.log(e.getMessage());
                return "";
            }
        }
        return access_token;
    }

    private void saveToken(Date occurTime, JSONObject rtnJson) throws JSONException {
        access_token = rtnJson.getString("accessToken");
        refresh_token = rtnJson.getString("refreshToken");
        expireTime = rtnJson.getLong("expires_in");
        this.occurTime = occurTime;
    }

    public boolean refreshToken() {
        try {
            Date occurTime = new Date();
            JSONObject param = new JSONObject();
            param.put("refresh_token", refresh_token);
            param.put("grant_type", "refresh_token");
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, param.toString());
            Request request = new Request.Builder()
                    .url(this.config.getE10Url() + "/papi/openapi/oauth2/refresh_token")
                    .method("POST", body)
                    .addHeader("Content-Type", "application/json")
                    .build();
            Response response = client.newCall(request).execute();
            JSONObject rtnJson = new JSONObject(response.body().string());
            if (rtnJson.getString("errmsg").equals("success")) {
                saveToken(occurTime, rtnJson);
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            Console.log(e.getMessage());
            return false;
        }
    }

    private boolean isExpired() {
        if (occurTime == null) {
            return true;
        }
        if (Util.null2String(access_token).equals("")) {
            return true;
        }
        long diff = new Date().getTime() - occurTime.getTime();
        if (diff < expireTime) {
            return false;
        } else if (Util.null2String(refresh_token).equals("")) {
            return refreshToken();
        } else {
            return false;
        }
    }

    // ===== CodeUtil 方法 =====

    public String getCode() {
        try {
            JSONObject param = new JSONObject();
            param.put("corpid", this.config.getE10CorpId());
            param.put("response_type", "code");
            param.put("state", "");
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, param.toString());
            Request request = new Request.Builder()
                    .url(this.config.getE10Url() + "/papi/openapi/oauth2/authorize")
                    .method("POST", body)
                    .addHeader("Content-Type", "application/json")
                    .build();
            Response response = client.newCall(request).execute();
            JSONObject rtnJson = new JSONObject(response.body().string());
            return rtnJson.getString("code");
        } catch (Exception e) {
            e.printStackTrace();
            Console.log(e.getMessage());
            return "";
        }
    }

    // ===== main 测试 =====

    public static void main(String[] args) {
        E10Utils utils = E10Utils.getInstance();
        String accessToken = utils.getAccessToken();
        System.out.println("accessToken = " + accessToken);
        String code = utils.getCode();
        System.out.println("code = " + code);
    }

}