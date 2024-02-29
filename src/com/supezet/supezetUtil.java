package com.supezet;

import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import weaver.conn.RecordSet;
import weaver.general.Util;

public class supezetUtil {
	/*
	 * 从建模数据表中获取集成的username
	 */
	public String getUsername() {
		String result = "";
		RecordSet rs = new RecordSet();
		String sql = "select username from uf_jkdzpz";
		rs.execute(sql);
		rs.next();
		result = Util.null2String(rs.getString(1));
		return result;
	}

	/*
	 * 从建模数据表中获取password
	 */
	public String getPassword() {
		String result = "";
		RecordSet rs = new RecordSet();
		String sql = "select password from uf_jkdzpz";
		rs.execute(sql);
		rs.next();
		result = Util.null2String(rs.getString(1));
		return result;
	}

	/*
	 * 从建模数据表中获取接口地址
	 */
	public String getDomain() {
		String result = "";
		RecordSet rs = new RecordSet();
		String sql = "select domain from uf_jkdzpz";
		rs.execute(sql);
		rs.next();
		result = Util.null2String(rs.getString(1));
		return result;
	}

	/*
	 * 获取第三方Token
	 */
	public String getToken() {
		String username = getUsername();
		String password = getPassword();
		String domain = getDomain();
		String api = "/external-api/getToken";
		String token = "";
		supezetLog.log("获取token");
		try {
			JSONObject json = new JSONObject();
			json.put("userName", username);
			json.put("password", password);

			OkHttpClient client = new OkHttpClient().newBuilder().build();
			MediaType mediaType = MediaType.parse("application/json");
			RequestBody body = RequestBody.create(mediaType, json.toString());
			Request requestPost = new Request.Builder().url(domain + api).method("POST", body)
					.addHeader("Content-Type", "application/json").build();
			Response response = client.newCall(requestPost).execute();

			JSONObject rtnJson = new JSONObject(response.body().string());
			supezetLog.log("接口返回"+rtnJson);
			token = Util.null2String(rtnJson.getString("token"));
		} catch (Exception e) {
			supezetLog.log("获取token异常：" + e);
		}

		return token;
	}

	/*
	 * 通过分部id获取对应的管理员电话
	 */
	public String getCompTelephone(int externalCompanyId) {
		RecordSet rs = new RecordSet();
		String telephone = "";
		String sql = "select glysjh from HrmSubcompanyDefined where subcomid=" + externalCompanyId;
		rs.execute(sql);
		if (rs.next()) {
			telephone = Util.null2String(rs.getString(1));
		}

		return telephone;
	}

	/*
	 * 通过分部id获取对应的erpid
	 */
	public String getCompERPId(int externalCompanyId) {
		RecordSet rs = new RecordSet();
		String erpid = "";
		String sql = "select erpid from HrmSubcompanyDefined where subcomid=" + externalCompanyId;
		rs.execute(sql);
		if (rs.next()) {
			erpid = Util.null2String(rs.getString(1));
		}

		return erpid;
	}

	/*
	 * 通过编码id获取对应的erpid
	 */
	public String getDeptERPId(int externalDepartmentId) {
		RecordSet rs = new RecordSet();
		String erpid = "";
		String sql = "select erpid from HrmDepartmentDefined where deptid=" + externalDepartmentId;
		rs.execute(sql);
		if (rs.next()) {
			erpid = Util.null2String(rs.getString(1));
		}

		return erpid;
	}
	
	/*
	 * 通过编码id获取对应的erpid
	 */
	public String getUserERPId(int externalUserId) {
		RecordSet rs = new RecordSet();
		String erpid = "";
		String sql = "select field1 from cus_fielddata where scope='HrmCustomFieldByInfoType' and scopeid=-1 and id=" + externalUserId;
		rs.execute(sql);
		if (rs.next()) {
			erpid = Util.null2String(rs.getString(1));
		}

		return erpid;
	}

	/*
	 * 查询分部的类型1添加2修改3删除
	 */
	public int getCompDealType(int externalCompanyId, String modifiedTime) {
		RecordSet rs = new RecordSet();
		RecordSet rst = new RecordSet();
		int dealType = -1;
		String sql = "select gxsj from uf_fbjl where fb=" + externalCompanyId;
		String gxsj = "";
		rs.execute(sql);
		if (rs.next() && rs.getCounts() > 0) {
			gxsj = Util.null2String(rs.getString(1));
			if (!gxsj.equals(modifiedTime) && !gxsj.equals("")) {
				dealType = 2;
				supezetLog.log("分部id=》" + externalCompanyId + "存在，操作类型=" + dealType);
			}else if(gxsj.equals("")) {
				dealType = 1;
				supezetLog.log("分部id=》" + externalCompanyId + "新增同步失败，操作类型=" + dealType);
			}
		} else {
			dealType = 1;
			sql = "insert into uf_fbjl(fb) values(" + externalCompanyId + ")";
			rst.execute(sql);
			supezetLog.log("分部id=》" + externalCompanyId + "不存在，操作类型=" + dealType);
		}

		return dealType;
	}

	/*
	 * 查询部门的类型1添加2修改3删除
	 */
	public int getDeptDealType(int externalDepartmentId, String modifiedTime) {
		RecordSet rs = new RecordSet();
		RecordSet rst = new RecordSet();
		int dealType = -1;
		String sql = "select gxsj from uf_bmjl where bm=" + externalDepartmentId;
		String gxsj = "";
		rs.execute(sql);
		if (rs.next() && rs.getCounts() > 0) {
			gxsj = Util.null2String(rs.getString(1));
			if (!gxsj.equals(modifiedTime) && !gxsj.equals("")) {
				dealType = 2;
				supezetLog.log("部门id=》" + externalDepartmentId + "存在，操作类型=" + dealType);
			}else if(gxsj.equals("")) {
				dealType = 1;
				supezetLog.log("部门id=》" + externalDepartmentId + "新增同步失败，操作类型=" + dealType);
			}
		} else {
			dealType = 1;
			sql = "insert into uf_bmjl(bm) values(" + externalDepartmentId + ")";
			rst.execute(sql);
			supezetLog.log("部门id=》" + externalDepartmentId + "不存在，操作类型=" + dealType);
		}

		return dealType;
	}

	/*
	 * 查询人员的类型1添加2修改3删除
	 */
	public int getUserDealType(int externalUserId, String modifiedTime) {
		RecordSet rs = new RecordSet();
		RecordSet rst = new RecordSet();
		int dealType = -1;
		String sql = "select gxsj from uf_ryjl where ry=" + externalUserId;
		String gxsj = "";
		rs.execute(sql);
		if (rs.next() && rs.getCounts() > 0) {
			gxsj = Util.null2String(rs.getString(1));
			if (!gxsj.equals(modifiedTime) && !gxsj.equals("")) {
				dealType = 2;
				supezetLog.log("人员id=》" + externalUserId + "存在，操作类型=" + dealType);
			}else if(gxsj.equals("")) {
				dealType = 1;
				supezetLog.log("人员id=》" + externalUserId + "新增同步失败，操作类型=" + dealType);
			}
		} else {
			dealType = 1;
			sql = "insert into uf_ryjl(ry) values(" + externalUserId + ")";
			rst.execute(sql);
			supezetLog.log("人员id=》" + externalUserId + "不存在，操作类型=" + dealType);
		}

		return dealType;
	}
}
