package com.supezet;

import okhttp3.*;
import org.json.JSONObject;
import weaver.general.TimeUtil;
import weaver.general.Util;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author 张骏山
 * @Date 2023/9/21 10:24
 * @PackageName: com.supezet
 * @ClassName: SubContractorResultCommit
 * @Description: 供应商审批同意
 * @Version 1.0
 */
public class SubContractorResultCommit implements Action {

    String domain;
    String token;

    String auditNo;
    String auditResultMsg;
    String auditTime;

    @Override
    public String execute(RequestInfo requestInfo) {

        supezetUtil supezetUtil = new supezetUtil();
        token = supezetUtil.getToken();
        domain = supezetUtil.getDomain();

        auditNo = requestInfo.getRequestid();
        auditResultMsg = removeHtmlTag(requestInfo.getRequestManager().getRemark());
        auditTime = TimeUtil.getCurrentTimeString();

        DetailTable[] detailTable = requestInfo.getDetailTableInfo().getDetailTable();// 获取所有明细表
        for (DetailTable dt : detailTable) {
            Row[] s = dt.getRow();// 当前明细表的所有数据,按行存储
            // 指定行
            for (Row r : s) {
                Cell[] c = r.getCell();// 每行数据再按列存储
                String schemaSupplierId = null;
                String auditResultCode = null;
                // 指定列
                for (Cell c1 : c) {
                    String name = c1.getName();// 明细字段名称
                    if (name.equals("schemaSupplierId")) {
                        schemaSupplierId = Util.null2String(c1.getValue());
                    }
                    if (name.equals("sftg")) {
                        auditResultCode = Util.null2String(c1.getValue());
                    }
                }
                try{
                    post(schemaSupplierId,auditResultCode);
                }catch (Exception e) {
                    return FAILURE_AND_CONTINUE;
                }
            }
        }
        return SUCCESS;
    }

    private void post(String schemaSupplierId, String auditResultCode) throws Exception {
        JSONObject param = new JSONObject();
        JSONObject rtnJson;


        param.put("schemaSupplierId", schemaSupplierId);
        param.put("auditNo", auditNo);
        param.put("auditResultCode", auditResultCode);
        param.put("auditResultMsg", auditResultMsg);
        param.put("auditTime", auditTime);

        OkHttpClient client = new OkHttpClient().newBuilder().build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, param.toString());
        String api = "/external-api/purchase/externalProject/auditPackageSchemaPlan";
        Request requestPost = new Request.Builder().url(domain + api).method("POST", body)
                .addHeader("mall-sso-token", token)
                .addHeader("Content-Type", "application/json").build();
        Response response = client.newCall(requestPost).execute();

        supezetLog.log("请求报文=>" + param);
        rtnJson = new JSONObject(response.body().string());
        supezetLog.log("接口调用成功返回=>" + rtnJson);

        boolean success = rtnJson.getBoolean("success");
        String code = rtnJson.getString("code");
        String message = rtnJson.getString("message");

        if (!(success && code.equals("200"))) {
            supezetLog.log("error message =>"+message);
            throw new Exception();
        }
    }

    private String removeHtmlTag(String s) {
        String html = "<[^>]+>";
        Pattern pHtml = Pattern.compile(html,Pattern.CASE_INSENSITIVE);
        Matcher mHtml = pHtml.matcher(s);
        return mHtml.replaceAll("");
    }

}
