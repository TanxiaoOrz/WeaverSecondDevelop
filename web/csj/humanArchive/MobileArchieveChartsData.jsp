<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%@ page import="weaver.general.Util"%>
<%@ page import="weaver.conn.RecordSet" %>
<%@ page import="net.minidev.json.JSONObject" %>
<%@ page import="csj.utils.Console" %>
<%@ page import="net.minidev.json.JSONArray" %>

<jsp:useBean id="rs" class="weaver.conn.RecordSet" scope="page" />
<%
    String subcompanyid = Util.null2String(request.getParameter("subcompanyid"));
    String sqlCondition = " 1 = 1";
    if (subcompanyid.equals(""))
        sqlCondition = "ssgs = " + subcompanyid + " ";
    JSONObject con = new JSONObject();


    {
        String[][] CompositionDataStatusMatrix = {
                {"试用","#e0e0e0"},
                {"正式","#bdbdbd"},
                {"临时","#f44336"},
                {"实习","#ff9800"},
                {"解聘","#e91e63"},
                {"离职","#2196f3"},
                {"退休","#4caf50"},
                {"无效","#ffeb3b"}
        };
        JSONArray array = new JSONArray();
        String queryEmployeeCompositionDataSql = "select rzygzt as status, count(1) as count from uf_rsda where " + sqlCondition + "   GROUP BY rzygzt ";
        Console.log("queryEmployeeCompositionDataSql = " + queryEmployeeCompositionDataSql);
        rs.execute(queryEmployeeCompositionDataSql);
        while (rs.next()) {
            JSONObject object = new JSONObject();
            int status = Util.getIntValue(Util.null2String(rs.getString("status")));
            if (status < 0 || status > 7)
                continue;
            String count = Util.null2String(rs.getString("count"));
            object.put("status", CompositionDataStatusMatrix[status][0]);
            object.put("color", CompositionDataStatusMatrix[status][1]);
            object.put("count",count);
            array.add(object);
        }
        con.put("employeeCompositionData",array);
    }

    {
        int[][] ageOptionsMatrix = {
                {0,25},
                {25,35},
                {35,45},
                {45,55},
                {55,100}
        };
    }

    response.getWriter().println(con.toString());
%>

