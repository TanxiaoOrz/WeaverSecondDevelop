<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%@ page import="weaver.general.Util,weaver.general.*" %>
<%@ include file="/systeminfo/init_wev8.jsp" %>
<jsp:useBean id="authEpic" class="com.supezet.authEpicNew" scope="page"/>


<%
String url = Util.null2String(request.getParameter("url"));
String param = "pm";
int hrmid = user.getUID();
String epicurl= "https://uat-pm-ic.supezet.com/#";
String loginkey = authEpic.getQueryLoginKey(epicurl, hrmid);
response.sendRedirect(url+loginkey);
%>

