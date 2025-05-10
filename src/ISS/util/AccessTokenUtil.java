package ISS.util;

import com.icbc.api.internal.apache.http.E;
import okhttp3.*;
import org.json.JSONException;
import org.json.JSONObject;
import weaver.conn.StringUtil;
import weaver.general.Util;

import java.util.Date;

/**
 * @Author: 张骏山
 * @Date: 2025/5/10 16:20
 * @PackageName: ISS.util
 * @ClassName: AccessTokenUtil
 * @Description: 获取e10的token工具类
 * @Version: 1.0
 */
public class AccessTokenUtil {

    private String app_key = ConfigUtil.getE10AppKey();
    private String app_secret = ConfigUtil.getE10AppSecurity();

    private String access_token;
    private String refresh_token;

    private Date occurTime;

    private long expireTime = 1000 * 60 * 60 * 2;

    private static AccessTokenUtil accessTokenUtil;

    public synchronized static AccessTokenUtil getInstance() {
        if (accessTokenUtil == null) {
            accessTokenUtil = new AccessTokenUtil();
        }
        return accessTokenUtil;
    }

    public String getAccessToken() {
        if (isExpired()) {
            try {
                Date occurTime = new Date();
                JSONObject param = new JSONObject();
                param.put("app_key",app_key);
                param.put("app_secret",app_secret);
                param.put("grant_type","authorization_code");
                param.put("code", CodeUtil.getInstance().getCode());
                OkHttpClient client = new OkHttpClient().newBuilder()
                        .build();
                MediaType mediaType = MediaType.parse("application/json");
                RequestBody body = RequestBody.create(mediaType, param.toString());
                Request request = new Request.Builder()
                        .url(ConfigUtil.getE10Url() + "/papi/openapi/oauth2/access_token")
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

    public boolean refreshToken(){
        try {
            Date occurTime = new Date();
            JSONObject param = new JSONObject();
            param.put("refresh_token",refresh_token);
            param.put("grant_type","refresh_token");
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, param.toString());
            Request request = new Request.Builder()
                    .url(ConfigUtil.getE10Url() + "/papi/openapi/oauth2/refresh_token")
                    .method("POST", body)
                    .addHeader("Content-Type", "application/json")
                    .build();
            Response response = client.newCall(request).execute();
            JSONObject rtnJson = new JSONObject(response.body().string());
            if (rtnJson.getString("errmsg").equals("success")){
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

    private boolean isExpired(){
        if (occurTime == null){
            return true;
        }
        if (Util.null2String(access_token).equals("")) {
            return true;
        }
        long diff = new Date().getTime() - occurTime.getTime();
        if (diff < expireTime)
            return false;
        else if (Util.null2String(refresh_token).equals("")) {
            return refreshToken();
        } else {
            return false;
        }
    }

    public static void main(String[] args) {
        String accessToken = new AccessTokenUtil().getAccessToken();
        System.out.println("accessToken = " + accessToken);
    }
}
