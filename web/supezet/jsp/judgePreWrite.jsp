<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%@ page import="weaver.general.Util"%>

<jsp:useBean id="rs" class="weaver.conn.RecordSet" scope="page" />
<%
String requestid = Util.null2String(request.getParameter("requestid"));
String sql = "select xmjl from uf_jefpbd_caigou where htsplc = "+ requestid;
//out.println(sql);
rs.execute(sql);
if (rs.next()) {
	int xmjl = rs.getInt("xmjl");
	
	out.println("xmjl");
}else
	out.println("-1");
%>



<!-- 传入参数 requestId请求编号,正数代表正常预填写的对应项目经理，-1代表没有预填写-->