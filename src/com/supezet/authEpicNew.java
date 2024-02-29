package com.supezet;

import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import weaver.general.Util;

public class authEpicNew {

	public String getQueryLoginKey(String url, int userid) {
		supezetUtil supUtil = new supezetUtil();
		String username = supUtil.getUsername();
		String password = supUtil.getPassword();
		String domain = url;
		String token = "";
		String api = "";
		
		String queryLoginKey = "";
		JSONObject json = new JSONObject();
		JSONObject reqJson = new JSONObject();
		JSONObject rtnJson = null;
		
		try {
			
			OkHttpClient client = new OkHttpClient().newBuilder().build();
			MediaType mediaType = MediaType.parse("application/json");
			RequestBody body = null;
			Request request = null;
			Response response = null;

			//获取token
			api = "/external-api/getToken";
			domain = supUtil.getDomain();
			json.put("userName", username);
			json.put("password", password);
			mediaType = MediaType.parse("application/json");
			body = RequestBody.create(mediaType, json.toString());
			request = new Request.Builder().url(domain + api).method("POST", body)
					.addHeader("Content-Type", "application/json").build();
			response = client.newCall(request).execute();
			rtnJson = new JSONObject(response.body().string());
			supezetLog.log("请求报文:\n"+json.toString()+", token接口返回:\n"+rtnJson);
			token = Util.null2String(rtnJson.getString("token"));
			
			//获取loginkey
			username = supUtil.getUserERPId(userid);
			api = "/external-api/user-center/external/queryLoginKey";
			reqJson.put("source", "OA");
			reqJson.put("userName", username);
			body = RequestBody.create(mediaType, reqJson.toString());
			request = new Request.Builder().url(domain + api).method("POST", body)
					.addHeader("mall-sso-token", token)
					.addHeader("Content-Type", "application/json")
					.build();
			response = client.newCall(request).execute();
			rtnJson = new JSONObject(response.body().string());
			supezetLog.log("请求报文:\n"+reqJson.toString()+",loginkey接口返回："+rtnJson);
			
			boolean success = rtnJson.getBoolean("success"); 
			String code = rtnJson.getString("code");
			JSONObject dataJson = rtnJson.getJSONObject("data");
			int resultCode = dataJson.getInt("resultCode");
			if(success && code.equals("200") && resultCode == 200) {
				queryLoginKey = dataJson.getString("loginKey");
			}
		
		}catch(Exception e) {
			supezetLog.log("验证异常："+e);
		}
		
		return queryLoginKey;
	}

}
