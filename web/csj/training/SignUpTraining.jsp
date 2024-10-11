<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%@ page import="weaver.general.Util"%>
<%@ page import="weaver.hrm.HrmUserVarify" %>

<jsp:useBean id="rs" class="weaver.conn.RecordSet" scope="page" />
<%
    int billId = Util.getIntValue(Util.null2String(request.getParameter("billId")));
    int userId = HrmUserVarify.getUser(request, response).getUID();
    String alertString;
    rs.execute("select kczt from uf_pxhy where id = '"+billId+"'");
    if (rs.next()) {    // 课程是否存在
        int kczt = rs.getInt("kczt");
        if (kczt == 1) {    // 报名中
            String sql = "select count(*) as c from uf_pxhy_dt1 where bmchry = '"+userId+"' and mainid = '"+ billId +"'";
            rs.execute(sql);
            if (rs.next()) {    //是否已报名
                int counts = rs.getInt("c");
                if (counts == 0) {
                    rs.execute("insert into uf_pxhy_dt1 (bmchry,mainid) values (" + userId + "," + billId + ")");
                    // out.println("insert into uf_pxhy_dt1 (bmchry,mainid) values (" + userId + "," + billId + ")");
                    alertString = "报名成功";
                } else {
                    alertString = "您已报名";
                }
            } else
                alertString = "您已报名";
        } else
            alertString = "该课程已结束报名";
    } else
        alertString = "未知课程";
%>
<script>
    alert('<%= alertString%>')
    window.history.back();
</script>