<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%@ page import="weaver.general.Util"%>
<%@ page import="weaver.hrm.HrmUserVarify" %>

<jsp:useBean id="rs" class="weaver.conn.RecordSet" scope="page" />
<%
    int billId = Util.getIntValue(Util.null2String(request.getParameter("billId")));
    int documentId = Util.getIntValue(Util.null2String(request.getParameter("documentId")));
    String sql = "update uf_xw set cycs = cycs +1 where id = '"+ billId +"'";
    rs.execute(sql);
    response.sendRedirect("/spa/document/index.jsp?id="+documentId+"&router=1#/main/document/detail?");
%>
