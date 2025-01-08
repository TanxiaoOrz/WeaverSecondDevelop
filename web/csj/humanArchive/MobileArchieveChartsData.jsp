<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%@ page import="weaver.general.Util" %>
<%@ page import="net.minidev.json.JSONObject" %>
<%@ page import="csj.utils.Console" %>
<%@ page import="net.minidev.json.JSONArray" %>
<%@ page import="weaver.hrm.HrmUserVarify" %>

<jsp:useBean id="rs" class="weaver.conn.RecordSet" scope="page"/>
<%
    int userId = HrmUserVarify.getUser(request, response).getUID();
    String sqlCondition ;
    rs.execute("select h.id, subcompanyid1 , ckgs from hrmresource as h  LEFT JOIN uf_ArchCharts4Mob as n on h.subcompanyid1 = n.ssgs where (ckqb is null or ckqb = 0 or ssgs is null) and h.id =" + userId);
    Console.log("select h.id, subcompanyid1 , ckgs from hrmresource as h  LEFT JOIN uf_ArchCharts4Mob as n on h.subcompanyid1 = n.ssgs where (ckqb is null or ckqb = 0 or ssgs is null) and h.id =" + userId);
    if (rs.next()) {
        String cksg = Util.null2String(rs.getString("ckgs"));
        String subcompanyid1 = Util.null2String(rs.getString("subcompanyid1"));
        if (cksg.equals(""))
            sqlCondition = " ssgs = " + subcompanyid1;
        else
            sqlCondition = " ssgs in (" + cksg + ")";
    } else
        sqlCondition = " 1 = 1";

    JSONObject con = new JSONObject();


    {
        String[][] CompositionDataStatusMatrix = {
                {"试用", "#e0e0e0"},
                {"正式", "#bdbdbd"},
                {"临时", "#f44336"},
                {"实习", "#ff9800"},
                {"解聘", "#e91e63"},
                {"离职", "#2196f3"},
                {"退休", "#4caf50"},
                {"无效", "#ffeb3b"}
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
            object.put("count", count);
            array.add(object);
        }
        con.put("employeeCompositionData", array);
    }

    {
        int[][] ageOptionsMatrix = {
                {0, 25},
                {25, 35},
                {35, 45},
                {45, 55},
                {55, 100}
        };
        JSONArray array = new JSONArray();
        for (int i = 0; i < ageOptionsMatrix.length; i++) {
            String queryAgeGroupCountDataSql = "select count(1) as data from uf_rsda where " + sqlCondition + " and age(TO_DATE(csrq),CURRENT_DATE) > " + ageOptionsMatrix[i][0] + " and age(TO_DATE(csrq),CURRENT_DATE) < " + ageOptionsMatrix[i][1];
            Console.log("queryAgeGroupCountDataSql[" + i + "] = " + queryAgeGroupCountDataSql);
            rs.execute(queryAgeGroupCountDataSql);
            if (rs.next())
                array.add(Util.getIntValue(Util.null2String(rs.getString("data"))));
        }
        String queryAgeGroupCountDataSql = "select count(1) as data from uf_rsda where " + sqlCondition + " csrq is null";
        Console.log("queryAgeGroupCountDataSql[null] = " + queryAgeGroupCountDataSql);
        rs.execute(queryAgeGroupCountDataSql);
        if (rs.next())
            array.add(Util.getIntValue(Util.null2String(rs.getString("data"))));
        con.put("ageOptionsData", array);
    }
    {
        JSONArray educationOptionsSeriesData = new JSONArray();
        JSONArray educationOptionsSxAxisData = new JSONArray();

        String queryEducationOptionsSql = "select ifnull(xl,'未知') as xl, count(1) as count from uf_rsda where " + sqlCondition + " GROUP BY xl";
        Console.log("queryEducationOptionsSql = " + queryEducationOptionsSql);
        rs.execute(queryEducationOptionsSql);
        while (rs.next()) {
            educationOptionsSxAxisData.add(Util.null2String(rs.getString("xl")));
            educationOptionsSeriesData.add(Util.getIntValue(Util.null2String(rs.getString("count"))));
        }
        con.put("educationOptionsSeriesData", educationOptionsSeriesData);
        con.put("educationOptionsSxAxisData", educationOptionsSxAxisData);
    }
    try {
        {
            JSONArray genderOptionsData = new JSONArray();
            String[] genderNameStringMatrix = {
                    "男性",
                    "女性",
                    "未知"
            };
            String queryGenderOptionSql = "select ifnull(xb,2) xb, count(1) count from uf_rsda where " + sqlCondition + " GROUP BY xb";
            Console.log("queryGenderOptionSql = " + queryGenderOptionSql);
            rs.execute(queryGenderOptionSql);
            while (rs.next()) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("value", Util.getIntValue(Util.null2String(rs.getString("count"))));
                jsonObject.put("name", genderNameStringMatrix[Util.getIntValue(Util.null2String(rs.getString("xb")))]);
                genderOptionsData.add(jsonObject);
            }
            con.put("genderOptionsData", genderOptionsData);
        }
        {
            JSONArray maritalStatusOptionsData = new JSONArray();
            String[] maritalStatusNameStringMatrix = {
                    "已婚已育", "已婚未育", "未婚", "其他", "已婚", "未知"
            };
            String queryMaritalStatusOptionSql = "select case when zzmm = 5 then 3 else ifnull(zzmm , 5) end  as hyzk, count(1) count from uf_rsda where " + sqlCondition + " GROUP BY zzmm";
            Console.log("queryMaritalStatusOptionSql = " + queryMaritalStatusOptionSql);
            rs.execute(queryMaritalStatusOptionSql);
            while (rs.next()) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("value", Util.getIntValue(Util.null2String(rs.getString("count"))));
                jsonObject.put("name", maritalStatusNameStringMatrix[Util.getIntValue(Util.null2String(rs.getString("hyzk")))]);
                maritalStatusOptionsData.add(jsonObject);
            }
            con.put("maritalStatusData", maritalStatusOptionsData);
        }
    } catch (Exception e) {
        Console.log(e.getMessage());
        Console.log(e.getClass().toString());
    }
    {
        int[][] companyAgeOptionsMatrix = {
                {0, 3},
                {3, 5},
                {5, 10},
                {10, 15},
                {15, 100}
        };
        JSONArray array = new JSONArray();
        for (int i = 0; i < companyAgeOptionsMatrix.length; i++) {
            String queryCompanyAgeGroupCountDataSql = "select count(1) as data from uf_rsda where " + sqlCondition + " and age(TO_DATE(dbdwrq),CURRENT_DATE) > " + companyAgeOptionsMatrix[i][0] + " and age(TO_DATE(dbdwrq),CURRENT_DATE) < " + companyAgeOptionsMatrix[i][1];
            Console.log("queryCompaneAgeGroupCountDataSql[" + i + "] = " + queryCompanyAgeGroupCountDataSql);
            rs.execute(queryCompanyAgeGroupCountDataSql);
            if (rs.next())
                array.add(Util.getIntValue(Util.null2String(rs.getString("data"))));
        }
        String queryAgeGroupCountDataSql = "select count(1) as data from uf_rsda where " + sqlCondition + " csrq is null";
        Console.log("queryAgeGroupCountDataSql[null] = " + queryAgeGroupCountDataSql);
        rs.execute(queryAgeGroupCountDataSql);
        if (rs.next())
            array.add(Util.getIntValue(Util.null2String(rs.getString("data"))));
        con.put("companyAgeOptionsData", array);
    }
    {
        String [][] rankOptionMatrix = {
                {"like '%,4,%'","初级"},
                {"like '%,5,%'","中级"},
                {"like '%,6,%'","高级"},
                {"not like '%,4,%' and not like '%,5,%' and not like '%,6,%'","其它"},
        };
        JSONArray rankOptionMatrixData = new JSONArray();
        for (int i = 0; i < rankOptionMatrix.length; i++) {
            String rankOptionDataSql = "select count(1) as count from uf_rsda where "+sqlCondition+"and ','||modelableid||',' " + rankOptionMatrix[i][0];
            System.out.println("rankOptionDataSql["+i+"] = " + rankOptionDataSql);
            rs.execute(rankOptionDataSql);
            if (rs.next()) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("value", Util.getIntValue(Util.null2String(rs.getString("count"))));
                jsonObject.put("name", rankOptionMatrix[i][1]);
                rankOptionMatrixData.add(jsonObject);
            }
        }
        con.put("rankOptionMatrixData",rankOptionMatrixData);

    }
    {
        String [][] rankOptionMatrix = {
                {"like '%,4,%'","初级"},
                {"like '%,5,%'","中级"},
                {"like '%,6,%'","高级"},
                {"not like '%,4,%' and not like '%,5,%' and not like '%,6,%'","其它"},
        };
        JSONArray rankOptionMatrixData = new JSONArray();
        for (int i = 0; i < rankOptionMatrix.length; i++) {
            String rankOptionDataSql = "select count(1) as count from uf_rsda where "+sqlCondition+"and ','||modelableid||',' " + rankOptionMatrix[i][0];
            System.out.println("rankOptionDataSql["+i+"] = " + rankOptionDataSql);
            rs.execute(rankOptionDataSql);
            if (rs.next()) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("value", Util.getIntValue(Util.null2String(rs.getString("count"))));
                jsonObject.put("name", rankOptionMatrix[i][1]);
                rankOptionMatrixData.add(jsonObject);
            }
        }
        con.put("rankOptionMatrixData",rankOptionMatrixData);
    }
    {
        String [][] gradeOptionMatrix = {
                {"like '%,7,%'","领导班子"},
                {"like '%,8,%'","部门正职"},
                {"like '%,9,%'","部门副职"},
                {"not like '%,4,%' and not like '%,5,%' and not like '%,6,%'","其它"},
        };
        JSONArray rankOptionMatrixData = new JSONArray();
        for (int i = 0; i < gradeOptionMatrix.length; i++) {
            String rankOptionDataSql = "select count(1) as count from uf_rsda where "+sqlCondition+"and ','||modelableid||',' " + gradeOptionMatrix[i][0];
            System.out.println("gradeOptionMatrix["+i+"] = " + rankOptionDataSql);
            rs.execute(rankOptionDataSql);
            if (rs.next()) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("value", Util.getIntValue(Util.null2String(rs.getString("count"))));
                jsonObject.put("name", gradeOptionMatrix[i][1]);
                rankOptionMatrixData.add(jsonObject);
            }
        }
        con.put("gradeOptionMatrix",rankOptionMatrixData);
    }
    response.getWriter().println(con.toString());
%>

