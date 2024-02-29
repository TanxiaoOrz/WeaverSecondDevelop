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

public class syncCompany extends BaseCronJob {

	public void execute() {
		int counts = 0;
		supezetLog.log("分部同步开始");
		counts = syncCompany();
		supezetLog.log("分部同步结束, 共计"+counts);
	}

	private int syncCompany() {
		RecordSet rs = new RecordSet();
		RecordSet rst = new RecordSet();
		supezetUtil supUtil = new supezetUtil();
		String domain = supUtil.getDomain();
		String token = supUtil.getToken();
		String api = "/external-api/user-center/external/syncCompany";
		int counts = 0;
		
		if(!token.equals("")) {
			int dealType = -1;
			int externalCompanyId = 0;
			String companyName = "";
			String address = "";
			String telephone = "";
			String modifiedTime = "";
			String canceled = "";
			String erpCompanyId = "";
			
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
	
			String sql = "select id, subcompanyname, modified, showorder, canceled, subcompanycode "
					+ " from hrmsubcompany";
			rs.execute(sql);
			while(rs.next()) {
				externalCompanyId = Util.getIntValue(rs.getString("id"));
				companyName = Util.null2String(rs.getString("subcompanyname"));
				modifiedTime = Util.null2String(rs.getString("modified"));
				canceled = Util.null2String(rs.getString("canceled"));
				telephone = supUtil.getCompTelephone(externalCompanyId);
				erpCompanyId = supUtil.getCompERPId(externalCompanyId);
				
				dealType = supUtil.getCompDealType(externalCompanyId, modifiedTime);
				if(dealType == 2 && canceled.equals("1")) {
					dealType = 3;
				}
				
				if(dealType>0) {
					try {
						json = new JSONObject();
						jsonlist = new JSONObject();
						jsonarr = new JSONArray();
						
						jsonlist.put("externalCompanyId", externalCompanyId);
						jsonlist.put("erpCompanyId", erpCompanyId);
						jsonlist.put("companyName", companyName);
						jsonlist.put("address", address);
						jsonlist.put("telephone", telephone);
						jsonarr.put(jsonlist);
						
						json.put("dealType", dealType);
						json.put("externalCompanyDetailList",jsonarr);
						
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
							rtndata = rtnJson.getJSONObject("data").getJSONArray("externalCompanyDetailList").getJSONObject(0);
							resultCode = rtndata.getInt("resultCode");
						}
						
						upsql = "update uf_fbjl set gxzt='"+success+"', result='"+rtnJson+"' where fb="+externalCompanyId;
						upsqlResult = rst.execute(upsql);
						supezetLog.log("更新记录状态=>"+upsqlResult);
						
						if(resultCode == 200) {
							upsql = "update uf_fbjl set gxsj='"+modifiedTime+"' where fb="+externalCompanyId;
							upsqlResult = rst.execute(upsql);
							supezetLog.log("更新记录时间=>"+upsqlResult);
						}
						counts++;
					}catch(Exception e) {
						supezetLog.log("分部id="+externalCompanyId+",name="+companyName+",同步异常："+e);
					}
				}
				
			}
		}
		return counts;
	}

}
