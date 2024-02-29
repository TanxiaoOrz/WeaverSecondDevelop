package com.supezet;

import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class authEpic {

	public static void main(String[] args) {
		// TODO 自动生成的方法存根

	}
	
	public String getQueryLoginKey(int userid) {
		supezetUtil supUtil = new supezetUtil();
		String domain = supUtil.getDomain();
		String token = supUtil.getToken();
		String api = "/external-api/user-center/external/queryLoginKey";
		
		String username = supUtil.getUserERPId(userid);
		String queryLoginKey = "";
		JSONObject reqJson = new JSONObject();
		
		try {
			reqJson.put("source", "OA");
			reqJson.put("userName", username);
			
			OkHttpClient client = new OkHttpClient().newBuilder().build();
			MediaType mediaType = MediaType.parse("application/json");
			RequestBody body = RequestBody.create(mediaType, reqJson.toString());
			Request request = new Request.Builder().url(domain + api).method("POST", body)
					.addHeader("mall-sso-token", token)
					.addHeader("Content-Type", "application/json")
					.build();
			Response response = client.newCall(request).execute();
			JSONObject rtnJson = new JSONObject(response.body().string());
			supezetLog.log("接口返回："+rtnJson);
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
