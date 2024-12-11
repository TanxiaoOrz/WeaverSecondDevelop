<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%@ page import="weaver.general.Util"%>

<jsp:useBean id="rs" class="weaver.conn.RecordSet" scope="page" />
<%
    String fd_id = (Util.null2String(request.getParameter("fd_id")));
    String requestid = (Util.null2String(request.getParameter("requestid")));
    if (fd_id.equals(""))
        response.sendRedirect("/spa/workflow/static4form/index.html#/main/workflow/req?requestid="+requestid+"&ismonitor=1");
    else
        response.sendRedirect("/spa/custom/static/index.html#/main/cs/app/1902f56d1a994cadaec766c5b08438f9_pageSimple?fdId=" + fd_id);
%>