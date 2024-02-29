package com.supezet;

import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import weaver.conn.RecordSet;
import weaver.general.TimeUtil;
import weaver.general.Util;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.RequestInfo;

public class auditResult4Commit implements Action {

    public String execute(RequestInfo request) {
        RecordSet rs = new RecordSet();
        supezetUtil supUtil = new supezetUtil();
        String domain = supUtil.getDomain();
        String token = supUtil.getToken();
        supezetLog.log("token=>"+token);
        String api = "/external-api/purchase/external/purchaseRequest/auditResult";

        String empty = "";

        String enquiryResultNo;
        String auditNo;	//没有看见该变量的使用位置
        int auditResultCode = 1;
        String auditResultMsg;
        String auditTime = TimeUtil.getCurrentTimeString();
        String companyCode;

        String requestid = request.getRequestid();
        String formtable = request.getRequestManager().getBillTableName();
        int operator = request.getRequestManager().getUserId();
        String remark = ""+request.getRequestManager().getRemark();
        String sql = "";

        JSONObject jsonMap = new JSONObject();
        JSONObject rtnJson = new JSONObject();

        sql = "select xjjgbh from "+formtable+" where requestid="+requestid;
        rs.execute(sql);
        if(rs.next()) {
            enquiryResultNo = Util.null2String(rs.getString(1));
        }else {
            enquiryResultNo = null;
        }

        auditNo = requestid;

        try {
            auditResultMsg = Util.HTMLtoTxt(remark);

            jsonMap.put("enquiryResultNo", enquiryResultNo==null?empty:enquiryResultNo);
            jsonMap.put("auditNo", auditNo==null?empty:auditNo);
            jsonMap.put("auditResultCode", auditResultCode);
            jsonMap.put("auditResultMsg", auditResultMsg==null?empty:auditResultMsg);
            jsonMap.put("auditTime", auditTime==null?empty:auditTime);

            OkHttpClient client = new OkHttpClient().newBuilder().build();
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, jsonMap.toString());
            Request requestPost = new Request.Builder().url(domain + api).method("POST", body)
                    .addHeader("mall-sso-token", token)
                    .addHeader("Content-Type", "application/json").build();
            Response response = client.newCall(requestPost).execute();

            supezetLog.log("请求报文=>"+jsonMap);
            rtnJson = new JSONObject(response.body().string());
            supezetLog.log("接口调用成功返回=>"+rtnJson);

            boolean success = rtnJson.getBoolean("success");
            String code = rtnJson.getString("code");
            String message = rtnJson.getString("message");

            if(success && code.equals("200")) {

            }else {
                request.getRequestManager().setMessagecontent(message);
                return Action.FAILURE_AND_CONTINUE;
            }

        }catch(Exception e) {
            supezetLog.log("异常："+e);
            request.getRequestManager().setMessagecontent("异常："+e);
            return Action.FAILURE_AND_CONTINUE;
        }

        return Action.SUCCESS;
    }


}
