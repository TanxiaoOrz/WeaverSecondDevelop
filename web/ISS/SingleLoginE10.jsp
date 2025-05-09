<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%@ page import="weaver.general.Util"%>
<%@ page import="java.util.Date" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.text.ParseException" %>
<%@ page import="weaver.hrm.HrmUserVarify" %>
<%@ page import="ISS.loginE10.GetE10Token" %>

<jsp:useBean id="rs" class="weaver.conn.RecordSet" scope="page" />
<%
    int userId = HrmUserVarify.getUser(request, response).getUID();
    String redirectUrl = request.getParameter("redirectUrl");
    String sql = "select loginid from hrmresource where id = "+userId;
    rs.execute(sql);
    if (rs.next()) {
        String loginid = Util.null2String(rs.getString("loginid"));
        String token = new GetE10Token().getToken(loginid);
        if (token != null) {
            String finalUrl = "https://oasandbox.oneiss.cn/papi/open/singleSignon?singleToken="+token+"&oauthType=singlesign&redirect_uri=" + redirectUrl;
            response.sendRedirect(finalUrl);
        }
    } else {
        out.println("尚未登录");
    }
%>

