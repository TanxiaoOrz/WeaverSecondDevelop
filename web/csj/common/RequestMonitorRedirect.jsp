<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%@ page import="weaver.general.Util" %>
<%@ page import="weaver.hrm.HrmUserVarify" %>

<jsp:useBean id="rs" class="weaver.conn.RecordSet" scope="page"/>
<%
    String jobid = (Util.null2String(request.getParameter("jobid")));
    String userId = String.valueOf(HrmUserVarify.getUser(request, response).getUID());
    String requestid = (Util.null2String(request.getParameter("requestid")));
    //  out.println("select * from hrmrolemembers where roleid in (" + jobid + ") and resourceid = " + userId);
    rs.execute("select * from hrmrolemembers where roleid in (" + jobid + ") and resourceid = " + userId);
    if (rs.next()) {    // 课程是否存在
        response.sendRedirect("/spa/workflow/static4form/index.html#/main/workflow/req?requestid=" + requestid + "&ismonitor=1");
    } else {
        response.sendRedirect("/spa/workflow/static4form/index.html#/main/workflow/req?requestid=" + requestid);
    }
%>
