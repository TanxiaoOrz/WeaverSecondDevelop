<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%@ page import="weaver.general.Util"%>

<jsp:useBean id="requestids" class="weaver.conn.RecordSet" scope="page" />

<%
String manager = Util.null2String(request.getParameter("manager"));
String tableName = Util.null2String(request.getParameter("tableName"));
String sql = "select counts(requestid) as co from "+tableName+" where pmdlua008_id = "+manager+" and requestid not in (select fplc from uf_htfplcjl) ";
//String sql = "update uf_xxtgmkbd set sfyd=1 where lcbh='"+lcbh+"'";
rs.execute(sql);
if (rs.next()) {
	int sum = rs.getInt("co");
	out.println(sum==0);
}
%>

<!-- 传入参数 manager 项目经理的id，从数据库中查询该项目经理下的合同流程请求中没有分配金额的数量，返回和0相比较的结果-->