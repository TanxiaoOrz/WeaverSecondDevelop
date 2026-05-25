<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%@ page import="weaver.general.Util,weaver.hrm.HrmUserVarify" %>
<%@ page import="java.util.Date,java.text.SimpleDateFormat" %>
<jsp:useBean id="rs" class="weaver.conn.RecordSet" scope="page"/>
<%
    try {
        String requestid = Util.null2String(request.getParameter("requestid"));
        if (!requestid.isEmpty()) {
            String newBD = Util.null2String(request.getParameter("bd"));
            String newBDName = Util.null2String(request.getParameter("name"));

            // 当前登录人ID姓名
            int userId = HrmUserVarify.getUser(request, response).getUID();
            String lastname = "";
            if(userId == 1){
                lastname= "系统管理员";
            }else{
                String getCurrentUserNameSql = "select lastname from hrmresource where id = " + userId;
                rs.execute(getCurrentUserNameSql);
                if (rs.next()) {
                    lastname = Util.null2String(rs.getString("lastname"));
                }
            }


            // 原有bd人员ID
            String getOriginalBDSql = "select bdspr from formtable_main_427 where requestid = " + requestid;
            rs.execute(getOriginalBDSql);
            String lastbd = "";
            if (rs.next()) {
                lastbd = Util.null2String(rs.getString("bdspr"));
            }

            // 原有bd人员姓名
            String lastbdname = "";
            if (!lastbd.isEmpty()) {
                String getOriginalBDNameSql = "select lastname from hrmresource where id = " + lastbd;
                rs.execute(getOriginalBDNameSql);
                if (rs.next()) {
                    lastbdname = Util.null2String(rs.getString("lastname"));
                }
            }

            // 时间拼接日志
            Date now = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String dateTime = sdf.format(now);
            String newRecord = lastname + " 在 " + dateTime + "修改了bd人员 由" + lastbdname + "修改为" + newBDName;

            // 更新业务字段
            String updateBDSql = "UPDATE formtable_main_427 set bdspr = '" + newBD + "' WHERE requestid = " + requestid;
            // 更新日志
            String updateLogSql = "UPDATE formtable_main_427 SET bgjl = CONCAT(bgjl, '\n', '" + newRecord + "') WHERE requestid = " + requestid;

            rs.execute(updateBDSql);
            rs.execute(updateLogSql);
            out.print("success");

        } else {
            out.print("fail：requestid 不能为空");
        }
    } catch (Exception e) {
        out.print("fail：" + e.getMessage());
//        Console.log(e.getMessage());
    }
%>