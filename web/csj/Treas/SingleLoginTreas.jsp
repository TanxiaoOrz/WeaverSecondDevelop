<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%@ page import="weaver.general.Util"%>
<%@ page import="weaver.hrm.HrmUserVarify" %>
<%@ page import="csj.treas_off.GetTreasTokenV2" %>
<%@ page import="csj.treas_off.TreasConfig" %>
<%@ page import="java.io.PrintWriter" %>

<jsp:useBean id="rs" class="weaver.conn.RecordSet" scope="page" />
<%
    //获取用户id
    int userId = HrmUserVarify.getUser(request, response).getUID();
    //获取重定向url
    String redirectUrl = request.getParameter("redirectUrl");

    //如果重定向url为空 ： 指定跳转链接
    if (Util.null2String(redirectUrl).equals(""))
        redirectUrl = "http://10.244.2.46:10600/portal/view/1183305232845447260?cusMenuId=1183305232845447260&urlPageTitle=57O757uf6aaW6aG1";
    //reload 读取配置文件状态 true 重新读取
    boolean reload = false;
    if (Util.null2String(request.getParameter("reload")).equals("true")) {
        reload = true;
    }
    if (reload) {
        //重读配置文件 更新配置信息
        TreasConfig.init();
    }
    String sql = "select workcode from hrmresource where id = "+userId;
    //执行sql查询
    rs.execute(sql);
    if (rs.next()) {
        String workcode = Util.null2String(rs.getString("workcode"));
        String token = new GetTreasTokenV2().getToken(workcode);
        if (token != null) {
            PrintWriter writer = response.getWriter();
            writer.println("token => "+token);
//            String finalUrl = (E10Link.isEmpty()?E10Config.getE10Url():E10Link) + "/papi/open/singleSignon?singleToken="+token+"&oauthType=singlesign&redirect_uri=" + redirectUrl;
//            response.sendRedirect(finalUrl);
        } else {
            out.println("token获取失败 请联系管理员");
        }
    } else {
        out.println("尚未登录");
    }
%>