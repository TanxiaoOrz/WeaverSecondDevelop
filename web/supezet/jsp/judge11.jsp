<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%@ page import="weaver.general.Util"%>

<jsp:useBean id="rs" class="weaver.conn.RecordSet" scope="page" />

<%
String htbh = Util.null2String(request.getParameter("htbh"));
String sql = "select isnull(sum(bcsyje),0) as je from uf_htfplcjl where sfyc != 1 and htbh = '"+htbh+"'";
//String sql = "update uf_xxtgmkbd set sfyd=1 where lcbh='"+lcbh+"'";
rs.execute(sql);
if (rs.next()) {
    int contactMoney = Integer.parseInt(Util.null2String(request.getParameter("htje")));
	float sum = rs.getFloat("je");
	//out.println(sum);
	//out.println(contactMoney);
	//out.println(",");
	//out.println(sum);
	//out.println(",");
	out.println(contactMoney - sum);
	//out.println(rs.toString());*/

}
%>