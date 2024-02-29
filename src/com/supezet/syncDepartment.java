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

public class syncDepartment extends BaseCronJob {

	public void execute() {
		int counts = 0;
		supezetLog.log("部门同步开始");
		counts = syncDepartment();
		supezetLog.log("部门同步结束, 共计"+counts);
	}

	private int syncDepartment() {
		RecordSet rs = new RecordSet();
		RecordSet rst = new RecordSet();
		supezetUtil supUtil = new supezetUtil();
		String domain = supUtil.getDomain();
		String token = supUtil.getToken();
		String api = "/external-api/user-center/external/syncDepartment";
		int counts = 0;
		
		if(!token.equals("")) {
			int dealType = -1;
			int externalDepartmentId = 0;
			int externalCompanyId = 0;
			int parentDepartmentId = 0;
			String departmentName = "";
			int sortOrder = 0;
			String modifiedTime = "";
			String canceled = "";
			String erpDepartmentId = "";
			
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
			
			String sql = "select id, departmentname, subcompanyid1, supdepid, modified, showorder, canceled "
					+ " from hrmdepartment";
			rs.execute(sql);
			while(rs.next()) {
				externalDepartmentId = Util.getIntValue(rs.getString("id"));
				externalCompanyId = Util.getIntValue(rs.getString("subcompanyid1"));
				parentDepartmentId = Util.getIntValue(rs.getString("supdepid"), 0);
				departmentName = Util.null2String(rs.getString("departmentname"));
				modifiedTime = Util.null2String(rs.getString("modified"));
				canceled = Util.null2String(rs.getString("canceled"));
				erpDepartmentId = supUtil.getDeptERPId(externalDepartmentId);
				
				dealType = supUtil.getDeptDealType(externalDepartmentId, modifiedTime);
				if(dealType == 2 && canceled.equals("1")) {
					dealType = 3;
				}
				
				if(dealType>0) {
					try {
						json = new JSONObject();
						jsonlist = new JSONObject();
						jsonarr = new JSONArray();
					
						jsonlist.put("externalDepartmentId", externalDepartmentId);
						jsonlist.put("erpDepartmentId", erpDepartmentId);
						jsonlist.put("externalCompanyId", externalCompanyId);
						jsonlist.put("parentDepartmentId", parentDepartmentId);
						jsonlist.put("departmentName", departmentName);
						jsonlist.put("sortOrder", sortOrder);
						jsonarr.put(jsonlist);
						
						json.put("dealType", dealType);
						json.put("externalDepartmentDetailAOList",jsonarr);
						
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
							rtndata = rtnJson.getJSONObject("data").getJSONArray("externalDepartmentDetailAOList").getJSONObject(0);
							resultCode = rtndata.getInt("resultCode");
						}
						
						upsql = "update uf_bmjl set gxzt='"+success+"', result='"+rtnJson+"' where bm="+externalDepartmentId;
						upsqlResult = rst.execute(upsql);
						supezetLog.log("更新记录状态=>"+upsqlResult);
						
						if(resultCode == 200) {
							upsql = "update uf_bmjl set gxsj='"+modifiedTime+"' where bm="+externalDepartmentId;
							upsqlResult = rst.execute(upsql);
							supezetLog.log("更新记录时间=>"+upsqlResult);
						}
						counts ++;
					} catch (Exception e) {
						supezetLog.log("部门id="+externalDepartmentId+",name="+departmentName+",同步异常："+e);
					}
				}
				
			}
		}
		return counts;
	}

}
