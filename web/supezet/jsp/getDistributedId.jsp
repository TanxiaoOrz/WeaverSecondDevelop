<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%@ page import="weaver.general.Util"%>

<jsp:useBean id="rs" class="weaver.conn.RecordSet" scope="page" />
<jsp:useBean id="rs1" class="weaver.conn.RecordSet" scope="page" />
<%
String requestid = Util.null2String(request.getParameter("requestid"));
String sql = "select top 1 ccsp,id from uf_jefpbd where dyhtlc = "+ requestid + " order by bbh desc";
//out.println(sql);
rs.execute(sql);
if (rs.next()) {
	int id = rs.getInt("id");
	out.println(id);
}else
	out.println("-1");
%>
