package com.supezet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import weaver.general.TimeUtil;
import weaver.general.Util;

public class suptest {

	public static void main(String[] args) throws Exception {
		// TODO 自动生成的方法存根
//		int i = 1.2;
		System.out.println(TimeUtil.getCurrentTimeString());
		
		String username = "epec_test";
		String password = "123456";
		String domain = "http://uat-apigateway-ic.supezet.com";
		String api = "/external-api/getToken";
		String token = "";
//		supezetLog.log("获取token");
		JSONArray jarr = new JSONArray();
		String remark = "<p>yesysy</p><br/>";
		System.out.println(Util.HTMLtoTxt(remark));
		String rtn = "{\"success\":true,\"code\":\"200\",\"message\":\"OK\",\"data\":[]}";
		
//		if(false){
			try {
				
				JSONObject json = new JSONObject();
				json = new JSONObject(rtn);
				System.out.println(json.getBoolean("success"));
				System.out.println(json.getString("code"));
//				json.put("userName", username);
//				json.put("password", "123456");
//				
//				OkHttpClient client = new OkHttpClient().newBuilder().build();
//				MediaType mediaType = MediaType.parse("application/json");
//				RequestBody body = RequestBody.create(mediaType, json.toString());
//				Request requestPost = new Request.Builder().url( domain + api).method("POST", body)
//						.addHeader("Content-Type", "application/json").build();
//				Response response = client.newCall(requestPost).execute();
//				
//				JSONObject rtnJson = new JSONObject(response.body().string());
//				token = Util.null2String(rtnJson.getString("token"));
//				System.out.println(rtnJson);
//				
//				json = new JSONObject();
//				JSONObject jsonlist = new JSONObject();
//				JSONArray jsonarr = new JSONArray();
//				
//				jsonlist.put("externalCompanyId", 2);
//				jsonlist.put("erpCompanyId", "zh");
//				jsonlist.put("companyName", "上海卓然工程技术股份有限公司");
//				jsonlist.put("address", "");
//				jsonlist.put("telephone", "13127876610");
//				jsonarr.put(jsonlist);
//				
//				json.put("dealType", 2);
//				json.put("externalCompanyDetailList",jsonarr);
//				
//				api = "/external-api/user-center/external/syncCompany";
//				body = RequestBody.create(mediaType, json.toString());
//				requestPost = new Request.Builder().url(domain + api).method("POST", body)
//						.addHeader("mall-sso-token", token)
//						.addHeader("Content-Type", "application/json").build();
//				response = client.newCall(requestPost).execute();
//				rtnJson = new JSONObject(response.body().string());
//				System.out.println(rtnJson);
				
			}catch(Exception e) {
				System.out.println("error:"+e);
	//			supezetLog.log("获取token异常："+e);
			}
//		}
//		String companyResult = "{\"success\":true,\"code\":\"200\",\"message\":\"OK\",\"data\":{\"dealType\":1,\"externalCompanyDetailList\":[{\"resultCode\":200,\"externalCompanyId\":1,\"companyName\":\"易派客测试分部\",\"address\":\"河北省克孜勒苏柯尔克孜自治州昌黎县\",\"telephone\":\"13810000000\"}]}}";
//		String userstr = "{\"code\":\"200\",\"data\":{\"externalUserDetailAOList\":[{\"externalDepartmentId\":2,\"realName\":\"张锦华\",\"externalCompanyId\":2,\"externalUserId\":7,\"erpUserId\":-1,\"resultCode\":100,\"errorMessage\":\"用户手机号为空\",\"telephone\":\"\",\"userName\":\"w3\",\"email\":\"\"}],\"dealType\":1},\"success\":true,\"message\":\"OK\"}";
//		JSONObject json = new JSONObject(userstr);
//		JSONObject data =  json.getJSONObject("data").getJSONArray("externalUserDetailAOList").getJSONObject(0);
//		String code = data.getString("resultCode");
//		data.getInt("resultCode");
//		System.out.println(data);
//		System.out.println(data.getInt("resultCode"));
			
			int i = 5;
			sss(i);
	}
	
	private static void sss(int i) {
		System.out.println(Util.getIntValue((short) i));
	}

}
