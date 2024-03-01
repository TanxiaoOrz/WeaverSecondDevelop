<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%@ page import="weaver.hrm.*" %>
<%@ page import="com.supezet.AuthGuangHua" %>
<%
    AuthGuangHua authGuangHua = new AuthGuangHua("secret", "corpCode");
    int userId = HrmUserVarify.getUser(request, response).getUID();
    String param = authGuangHua.getParam(userId);
    String guangHuaUrl= "http://localhost:8080"+"/login/sso.init.do";
    String returnUrl = guangHuaUrl + param;
    response.sendRedirect(returnUrl);
%>

