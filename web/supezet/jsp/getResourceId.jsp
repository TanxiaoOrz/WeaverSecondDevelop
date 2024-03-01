<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%@ page import="weaver.general.Util"%>

<jsp:useBean id="rs" class="weaver.conn.RecordSet" scope="page" />
<jsp:useBean id="rs1" class="weaver.conn.RecordSet" scope="page" />
<%
String roleid = Util.null2String(request.getParameter("roleid"));
String sql = "select resourceid from hrmrolemembers where roleid =" + roleid;
//out.println(sql);
rs.execute(sql);
String ret = "";
while (rs.next()) {
	ret += rs.getInt("resourceid");
    ret += ",";
}
out.println(ret);
%>
