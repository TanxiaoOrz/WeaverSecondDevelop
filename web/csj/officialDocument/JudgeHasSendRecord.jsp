<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%@ page import="weaver.general.Util"%>


<jsp:useBean id="rs" class="weaver.conn.RecordSet" scope="page" />
<%


    String requestid = Util.null2String(request.getParameter("requestid"));
    String sql = "select count(*) as count from vw_gwswsj where requestid = " + requestid;
    rs.execute(sql);
    if (rs.next()) {
        int count = Util.getIntValue(Util.null2String(rs.getString("count")));
        out.println(count);
    }
    out.println(0);
%>
