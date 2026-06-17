package rundo.util;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONUtil;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import weaver.conn.RecordSet;
import weaver.general.Util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description: 流程干预与删除公共工具类
 *               提供流程干预（退回指定节点）、表单数据删除、认证请求构建等公共方法
 * @Author: 张骏山
 * @Date: 2026/6/17
 * @PackageName: rundo.util
 * @ClassName: ApiUtil
 * @Version: 1.0
 */
public class ApiUtil {

    private static volatile ApiUtil instance;

    private final String appId;
    private final String baseUrl;



    /**
     * 私有构造函数，从 QuoteConfig 配置中加载 appId 和 baseUrl
     */
    private ApiUtil() {
        this.appId = getPropsWithDefault("appId", "rundo");
        this.baseUrl = getPropsWithDefault("baseUrl", "http://127.0.0.1:8081");
    }

    /**
     * 获取 ApiUtil 单例实例
     *
     * @return ApiUtil 单例
     */
    public static ApiUtil getInstance() {
        if (instance == null) {
            synchronized (ApiUtil.class) {
                if (instance == null) {
                    instance = new ApiUtil();
                }
            }
        }
        return instance;
    }

    /**
     * 获取带认证 Token 的 HTTP 请求构建器
     * <p>
     * 从 ecology_biz_ec 表获取 appId 对应的密钥对，
     * 若没有对应记录 则从 /api/ec/dev/auth/regist 注册新应用，
     * 通过 /api/ec/dev/auth/applytoken 获取认证 token，
     * 并将 token 和加密后的 userid 附加到请求头中。
     * </p>
     *
     * @param apiUrl   接口地址
     * @param operator 操作人 UID
     * @return 带认证头的 Request.Builder
     */
    public Request.Builder getRequest(String apiUrl, int operator) {
        RecordSet rs = new RecordSet();
        String sql = "select * from ecology_biz_ec where appid='" + appId + "'";
        rs.executeQuery(sql);
        String spk = "";
        String secret = "";
        if (rs.next()) {
            spk = rs.getString("server_public_key");
            secret = rs.getString("secrit");
        }
        if (spk.isEmpty() || secret.isEmpty()) {
            RSA rsa = new RSA();
            String publicKey = rsa.getPublicKeyBase64();
            rsa.getPrivateKeyBase64();

            String requestData = HttpRequest.post(baseUrl + "/api/ec/dev/auth/regist")
                    .header("appid", appId).header("cpk", publicKey)
                    .timeout(2000).execute().body();
            Console.log("注册接口信息：" + requestData);

            rs.execute(sql);
            if (rs.next()) {
                spk = rs.getString("server_public_key");
                secret = rs.getString("secrit");
            }
        }
        RSA rsa = new RSA(null, spk);
        String encryptSecret = rsa.encryptBase64(secret, CharsetUtil.CHARSET_UTF_8, KeyType.PublicKey);
        String requestData = HttpRequest.post(baseUrl + "/api/ec/dev/auth/applytoken")
                .header("appid", appId).header("secret", encryptSecret)
                .header("time", "3600").execute().body();
        Console.log("申请token接口信息：" + requestData);
        String token = (String) JSONUtil.parseObj(requestData).get("token");
        String encryptUserid = rsa.encryptBase64(String.valueOf(operator),
                CharsetUtil.CHARSET_UTF_8, KeyType.PublicKey);

        return new Request.Builder()
                .url(apiUrl)
                .addHeader("Content-Type", "application/json")
                .addHeader("appid", appId)
                .addHeader("token", token)
                .addHeader("userid", encryptUserid);
    }

    /**
     * 干预流程回到指定节点
     * <p>
     * 调用 /api/workflow/paService/doIntervenor 接口，
     * 将指定流程退回到目标节点，并指定干预人。
     * </p>
     *
     * @param requestId 要干预的流程 requestId
     * @param receivers      BDS 分发人员（干预人）
     * @param aimNodeId 目标节点 ID
     * @param uid       操作人 UID
     * @param remark    干预备注
     * @return true 干预成功, false 干预失败
     */
    public boolean interventionRequest(int requestId, String receivers, int aimNodeId, int uid,
                                               String remark) {
        try {
            Console.log("干预流程回到指定节点, requestId = " + requestId
                    + ", BDSs = " + receivers + ", aimNodeId = " + aimNodeId);

            JSONObject params = new JSONObject();
            params.put("requestId", requestId);
            params.put("submitNodeId", aimNodeId);
            params.put("Intervenorid", receivers);
            params.put("enableIntervenor", true);
            params.put("SignType", 0);
            params.put("remark", remark);
            Console.log("干预流程, params = " + params);

            JSONObject rtnJson = exceuteRequest("/api/workflow/paService/doIntervenor", params, uid);
            Console.log("干预流程结果, rtnJson = " + rtnJson);
            return rtnJson.getString("code").equals("SUCCESS");
        } catch (Exception e) {
            Console.log("干预流程异常: " + e.getMessage());
            return false;
        }
    }

    /**
     * 删除表单数据
     * <p>
     * 调用 /api/cube/expand/deleteData 接口删除指定 billId 对应的表单数据。
     * </p>
     *
     * @param billId   数据 ID
     * @param modeId   模块 ID (workflowId)
     * @param uid      操作人 UID
     * @param customId 表单对应的 customId
     * @return true 删除成功, false 删除失败
     */
    public boolean deleteData(int billId, int modeId, int uid, int customId) {
        try {
            Console.log("删除表单数据, billId = " + billId + ", modeId = " + modeId);

            String apiUrl = baseUrl + "/api/cube/expand/deleteData?"
                    + "billids=" + billId
                    + "&customid=" + customId
                    + "&modeId=" + modeId
                    + "&type=0"
                    + "&viewtype=0";
            Console.log("删除数据, apiUrl = " + apiUrl);
            JSONObject rtnJson = exceuteRequest("/api/cube/expand/deleteData", null, uid);
            Console.log("删除数据结果, rtnJson = " + rtnJson);
            return rtnJson.getString("message").contains("删除成功");
        } catch (Exception e) {
            Console.log("删除数据异常: " + e.getMessage());
            return false;
        }
    }

    /**
     * 删除流程
     * <p>
     * 调用 /api/workflow/paService/deleteRequest 接口删除指定流程。
     * </p>
     *
     * @param requestId   要删除的流程请求 ID
     * @param uid         操作人 UID
     * @param otherParams 其他参数（可选），如 {"ismonitor":"1"} 表示以监控权限删除
     * @return true 删除成功, false 删除失败
     */
    public boolean deleteRequest(String requestId, int uid, String otherParams) {
        try {
            Console.log("删除流程, requestId = " + requestId);

            JSONObject params = new JSONObject();
            params.put("requestId", requestId);
            if (otherParams != null && !otherParams.isEmpty()) {
                params.put("otherParams", otherParams);
            }
            Console.log("删除流程, params = " + params);

            JSONObject rtnJson = exceuteRequest("/api/workflow/paService/deleteRequest", params, uid);
            Console.log("删除流程结果, rtnJson = " + rtnJson);
            return rtnJson.getString("code").equals("SUCCESS");
        } catch (Exception e) {
            Console.log("删除流程异常: " + e.getMessage());
            return false;
        }
    }

    @NotNull
    private JSONObject exceuteRequest(String path, JSONObject params, int uid) throws IOException, JSONException {
        String apiUrl = baseUrl + path;
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        MediaType mediaType = MediaType.parse("application/json");
        if (params == null )
            params = new JSONObject();
        RequestBody body = RequestBody.create(mediaType, params.toString());
        Request request = getRequest(apiUrl, uid).post(body).build();
        Response response = client.newCall(request).execute();
        return new JSONObject(response.body().string());
    }



    /**
     * 删除流程 固定以管理员权限删除
     * @param requestId     流程请求ID
     * @return true 删除成功，false 删除失败
     */
    public boolean deleteRequest(String requestId) {
        return deleteRequest(requestId, 1, "{\"ismonitor\":\"1\"}");
    }

    /**
     * 返回错误信息并记录日志
     *
     * @param errorMessage 错误信息（同时作为日志信息）
     * @return 包含 errmsg 和 flag 的结果 Map
     */
    public static Map<String, String> rtnError(String errorMessage) {
        return rtnError(errorMessage, errorMessage);
    }

    /**
     * 返回错误信息并记录日志
     *
     * @param errorMessage 返回给前端的错误信息
     * @param logMessage   记录到日志的详细信息
     * @return 包含 errmsg 和 flag 的结果 Map
     */
    public static Map<String, String> rtnError(String errorMessage, String logMessage) {
        Console.log(logMessage);
        Map<String, String> result = new HashMap<>();
        result.put("errmsg", errorMessage);
        result.put("flag", "false");
        return result;
    }

    /**
     * 获取配置属性值，若为空则返回默认值
     * <p>
     * 从 QuoteConfig 配置中读取指定属性，如果配置值为空则返回传入的默认值。
     * </p>
     *
     * @param propName 属性名
     * @param defaults 默认值
     * @return 配置值或默认值
     */
    public static String getPropsWithDefault(String propName, String defaults) {
        RecordSet rs = new RecordSet();
        String value = rs.getPropValue("QuoteConfig", propName);
        if (Util.null2String(value).isEmpty()) {
            value = defaults;
        }
        return value;
    }
}