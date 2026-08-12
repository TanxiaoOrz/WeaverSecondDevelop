<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="weaver.general.Util"%>
<%@ page import="weaver.hrm.HrmUserVarify" %>
<%@ page import="csj.E10.E10Config" %>
<%@ page import="csj.E10.GetE10TokenV5" %>
<jsp:useBean id="rs" class="weaver.conn.RecordSet"/>
<%

    int userId = HrmUserVarify.getUser(request, response).getUID();
    String redirectUrl = request.getParameter("redirectUrl");
    String code = request.getParameter("code");
    String E10Link = Util.null2String(request.getParameter("E10Link"));
    E10Config e10Config = E10Config.getInstance(code);
    if (Util.null2String(redirectUrl).isEmpty())
        redirectUrl = e10Config.getE10Url() + "/portal/view/1183305232845447260?cusMenuId=1183305232845447260&urlPageTitle=57O757uf6aaW6aG1";
    else
        redirectUrl = redirectUrl.replace("createHA","create");
    boolean reload = Util.null2String(request.getParameter("reload")).equals("true");
    if (reload) {
        E10Config.init();
    }
    String sql = "select workcode from hrmresource where id = "+userId;
    rs.execute(sql);
    if (rs.next()) {
        String loginid = Util.null2String(rs.getString("workcode"));
        String token = new GetE10TokenV5(code).getToken(loginid);
        if (token != null) {
            String finalUrl = (E10Link.isEmpty()?e10Config.getE10Url():E10Link) + "/papi/open/singleSignon?singleToken="+token+"&oauthType=singlesign&redirect_uri=" + redirectUrl;
            response.sendRedirect(finalUrl);
        }
    } else {
        out.println("尚未登录");
    }
%>

