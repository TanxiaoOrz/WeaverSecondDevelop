<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%@ page import="weaver.general.Util"%>
<%@ page import="java.util.Date" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.text.ParseException" %>

<jsp:useBean id="rs" class="weaver.conn.RecordSet" scope="page" />
<%
    String sql = "select max(modifydatetime) from meeting";
    Long timeStamp = new Long(Util.null2String(request.getParameter("timestamp")));
    rs.execute(sql);
    if (rs.next()) {
        String s = Util.null2String(rs.getString("max"));
//        out.println(s);
        SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date;
        try {
            date = ft.parse(s);
            if (date.getTime() > timeStamp + 1000)
                out.println(1);
            else
                out.println(0);
        } catch (ParseException e) {
            out.println(e);
        }

    }
%>

