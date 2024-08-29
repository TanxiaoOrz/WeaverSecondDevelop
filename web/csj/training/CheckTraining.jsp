<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%@ page import="weaver.general.Util"%>
<%@ page import="weaver.hrm.HrmUserVarify" %>
<%@ page import="java.util.Date" %>
<%@ page import="tebie.applib.api.L" %>

<jsp:useBean id="rs" class="weaver.conn.RecordSet" scope="page" />
<%
    int billId = Util.getIntValue(Util.null2String(request.getParameter("billId")));
    Long timeStamp = new Long(Util.null2String(request.getParameter("timestamp")));
    Long now = new Date().getTime();
    String alertString;
    if (now < timeStamp || now - timeStamp > 1000 * 10) {
        alertString = "二维码已过期";
    } else {
        int userId = HrmUserVarify.getUser(request, response).getUID();

        rs.execute("select kczt from uf_pxhy where id = '"+billId+"'");
        if (rs.next()) {    // 课程是否存在
            int kczt = rs.getInt("kczt");
            if (kczt == 2) {    // 培训中
                String sql = "select qdzt as c from uf_pxqdmx where pxry = '"+userId+"' and pxkc = '"+ billId +"' and pxrq = concat(CURRENT_DATE,'')";
                rs.execute(sql);
                if (rs.next()) {    //是否已报名且签到
                    int counts = rs.getInt("c");
                    if (counts == 1) {
                        int hours = new Date().getHours();
                        int sj;
                        if (hours>=12)
                            sj = 1;
                        else
                            sj = 0;
                        rs.execute("update uf_pxqdmx set qdzt = 2 where pxry = '"+userId+"' and pxkc = '"+ billId +"' and pxrq = concat(CURRENT_DATE,'') and sj = " + sj);

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
    }


%>
<script>
    alert('<%= alertString%>')
    window.close()
</script>