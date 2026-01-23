<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%@ page import="weaver.general.Util"%>
<%@ page import="weaver.hrm.HrmUserVarify" %>
<%@ page import="csj.E10.E10Config" %>
<%@ page import="csj.E10.GetE10Token" %>
<jsp:useBean id="rs" class="weaver.conn.RecordSet" scope="page" />
<%
    String e10AppKey = E10Config.getE10AppKey();
    String e10AppSecurity = E10Config.getE10AppSecurity();
    String e10Url = E10Config.getE10Url();
    String e10CorpId = E10Config.getE10CorpId();
    String authType = E10Config.getAuthType();
    out.println("e10AppKey: " + e10AppKey);
    out.println("e10AppSecurity: " + e10AppSecurity);
    out.println("e10Url: " + e10Url);
    out.println("e10CorpId: " + e10CorpId);
    out.println("authType: " + authType);
%>

