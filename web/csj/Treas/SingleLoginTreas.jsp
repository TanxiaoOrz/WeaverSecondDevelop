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

    //reload 读取配置文件状态 true 重新读取
    boolean reload = Util.null2String(request.getParameter("reload")).equals("true");
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
            String finalUrl = TreasConfig.getUrl() + "/eib/#/atpJumpLogin?" + token;
            response.sendRedirect(finalUrl);
        } else {
            out.println("token获取失败 请联系管理员");
        }
    } else {
        out.println("尚未登录");
    }
%>