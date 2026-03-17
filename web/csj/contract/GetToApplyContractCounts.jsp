<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%@ page import="weaver.hrm.HrmUserVarify" %>

<jsp:useBean id="rs" class="weaver.conn.RecordSet" scope="page" />
<%
    String userId = String.valueOf(HrmUserVarify.getUser(request, response).getUID());
    rs.execute("select count(*) as count from vw_contractreminderCount  where reminderperson = "+userId);
    if (rs.next()) {    // 课程是否存在
        response.getWriter().println(rs.getString("count"));
    } else
        response.getWriter().println("0");
%>
