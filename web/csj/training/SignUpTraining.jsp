<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%@ page import="weaver.general.Util"%>
<%@ page import="weaver.hrm.HrmUserVarify" %>

<jsp:useBean id="rs" class="weaver.conn.RecordSet" scope="page" />
<%
    int billId = Util.getIntValue(Util.null2String(request.getParameter("billId")));
    int userId = HrmUserVarify.getUser(request, response).getUID();
    String alertString;
    String sql = "select count(*) as c from uf_pxhy_dt1 where bmchry = '"+userId+"' and mainid =1 '"+ billId +"'";
    rs.execute(sql);
    if (rs.next()) {
        int counts = rs.getInt("c");
        if (counts == 0) {
            rs.execute("insert into uf_pxhy_dt1 (bmchry,sfqd,mainid) value (" + userId + ",0," + billId + ")");
            alertString = " 报名成功";
        } else {
            alertString = "您已报名";
        }
    } else
        alertString = "您已报名";

%>
<script>
    alert(<%= alertString%>)
    window.close()
</script>