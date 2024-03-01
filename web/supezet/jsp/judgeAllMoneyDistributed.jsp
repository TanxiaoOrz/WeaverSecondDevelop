<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%@ page import="weaver.general.Util"%>

<jsp:useBean id="rs" class="weaver.conn.RecordSet" scope="page" />
<%
String requestid = Util.null2String(request.getParameter("requestid"));
String formid = Util.null2String(request.getParameter("formid"));
String sql = "select ccsp,id from uf_jefpbd where sxzt = 0 and dyhtlc = "+ requestid;
//out.println(sql);
rs.execute(sql);
if (rs.next()) {
	int id = rs.getInt("id");
	//out.println("  ");
	//out.println(id);
	//out.println("  ");
	//out.println(rs.getInt("ccsp")==1);
	if (rs.getInt("ccsp")==1) {
		rs.execute("SELECT tableName FROM workflow_bill where id ="+formid);
		rs.next();
		String formtable = Util.null2String(rs.getString("tableName"));
		//out.println(formtable);
		String sqlUpdate = "update " + formtable + " set ccsp = 1 , fpjl = " + id + " where requestid = " + requestid; 
		//out.println(sqlUpdate);
		rs.execute(sqlUpdate);
		out.println(id);
		
	}else
		out.println("0");
}else
	out.println("-1");
%>



<!-- 传入参数 requestId请求编号,formtable表单名称,dataId数据编号,1代表审批增肌，0代表正常审批，-1代表不能提交-->