<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%@ page import="weaver.general.Util" %>

<jsp:useBean id="rs" class="weaver.conn.RecordSet" scope="page"/>
<jsp:useBean id="rci" class="weaver.hrm.resource.ResourceComInfo" scope="page"/>

<%
    String rtnStr = "";
    String uid = Util.null2String(request.getParameter("uid"));
    rtnStr = rci.getSubCompanyID(uid);
%>

<%=rtnStr%>