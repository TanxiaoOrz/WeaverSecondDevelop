package csj.E10;

import csj.utils.Console;
import okhttp3.*;
import org.json.JSONException;
import org.json.JSONObject;
import weaver.general.Util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URLEncoder;
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

    /**
     * 作废当前缓存的 access_token，使下一次 {@link #getAccessToken()} 重新签发。
     * <p>
     * 用途：服务端返回 {@code errcode=200007「无效的access_token」} 时，
     * 可能是改了 app_key/corpid 或换了环境但进程未重启、仍用旧缓存。
     * 调用本方法后再取一次，即可排除「缓存导致」的假象。
     * 注意只清 token，不动 config（config 变更需重新 {@code E10Config.init()}）。
     */
    public void invalidateToken() {
        this.access_token = null;
        this.refresh_token = null;
        this.occurTime = null;
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

    // ===== WebHook 通用调用方法 =====

    /**
     * 通用ESB-WebHook调用方法。
     * <p>
     * ★ 会自动把 {@code access_token} 作为 <b>query 参数</b> 拼到 URL 上。
     * 原因：ESB 侧的 webhook 校验读的是 query（实测缺 token 时返回
     * {@code {"errcode":"200008","errmsg":"access_token没传"}}），
     * 只把它放进请求体不会生效。
     * <p>
     * 兼容性处理：
     * <ul>
     *   <li>若 {@code path} 里已经带了 {@code access_token=}，不再重复拼接；</li>
     *   <li>请求体里原有的 {@code access_token} 字段保留不动，不影响仍在用
     *       旧写法的调用方（{@code MeetingToProjectAction} /
     *       {@code QueryTodoContractCount} 等）。</li>
     * </ul>
     *
     * @param object 请求体JSON对象(调用方自行构建业务参数)
     * @param path   URL路径,相对于E10_URL,例如 "/papi/openapi/api/open-esb/server/webhook/trigger/xxx"
     * @return 响应JSONObject,异常时返回null
     */
    public JSONObject callWebhook(JSONObject object, String path) {
        try {
            String apiUrl = appendAccessToken(this.config.getE10Url() + path);

            OkHttpClient client = new OkHttpClient().newBuilder().build();
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, object.toString());
            Request request = new Request.Builder()
                    .url(apiUrl)
                    .method("POST", body)
                    .addHeader("Content-Type", "application/json")
                    .build();
            Response response = client.newCall(request).execute();
            JSONObject rtnJson = new JSONObject(response.body().string());
            Console.log("callWebhook成功: path=" + path + ", response=" + rtnJson.toString());
            return rtnJson;
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw, true));
            Console.log("callWebhook异常: path=" + path + ", body=" + object.toString()
                    + ", error=" + e.getMessage() + "\n" + sw.toString());
            return null;
        }
    }

    /**
     * 给 URL 补上 {@code access_token} query 参数。
     * <p>
     * 已有 {@code access_token=} 时原样返回，避免重复拼接；
     * 取不到 token 时也只返回原 URL（让服务端如实报缺 token，便于排查）。
     */
    private String appendAccessToken(String url) {
        try {
            if (url == null) {
                return "";
            }
            if (url.contains("access_token=")) {
                return url;
            }
            String token = getAccessToken();
            if (token == null || token.trim().isEmpty()) {
                return url;
            }
            String sep = url.contains("?") ? "&" : "?";
            return url + sep + "access_token=" + URLEncoder.encode(token, "UTF-8");
        } catch (Exception e) {
            Console.log("拼接access_token失败, url=" + url
                    + ", error=" + e.getClass().getName() + ": " + e.getMessage());
            return url;
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