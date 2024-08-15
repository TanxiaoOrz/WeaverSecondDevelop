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
        if (kczt == 2) {    // 培训中
            String sql = "select sfqd as c from uf_pxhy_dt1 where bmchry = '"+userId+"' and mainid = '"+ billId +"'";
            rs.execute(sql);
            if (rs.next()) {    //是否已报名
                int counts = rs.getInt("c");
                if (counts == 1) {
                    rs.execute("update uf_pxhy_dt1 sfqd = 2 where mainid = '" + billId + "' and bmchry = '" +  userId +"'");

                    alertString = "签到成功";
                } else {
                    alertString = "您已签到";
                }
            } else
                alertString = "您未报名";
        } else
            alertString = "非签到时间";
    } else
        alertString = "未知课程";
%>
<script>
    alert('<%= alertString%>')
    window.close()
</script>