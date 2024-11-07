<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%@ page import="weaver.general.Util"%>
<%@ page import="weaver.hrm.HrmUserVarify" %>
<%@ page import="net.minidev.json.JSONObject" %>

<jsp:useBean id="rs" class="weaver.conn.RecordSet" scope="page" />
<%
    String userId = (Util.null2String(request.getParameter("billId")));
    if (userId.equals(""))
        userId = String.valueOf(HrmUserVarify.getUser(request, response).getUID());
    String alertString;
    rs.execute("select * from hrmresource where id = '"+userId+"'");
    if (rs.next()) {    // 课程是否存在
        JSONObject hrmInfo = new JSONObject();
        for (String column:
        rs.getColumnName()) {
            hrmInfo.put(column, rs.getString(column));
        }
        response.getWriter().println(hrmInfo.toString());
    } else
        response.getWriter().println("error");
%>
