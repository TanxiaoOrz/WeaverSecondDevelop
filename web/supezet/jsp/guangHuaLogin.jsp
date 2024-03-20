<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%@ page import="weaver.hrm.*" %>
<%@ page import="com.supezet.AuthGuangHua" %>
<%
    AuthGuangHua authGuangHua = new AuthGuangHua("supezet_sso_123", "supezet");
    int userId = HrmUserVarify.getUser(request, response).getUID();
    String param = authGuangHua.getParam(userId);
    String guangHuaUrl= "https://supezet.21tb.com/"+"/login/sso.init.do";
    String returnUrl = guangHuaUrl + param;
    response.sendRedirect(returnUrl);
    // out.println(returnUrl);
%>

