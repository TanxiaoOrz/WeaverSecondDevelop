package com.supezet;

import org.json.JSONArray;
import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import weaver.conn.RecordSet;
import weaver.general.Util;
import weaver.interfaces.schedule.BaseCronJob;

public class syncUser extends BaseCronJob {

	public void execute() {
		int counts = 0;
		supezetLog.log("人员同步开始");
		counts = syncUser();
		supezetLog.log("人员同步结束, 共计"+counts);
	}
	
	
	private int syncUser() {
		RecordSet rs = new RecordSet();
		RecordSet rst = new RecordSet();
		supezetUtil supUtil = new supezetUtil();
		String domain = supUtil.getDomain();
		String token = supUtil.getToken();
		String api = "/external-api/user-center/external/syncUser";
		int counts = 0;
		
		if(!token.equals("")) {
			int dealType = 1;
			int externalUserId = 0;
			int externalCompanyId = 0;
			int externalDepartmentId = 0;
			String userName = "";
			String realName = "";
			String telephone = "";
			String email = "";
			String modifiedTime = "";
			int status = 0;
			String erpUserId = "";
			
			JSONObject json = new JSONObject();
			JSONObject jsonlist = new JSONObject();
			JSONArray jsonarr = new JSONArray();
			JSONObject rtnJson = new JSONObject();
			boolean success = false;
			String code = "";
			String message = "";
			JSONObject rtndata = new JSONObject();
			int resultCode = 0;
			
			OkHttpClient client = new OkHttpClient().newBuilder().build();
			MediaType mediaType = MediaType.parse("application/json");
			RequestBody body = null;
			Request requestPost = null;
			Response response = null;
			String upsql = "";
			boolean upsqlResult = false;
			
			String sql = "select id, loginid, lastname, subcompanyid1, departmentid, mobile, email, modified, status "
					+ " from hrmresource";
			rs.execute(sql);
			while(rs.next()) {
				externalUserId = Util.getIntValue(rs.getString("id"));
				externalCompanyId = Util.getIntValue(rs.getString("subcompanyid1"));
				externalDepartmentId = Util.getIntValue(rs.getString("departmentid"));
				userName = Util.null2String(rs.getString("loginid"));
				realName = Util.null2String(rs.getString("lastname"));
				telephone = Util.null2String(rs.getString("mobile"));
				email = Util.null2String(rs.getString("email"));
				modifiedTime = Util.null2String(rs.getString("modified"));
				status = Util.getIntValue(rs.getString("status"));
						
				erpUserId = supUtil.getUserERPId(externalUserId);
				dealType = supUtil.getUserDealType(externalUserId, modifiedTime);
				if(dealType == 2 && status > 3) {
					dealType = 3;
				}
				
				if(dealType>0) {
					try {
						json = new JSONObject();
						jsonlist = new JSONObject();
						jsonarr = new JSONArray();
						
						jsonlist.put("externalUserId", externalUserId);
						jsonlist.put("erpUserId", erpUserId);
						jsonlist.put("externalCompanyId", externalCompanyId);
						jsonlist.put("externalDepartmentId", externalDepartmentId);
						jsonlist.put("userName", userName);
						jsonlist.put("realName", realName);
						jsonlist.put("telephone", telephone);
						jsonlist.put("email", email);
						jsonarr.put(jsonlist);
						
						json.put("dealType", dealType);
						json.put("externalUserDetailAOList", jsonarr);
						
						body = RequestBody.create(mediaType, json.toString());
						requestPost = new Request.Builder().url(domain + api).method("POST", body)
								.addHeader("mall-sso-token", token)
								.addHeader("Content-Type", "application/json").build();
						response = client.newCall(requestPost).execute();
						
						rtnJson = new JSONObject(response.body().string());
						success = rtnJson.getBoolean("success");
						code = rtnJson.getString("code");
						message = rtnJson.getString("message");
						supezetLog.log("请求报文=>"+json.toString());
						supezetLog.log("接口调用成功返回=>"+rtnJson);
						if(success) {
							rtndata = rtnJson.getJSONObject("data").getJSONArray("externalUserDetailAOList").getJSONObject(0);
							resultCode = rtndata.getInt("resultCode");
						}
						
						upsql = "update uf_ryjl set gxzt='"+success+"', result='"+rtnJson+"' where ry="+externalUserId;
						upsqlResult = rst.execute(upsql);
						supezetLog.log("更新记录状态=>"+upsqlResult);
						
						if(resultCode == 200) {
							upsql = "update uf_ryjl set gxsj='"+modifiedTime+"' where ry="+externalUserId;
							upsqlResult = rst.execute(upsql);
							supezetLog.log("更新记录时间=>"+upsqlResult);
						}
						counts++;
					}catch(Exception e) {
						supezetLog.log("人员id="+externalUserId+",name="+realName+",同步异常："+e);
					}
				}
			}
			
		}
		return counts;
	}

}
