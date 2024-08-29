<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%@ page import="weaver.general.Util"%>
<%@ page import="weaver.hrm.HrmUserVarify" %>

<jsp:useBean id="rs" class="weaver.conn.RecordSet" scope="page" />
<%
    int hrmResourceID = Util.getIntValue(Util.null2String(request.getParameter("hrmResourceID")));
    String sql = "select id from uf_rsda where ry  = '"+ hrmResourceID +"'";
    rs.execute(sql);
    if (rs.next())
        response.sendRedirect("/spa/cube/index.html#/main/cube/card?type=0&modeId=88&formId=-294&billid="+rs.getInt("id")+"&opentype=0&customid=97");
%>
<div>该角色无档案信息</div>
