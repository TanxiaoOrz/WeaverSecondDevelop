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

public class auditResult4Refused implements Action {

	public String execute(RequestInfo request) {
		RecordSet rs = new RecordSet();
		supezetUtil supUtil = new supezetUtil();
		String domain = supUtil.getDomain();
		String token = supUtil.getToken();
		String api = "/external-api/purchase/external/purchaseRequest/auditResult";
		
		String enquiryResultNo = "";
		String auditNo = "";
		int auditResultCode = 0;
		String auditResultMsg = "";
		String auditTime = TimeUtil.getCurrentTimeString();
		
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
		}
		
		try {
			auditResultMsg = Util.HTMLtoTxt(remark);
			
			jsonMap.put("enquiryResultNo", enquiryResultNo);
			jsonMap.put("auditNo", requestid);
			jsonMap.put("auditResultCode", auditResultCode);
			jsonMap.put("auditResultMsg", auditResultMsg);
			jsonMap.put("auditTime", auditTime);
			
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
