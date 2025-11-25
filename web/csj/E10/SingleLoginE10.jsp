<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%@ page import="weaver.general.Util"%>
<%@ page import="weaver.hrm.HrmUserVarify" %>
<%@ page import="csj.E10.E10Config" %>
<%@ page import="csj.E10.GetE10Token" %>
<jsp:useBean id="rs" class="weaver.conn.RecordSet" scope="page" />
<%
    int userId = HrmUserVarify.getUser(request, response).getUID();
    String redirectUrl = request.getParameter("redirectUrl");
    if (Util.null2String(redirectUrl).equals(""))
        redirectUrl = "http://10.244.2.46:10600/portal/view/1183305232845447260?cusMenuId=1183305232845447260&urlPageTitle=57O757uf6aaW6aG1";
    else
        redirectUrl = redirectUrl.replace("createHA","create");
    boolean reload = false;
    if (Util.null2String(request.getParameter("reload")).equals("true")) {
        reload = true;
    }
    if (reload) {
        E10Config.init();
    }
    String sql = "select loginid from hrmresource where id = "+userId;
    rs.execute(sql);
    if (rs.next()) {
        String loginid = Util.null2String(rs.getString("loginid"));
        String token = new GetE10Token().getToken(loginid);
        if (token != null) {
            String finalUrl = E10Config.getE10Url()+"/papi/open/singleSignon?singleToken="+token+"&oauthType=singlesign&redirect_uri=" + redirectUrl;
            response.sendRedirect(finalUrl);
        }
    } else {
        out.println("尚未登录");
    }
%>

