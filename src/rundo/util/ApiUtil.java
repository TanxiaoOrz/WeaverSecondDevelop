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

/**
 * @Description: 流程干预与删除公共工具类
 * 提供流程干预（退回指定节点）、表单数据删除、认证请求构建等公共方法
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
    private final String defaultOperator;


    /**
     * 私有构造函数，从 QuoteConfig 配置中加载 appId 和 baseUrl
     */
    private ApiUtil() {
        this.appId = getPropsWithDefault("appId", "rundo");
        this.baseUrl = getPropsWithDefault("baseUrl", "http://127.0.0.1:8081");
        this.defaultOperator = getPropsWithDefault("defaultOperator", "2103");
    }

    /**
     * 私有构造函数，从 QuoteConfig 配置中加载 appId 和 baseUrl
     */
    private ApiUtil(String appId, String baseUrl, String defaultOperator) {
        this.appId = appId;
        this.baseUrl = baseUrl;
        this.defaultOperator = defaultOperator;
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
     * @return 带认证头的 Request.Builder
     */
    public Request.Builder getRequest(String apiUrl) {
        RecordSet rs = new RecordSet();
        String sql = "select * from ecology_biz_ec where appid='" + appId + "'";
        Console.log("sql => "+sql);
        rs.executeQuery(sql);
        String spk = "";
        String secret = "";
        if (rs.next()) {
            spk = rs.getString("server_public_key");
            secret = rs.getString("SECRIT");
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
        Console.log("spk=>"+spk);
        Console.log("secret=>"+secret);
        RSA rsa = new RSA(null, spk);
        String encryptSecret = rsa.encryptBase64(secret, CharsetUtil.CHARSET_UTF_8, KeyType.PublicKey);
        String requestData = HttpRequest.post(baseUrl + "/api/ec/dev/auth/applytoken")
                .header("appid", appId).header("secret", encryptSecret)
                .header("time", "3600").execute().body();
        Console.log("申请token接口信息：" + requestData);
        String token = (String) JSONUtil.parseObj(requestData).get("token");
        String encryptUserid = rsa.encryptBase64(String.valueOf(defaultOperator),
                CharsetUtil.CHARSET_UTF_8, KeyType.PublicKey);
        Console.log("encryptUserid = " + encryptUserid);
        Console.log("token = " + token);
        Console.log("appId = " + appId);

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
     * @param receivers BDS 分发人员（干预人）
     * @param aimNodeId 目标节点 ID
     * @param remark    干预备注
     * @return true 干预成功, false 干预失败
     */
    public boolean interventionRequest(int requestId, String receivers, int aimNodeId,
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

            JSONObject rtnJson = executeRequest("/api/workflow/paService/doIntervenor", params);
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
     * @param customId 表单对应的 customId
     * @return true 删除成功, false 删除失败
     */
    public boolean deleteData(int billId, int modeId, int customId) {
        try {
            Console.log("删除表单数据, billId = " + billId + ", modeId = " + modeId);

            String apiUrl = "/api/cube/expand/deleteData?"
                    + "billids=" + billId
                    + "&customid=" + customId
                    + "&modeId=" + modeId
                    + "&type=0"
                    + "&viewtype=0";
            Console.log("删除数据, apiUrl = " + apiUrl);
            JSONObject rtnJson = executeRequest(apiUrl);
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
     * @param otherParams 其他参数（可选），如 {"ismonitor":"1"} 表示以监控权限删除
     * @return true 删除成功, false 删除失败
     */
    public boolean deleteRequest(int requestId, String otherParams) {
        try {
            Console.log("删除流程, requestId = " + requestId);

            String apiUrl = "/api/workflow/paService/deleteRequest?requestId=" + requestId;
            if (otherParams != null && !otherParams.isEmpty())
                apiUrl += ("&otherParams=" + otherParams);
            Console.log("删除流程, apiUrl = " + apiUrl);
            JSONObject rtnJson = executeRequest(apiUrl, null, "application/x-www-form-urlencoded");
            Console.log("删除流程结果, rtnJson = " + rtnJson);
            return rtnJson.getString("code").equals("SUCCESS");
        } catch (Exception e) {
            Console.log("删除流程异常: " + e.getMessage());
            return false;
        }
    }

    @NotNull
    private JSONObject executeRequest(String path, JSONObject params) throws IOException, JSONException {
        return executeRequest(path, params, "application/json");

    }

    @NotNull
    private JSONObject executeRequest(String path) throws IOException, JSONException {
        String apiUrl = baseUrl + path;
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        Request request = getRequest(apiUrl).get().build();
        Console.log(request.toString());
        Response response = client.newCall(request).execute();
        String returnString = response.body().string();
        Console.log(returnString);
        return new JSONObject(returnString);

    }

    @NotNull
    private JSONObject executeRequest(String path, JSONObject params, String bodyType) throws IOException, JSONException {
        String apiUrl = baseUrl + path;
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        MediaType mediaType = MediaType.parse(bodyType);
        if (params == null)
            params = new JSONObject();
        RequestBody body = RequestBody.create(mediaType, params.toString());
        Request request = getRequest(apiUrl).post(body).build();
        Console.log(request.toString());
        Response response = client.newCall(request).execute();
        String returnString = response.body().string();
        Console.log(returnString);
        return new JSONObject(returnString);
    }


    /**
     * 删除流程 固定以管理员权限删除
     *
     * @param requestId 流程请求ID
     * @return true 删除成功，false 删除失败
     */
    public boolean deleteRequest(int requestId) {
        return deleteRequest(requestId,  "{\"ismonitor\":\"1\"}");
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

    /**
     * 干预流程测试入口（纯静态代码，不依赖实例，调用的方法实现已抄入本方法内部）
     */
    public static void main(String[] args) {
        // ===================== 1. 配置（对应 QuoteConfig.properties） =====================
        String appId = "rundo";
        String baseUrl = "https://oa.rundo-cro.com:8443";
        String defaultOperator = "2103";

        // ===================== 2. 需要查询出来的参数（待填写，不使用 RecordSet） =====================
        // 来自 ecology_biz_ec 表: select server_public_key, secrit from ecology_biz_ec where appid='rundo'
        String spk = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA0CP1UQ9zpryA88YcHkDYrYnf0XcyGCs71vtCMe0dTHDF/0ocxkhLGmRxhoeKWXtiu2G+eoWxmHnLdHcvVa6/1WWkJcOqVMHzRJtQuXsDbXPZ8RRaP78FU1FFSjcVSwjzF1oIi94EeyAgXcrseyeL1NJ6AhqnEa+SbOo+NMbyAPiBnauzg7zXfjQmdwXIAwFrj5Hnj8PKG8eYSZzjHi0xP5nS3azTch97Q3TSGZ1nnabgq/OQ705WNDuGYJqyhwdH9ayFU1l9A6J4d+4gFe00VB2bhnfjdtNaMrt+R2c/DMnNZOGFoM+YwXuoKT+XNKxsMWU0qMIB75tF6Id+cHqhaQIDAQAB";
        String secret = "799fd7d3-9e4b-4eec-9c39-b944fae6dcc7";

        // ===================== 3. 干预流程入参（待填写） =====================
        int requestId = 1301372;    // TODO 待填写: 要干预的流程 requestId
        String receivers = "2103";      // TODO 待填写: BDS 分发人员（干预人）
        int aimNodeId = 2801;          // TODO 待填写: 目标节点 ID
        String remark = "测试干预";         // TODO 待填写: 干预备注

        try {
            /* ----------------- getRequest 实现（去掉取密钥部分，spk/secret 由上方入参提供） ----------------- */
            System.out.println("spk=>" + spk);
            System.out.println("secret=>" + secret);
            RSA rsa = new RSA(null, spk);
            String encryptSecret = rsa.encryptBase64(secret, CharsetUtil.CHARSET_UTF_8, KeyType.PublicKey);
            String tokenData = HttpRequest.post(baseUrl + "/api/ec/dev/auth/applytoken")
                    .header("appid", appId).header("secret", encryptSecret)
                    .header("time", "3600").execute().body();
            System.out.println("申请token接口信息：" + tokenData);
            String token = (String) JSONUtil.parseObj(tokenData).get("token");
            String encryptUserid = rsa.encryptBase64(String.valueOf(defaultOperator),
                    CharsetUtil.CHARSET_UTF_8, KeyType.PublicKey);
            System.out.println("encryptUserid = " + encryptUserid);
            System.out.println("token = " + token);
            System.out.println("appId = " + appId);

            /* ------------- interventionRequest 实现（干预流程回到指定节点） ------------- */
            JSONObject params = new JSONObject();
            params.put("requestId", requestId);
            params.put("submitNodeId", aimNodeId);
            params.put("Intervenorid", receivers);
            params.put("enableIntervenor", true);
            params.put("SignType", 0);
            params.put("remark", remark);
            System.out.println("干预流程, params = " + params);

            /* ----------------- executeRequest 实现（POST application/json） ----------------- */
            String apiUrl = baseUrl + "/api/workflow/paService/doIntervenor";
            OkHttpClient client = new OkHttpClient().newBuilder().build();
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, params.toString());
            Request request = new Request.Builder()
                    .url(apiUrl)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("appid", appId)
                    .addHeader("token", token)
                    .addHeader("userid", encryptUserid)
                    .post(body).build();
            System.out.println(request.toString());
            Response response = client.newCall(request).execute();
            String returnString = response.body().string();
            System.out.println(returnString);
            JSONObject rtnJson = new JSONObject(returnString);

            System.out.println("干预流程结果, rtnJson = " + rtnJson);
            System.out.println("干预流程是否成功 = " + rtnJson.getString("code").equals("SUCCESS"));
        } catch (Exception e) {
            System.out.println("干预流程异常: " + e.getMessage());
            e.printStackTrace();
        }
    }
}