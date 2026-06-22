<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%@ page import="weaver.general.Util,weaver.hrm.HrmUserVarify" %>
<%@ page import="java.util.Date,java.text.SimpleDateFormat" %>
<jsp:useBean id="rs" class="weaver.conn.RecordSet" scope="page"/>
<%
    try {
        String requestid = Util.null2String(request.getParameter("requestid"));
        if (!requestid.isEmpty()) {
            String newBDS = Util.null2String(request.getParameter("bds"));
            String newBDSName = Util.null2String(request.getParameter("name"));
            String getCurrentFt = "select currentft from bjmonitor where requestid = " + requestid;
            rs.execute(getCurrentFt);
            String currentFt = "";
            if (rs.next()) {
                currentFt = Util.null2String(rs.getString("currentft"));
            }
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

            // 原有bds人员ID
            String getOriginalBDSSql = "select bdsspry from "+ currentFt +" where requestid = " + requestid;
            rs.execute(getOriginalBDSSql);
            String lastbds = "";
            if (rs.next()) {
                lastbds = Util.null2String(rs.getString("bdsspry"));
            }

            // 原有bds人员姓名 —— 这里已修复
            String lastbdsname = "";
            if (!lastbds.isEmpty()) {
                String getOriginalBDSNameSql = "SELECT " +
                        "STUFF((" +
                        "SELECT ',' + lastname " +
                        "FROM hrmresource " +
                        "WHERE id IN (" + lastbds + ") " +  // 关键修复：加括号
                        "FOR XML PATH(''), TYPE " +
                        ").value('.', 'NVARCHAR(MAX)'), 1, 1, '') AS lastnames";
                rs.execute(getOriginalBDSNameSql);
                if (rs.next()) {
                    lastbdsname = Util.null2String(rs.getString("lastnames"));
                }
            }

            // 时间拼接日志
            Date now = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String dateTime = sdf.format(now);
            String newRecord = lastname + " 在 " + dateTime + " 修改了BDS人员，由【" + lastbdsname + "】修改为【" + newBDSName + "】";

            // 更新业务字段
            String updateBDSSql = "UPDATE "+ currentFt +" set bdsspry = '" + newBDS + "' WHERE requestid = " + requestid;
            // 更新日志
            String updateLogSql = "UPDATE "+ currentFt +" SET bgjl = ISNULL(bgjl, '') + '\n' + '" + newRecord + "' WHERE requestid = " + requestid;

            rs.execute(updateBDSSql);
            rs.execute(updateLogSql);
            out.print("success");

        } else {
            out.print("fail：requestid 不能为空");
        }
    } catch (Exception e) {
        out.print("fail：" + e.getMessage());
        e.printStackTrace();
    }
%>