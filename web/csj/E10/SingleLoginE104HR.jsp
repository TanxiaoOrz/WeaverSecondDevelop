<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%@ page import="weaver.general.Util" %>
<%@ page import="weaver.hrm.HrmUserVarify" %>
<%@ page import="csj.E10.E10Config" %>
<%@ page import="csj.E10.GetE10TokenV5" %>
<jsp:useBean id="rs" class="weaver.conn.RecordSet" scope="page"/>
<jsp:useBean id="rci" class="weaver.hrm.resource.ResourceComInfo" scope="page"/>
<%

    String userId = "" + HrmUserVarify.getUser(request, response).getUID();
    String targetuid = request.getParameter("targetcard");

    String redirectUrl = "/sp/hrm/profileInfo/";
    boolean reload = false;
    if (Util.null2String(request.getParameter("reload")).equals("true")) {
        reload = true;
    }
    if (reload) {
        E10Config.init();
    }
    String loginidSql = "select workcode from hrmresource where id = " + userId;
    rs.execute(loginidSql);
    if (!rs.next()) {
        out.println("未知用户");
        return;
    }
    String loginid = Util.null2String(rs.getString("workcode"));

    String sql = "select certificatenum from hrmresource where id = " + targetuid;
    rs.execute(sql);
    if (rs.next()) {
        String certificatenum = Util.null2String(rs.getString(1));

        String token = new GetE10TokenV5().getToken(loginid);
        if (token != null) {
            String finalUrl = E10Config.getInstance().getE10Url() + "/papi/open/singleSignon?singleToken=" + token + "&oauthType=singlesign&redirect_uri=" + redirectUrl + certificatenum;
            response.sendRedirect(finalUrl);

        }
    } else {
        out.println("尚未登录");
    }
%>
