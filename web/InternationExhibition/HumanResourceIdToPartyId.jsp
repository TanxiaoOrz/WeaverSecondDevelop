<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%@ page import="weaver.general.Util"%>

<jsp:useBean id="rs" class="weaver.conn.RecordSet" scope="page" />
<%
String humamId = Util.null2String(request.getParameter("humanId"));
String sql = "select id from formtable_main_395 where rlzyzd = "+ humamId;
// out.println(sql);
rs.execute(sql);
if (rs.next()) {
	int partyId = rs.getInt("id");
	out.println(partyId);
}else
	out.println("-1");
%>



<!-- 传入参数 humanId请求编号-->