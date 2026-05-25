package rundo.quote;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONUtil;
import okhttp3.*;
import org.json.JSONException;
import org.json.JSONObject;
import rundo.util.Console;
import weaver.conn.RecordSet;
import weaver.formmode.customjavacode.AbstractModeExpandJavaCodeNew;
import weaver.general.Util;
import weaver.hrm.User;
import weaver.soa.workflow.request.RequestInfo;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: 张骏山
 * @Date: 2026/5/22 14:08
 * @PackageName: rundo.quote
 * @ClassName: RestartQuoteExpend
 * @Description: 报价重启按钮页面拓展
 * @Version: 1.0
 */

public class RestartQuoteExpendV6 extends AbstractModeExpandJavaCodeNew {

    /**
     * 部署前执行内容说明
     * 1. 注册appid : INSERT INTO ECOLOGY_BIZ_EC(ID,APPID,NAME) VALUES('1','rundo','润东医疗Ecology');
     * 2. 生成公钥和私钥对 /api/ec/dev/auth/regist {header:{"appid":"rundo", "cpk":"${RSA.getPublicKeyBase64()}"}}
     */

    private String appId = "rundo";

    private String baseUrl = "http://127.0.0.1:8081";
    private String formName;
    private String BDSColumnName;
    private String aimNodeColumn;
    private String requestIdColumn;
    private String interventionRemark = "退回重启";
    private int customId;

    public RestartQuoteExpendV6() {
        super();
        baseUrl = getPropsWithDefault("baseUrl", baseUrl);
        formName = getPropsWithDefault("formName", formName);
        BDSColumnName = getPropsWithDefault("BDSColumnName", BDSColumnName);
        aimNodeColumn = getPropsWithDefault("aimNodeColumn", aimNodeColumn);
        requestIdColumn = getPropsWithDefault("requestIdColumn", requestIdColumn);
        customId = Util.getIntValue(getPropsWithDefault("customId", String.valueOf(customId)));
        appId = getPropsWithDefault("appId", appId);
    }



    /**
     * 删除本行数据, 干预流程回到BDS分发节点
     *
     * @param map param包含(但不限于)以下数据
     *            user 当前用户
     *            importtype 导入方式(仅在批量导入的接口动作会传输) 1 追加，2覆盖,3更新，获取方式(int)param.get("importtype")
     *            导入链接中拼接的特殊参数(仅在批量导入的接口动作会传输)，比如a=1，可通过param.get("a")获取参数值
     *            页面链接拼接的参数，比如b=2,可以通过param.get("b")来获取参数
     * @return 执行结果
     */
    @Override
    public Map<String, String> doModeExpand(Map<String, Object> map) {
        try {
            int uid = -1;
            int billid = -1;//数据id
            int modeid = -1;//模块id
            String BDSs = "";
            int aimNodeId = -1;
            int requestId = -1;
            User user = (User) map.get("user");
            uid = user.getUID();
            RequestInfo requestInfo = (RequestInfo) map.get("RequestInfo");
            if (requestInfo == null) {
                return rtnError("RequestInfo 为空, 请联系管理员");
            }
            billid = Util.getIntValue(requestInfo.getRequestid());
            modeid = Util.getIntValue(requestInfo.getWorkflowid());
            if (billid < 0 || modeid < 0 || uid < 0) {
                return rtnError("关键数据获取失败, 请联系管理员",
                        "关键数据获取失败, billid, modeid, uid 分别为: " + billid + ", " + modeid + ", " + uid);
            }
            RecordSet recordSet = new RecordSet();
            recordSet.executeQuery("select "+BDSColumnName+", "+aimNodeColumn+", "+requestIdColumn+" from "+formName+" where id = " + billid);
            Console.log("select "+BDSColumnName+", "+aimNodeColumn+", "+requestIdColumn+" from "+formName+" where id = " + billid);
            if (recordSet.next()) {
                BDSs = recordSet.getString(BDSColumnName);
                aimNodeId = recordSet.getInt(aimNodeColumn);
                requestId = recordSet.getInt(requestIdColumn);
            }
            if (BDSs.isEmpty()) {
                return rtnError("BDS为空, 请联系管理员");
            }
            if (aimNodeId < 0) {
                return rtnError("目标节点为空, 请联系管理员");
            }
            if (requestId < 0) {
                return rtnError("对应流程为空, 请联系管理员");
            }
            if (!interventionRequest(requestId, BDSs, aimNodeId, uid)) {
                return rtnError("干预流程回到BDS分发节点失败, 请联系管理员");
            }
            if (!deleteQuote(billid, modeid, uid)) {
                return rtnError("删除本条数据失败, 请联系管理员");
            }
        } catch (Exception e) {
            return rtnError("代码执行报错, 请联系管理员", "代码执行报错 => " + e.getMessage());
        }
        return new HashMap<String, String>();
    }

    /**
     * 干预流程回到BDS分发节点
     * @param requestId 流程id
     * @return 执行结果
     */
    private boolean interventionRequest(int requestId, String BDSs, int aimNodeId, int uid) throws Exception {
        Console.log("干预流程回到BDS分发节点, requestId = " + requestId + ", BDSs = " + BDSs + ", aimNodeId = " + aimNodeId);
        JSONObject params = new JSONObject();
        params.put("requestId", requestId);
        params.put("submitNodeId", aimNodeId);
        params.put("Intervenorid", BDSs);
        params.put("enableIntervenor", true);
        params.put("SignType", 0);
        params.put("remark", interventionRemark);
        Console.log("干预流程回到BDS分发节点, params = " + params.toString());
        String apiUrl = baseUrl + "/api/workflow/paService/doIntervenor";
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, params.toString());
        Request request = getRequest(apiUrl, uid).post(body).build();
        Response response = client.newCall(request).execute();
        JSONObject rtnJson = new JSONObject(response.body().string());
        Console.log("干预流程回到BDS分发节点, rtnJson = " + rtnJson.toString());
        return rtnJson.getString("code").equals("SUCCESS");
    }

    /**
     * 删除本行数据
     * @param billId 数据id
     * @param modeId 模块id
     * @param uid 操作人
     * @return 执行结果
     */
    private boolean deleteQuote(int billId,int modeId, int uid) throws JSONException, IOException {
        Console.log("删除本行数据, billId = " + billId + ", modeId = " + modeId);

        String apiUrl = baseUrl + "/api/cube/expand/deleteData?"
                + "billids=" + billId
                + "&customid=" + customId
                + "&modeId=" + modeId
                + "&type=0"
                + "&viewtype=0";
        Console.log("删除本行数据, apiUrl = " + apiUrl);
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("application/json");
        Request request = getRequest(apiUrl, uid).get().build();
        Response response = client.newCall(request).execute();
        JSONObject rtnJson = new JSONObject(response.body().string());
        Console.log("删除本条数据, rtnJson = " + rtnJson.toString());
        return rtnJson.getString("message").equals("本次共操作1条数据，1条删除成功。");
    }

    /**
     * 获取请求对象 统一增加认证token
     * @param apiUrl 接口地址
     * @param operator 操作人
     * @return 请求对象
     */
    private Request.Builder getRequest(String apiUrl, int operator) {
        RecordSet rs = new RecordSet();
        String sql = "select * from ecology_biz_ec where appid='"+appId+"'";
        rs.executeQuery(sql);
        String spk = "";
        String secret = "";
        if(rs.next()) {
            spk = rs.getString("server_public_key");
            secret = rs.getString("secrit");
        }
        if(spk.isEmpty() || secret.isEmpty()) {
            RSA rsa = new RSA();
            String publicKey = rsa.getPublicKeyBase64();
            rsa.getPrivateKeyBase64();

            String requestData = HttpRequest.post(baseUrl + "/api/ec/dev/auth/regist").header("appid", appId).header("cpk", publicKey).timeout(2000).execute().body();
            Console.log("注册接口信息："+requestData);

            rs.execute(sql);
            if(rs.next()) {
                spk = rs.getString("server_public_key");
                secret = rs.getString("secrit");
            }
        }
        RSA rsa = new RSA(null, spk);
        String encryptSecret = rsa.encryptBase64(secret, CharsetUtil.CHARSET_UTF_8, KeyType.PublicKey);
        String requestData = HttpRequest.post(baseUrl + "/api/ec/dev/auth/applytoken").header("appid", appId).header("secret", encryptSecret).header("time", "3600").execute().body();
        Console.log("申请token接口信息："+requestData);
        String token = (String) JSONUtil.parseObj(requestData).get("token");
        String encryptUserid = rsa.encryptBase64(String.valueOf(operator), CharsetUtil.CHARSET_UTF_8, KeyType.PublicKey);


        return new Request.Builder()
                .url(apiUrl)
                .addHeader("Content-Type", "application/json")
                .addHeader("appid", appId)
                .addHeader("token", token)
                .addHeader("userid", encryptUserid);
    }


    /**
     * 返回错误信息 并记录日志
     * @param errorMessage 错误信息
     * @return 错误信息结构体
     */
    private Map<String , String > rtnError(String errorMessage) {
        return rtnError(errorMessage, errorMessage);
    }

    /**
     * 返回错误信息 并记录日志
     * @param errorMessage 错误信息
     * @param logMessage 日志信息
     * @return 错误信息结构体
     */
    private Map<String , String > rtnError(String errorMessage, String logMessage) {
        Console.log(logMessage);
        Map<String , String > result = new HashMap<>();
        result.put("errmsg", errorMessage);
        result.put("flag", "false");
        return result;
    }

    /**
     * 获取配置属性值，若为空则返回默认值
     * @param propName 属性名
     * @param defaults 默认值
     * @return 属性值
     */
    private String  getPropsWithDefault(String propName, String defaults) {
        RecordSet rs = new RecordSet();
        String value = rs.getPropValue("QuoteConfig", propName);
        if (Util.null2String(value).isEmpty()) {
            value = defaults;
        }
        return value;
    }

    public static void main(String[] args) {
//        RSA rsa = new RSA();
//        String publickey = rsa.getPublicKeyBase64();
//        System.out.println("publickey = " + publickey);
        String appId = "rundo";
        String baseUrl = "http://192.168.11.20:8081";
        int operator = 1;
        String secret = "884f7a1a-0179-45eb-b79c-f4ed6dc26b5d";
        String spk = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAntWDGxDvxeeTgOB+6UiEjPc6czUgzHVPf8E6n5g59gGXgHp53Q1cOmfomn+ZYf7Vmj8MezZljD8Vnt8N4SMhygp66T4mfsSE0g0ZHU4fuzuEILAD0X4wUgBsRD8GA+QWZlGeTeInYdVahBra8LzAFxjupJNJtO9zjS1HRvIsN1wqxRbt5B2Je3pp0xW7IeXSFO3gu90Y9pVIB26/fAKel3j8sTujavSICPfrJoaaXW8rtlid1HnL1bJ606kui3fJ8xULa/vcPiuVwjYAlLYIn24RZBTdfUNT75ZTX4H6/TmZfXUBbhQUKi+FWH9MqVJ2zILr+ElCyavlSLxwweKa+wIDAQAB";
        RSA rsa = new RSA(null, spk);
        String encryptSecret = rsa.encryptBase64(secret, CharsetUtil.CHARSET_UTF_8, KeyType.PublicKey);
        System.out.println("encryptSecret = \n" + encryptSecret);
        String requestData = HttpRequest.post(baseUrl + "/api/ec/dev/auth/applytoken").header("appid", appId).header("secret", encryptSecret).header("time", "3600").execute().body();
        System.out.println("申请token接口信息："+requestData);
        String token = (String) JSONUtil.parseObj(requestData).get("token");
        String encryptUserid = rsa.encryptBase64(String.valueOf(operator), CharsetUtil.CHARSET_UTF_8, KeyType.PublicKey);
        System.out.println("encryptUserid = " + encryptUserid);
    }

}
