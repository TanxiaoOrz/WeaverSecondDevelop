<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%@ page import="weaver.general.Util"%>
<%@ page import="weaver.hrm.HrmUserVarify" %>
<%@ page import="net.minidev.json.JSONObject" %>
<%@ page import="java.util.ArrayList" %>

<jsp:useBean id="rs" class="weaver.conn.RecordSet" scope="page" />
<%
    String billid = (Util.null2String(request.getParameter("billid")));
    String requestid = (Util.null2String(request.getParameter("requestid")));
    ArrayList<String> longs = new ArrayList<>();
    rs.execute("select * from workflow_requestbase where currentnodetype = 0 and requestid = " + requestid);
    if (rs.next()) {
        rs.execute("select id,ifnull(detailtable,'0dt0') detailtable from workflow_billfield where billid = " + billid);
        try {
            while (rs.next()) {
                JSONObject object = new JSONObject();
                object.put("id", rs.getString("id"));

                String string = rs.getString("detailtable").split("dt")[1];
                object.put("detailtable", string);

                longs.add(object.toString());
            }
            response.getWriter().println(longs.toString());
        } catch (Exception e) {
            response.getWriter().println(e.toString());
        }
    } else {
        response.getWriter().println(longs.toString());

    }
%>