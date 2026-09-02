<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%@ page import="weaver.general.Util" %>
<%@ page import="net.minidev.json.JSONObject" %>
<%@ page import="rundo.util.Console" %>
<%@ page import="java.io.StringWriter,java.io.PrintWriter" %>
<jsp:useBean id="rs" class="weaver.conn.RecordSet" scope="page" />

<%
    try {
        int mainRequestId = Util.getIntValue(Util.null2String(request.getParameter("mainRequestId")));

        boolean hasSub = false;
        String currentOperators = "";

        if (mainRequestId > 0) {
            // 判断主流程是否存在子流程
            rs.execute("select count(*) as cnt from workflow_requestbase where mainRequestId = " + mainRequestId);
            if (rs.next()) {
                hasSub = rs.getInt("cnt") > 0;
            }

            // 查询子流程当前未操作者
            String currentOperatorSql = "SELECT STUFF((SELECT ' ' + lastname FROM workflow_requestbase b "
                    + "INNER JOIN WORKFLOW_CURRENTOPERATOR c ON b.requestid = c.requestid "
                    + "INNER JOIN hrmresource h ON c.userid = h.id "
                    + "WHERE b.mainRequestId = " + mainRequestId + " AND c.isremark = 0 "
                    + "FOR XML PATH('')), 1, 1, '')";
            rs.execute(currentOperatorSql);
            if (rs.next()) {
                currentOperators = Util.null2String(rs.getString(1));
            }
        }

        JSONObject json = new JSONObject();
        json.put("hasSub", hasSub);
        json.put("currentOperators", currentOperators);
        out.print(json.toString());
    } catch (Exception e) {
        // 记录完整异常堆栈（含 Caused by 链）
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        String stackTrace = sw.toString();
        Console.log(stackTrace);
        out.print(stackTrace);
    }
%>