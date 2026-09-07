<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%@ page import="weaver.general.Util" %>
<%@ page import="weaver.hrm.HrmUserVarify" %>
<%@ page import="csj.E10.QueryTodoContractCount" %>
<%@ page import="csj.treas_off.GetTreasTodoSize" %>
<%@ page import="net.minidev.json.JSONArray" %>
<%@ page import="net.minidev.json.JSONObject" %>

<jsp:useBean id="rs" class="weaver.conn.RecordSet" scope="page" />
<%
    // 获取当前登录用户id
    int userId = HrmUserVarify.getUser(request, response).getUID();
    // 查询工号
    String workcode = "";
    String sql = "select workcode from hrmresource where id = " + userId;
    rs.execute(sql);
    if (rs.next()) {
        workcode = Util.null2String(rs.getString("workcode"));
    }

    // 调用两个接口: 待履约合同数量 + 司库待办总条数
    String todoCount = Util.null2String(new QueryTodoContractCount().queryTodoCount(workcode));
    String todoSize = Util.null2String(new GetTreasTodoSize().getTodoSize(workcode));

    // 组装json对象数组返回, 每个接口对应一个对象, href为各自固定值
    // 数量为0时不返回该行
    JSONArray array = new JSONArray();

    // 接口1: 待履约合同数量(合同)
    if (!todoCount.isEmpty() && !"0".equals(todoCount)) {
        JSONObject obj1 = new JSONObject();
        obj1.put("viewString", "合同履约提醒:您当前仍有" + todoCount + "个合同未履约完成,请尽快处理");
        obj1.put("href", "/csj/E10/SingleLoginE10V2.jsp?code=Contract_SIGN_&redirectUrl=%2febdapp%2fview%2f1289103832496308230%2fVIEWPORT%2f1296432721540562948-8138572677550216890%3fcusMenuId%3d8138572677550216890%26urlPageTitle%3d5b6F6K%252BE5Lu35ZCI5ZCM%26cusview%3d0");
        array.add(obj1);
    }

    // 接口2: 司库待办总条数(司库)
    if (!todoSize.isEmpty() && !"0".equals(todoSize)) {
        JSONObject obj2 = new JSONObject();
        obj2.put("viewString", "司库待办提醒:您当前仍有" + todoSize + "个待办事项待处理");
        obj2.put("href", "/csj/Treas/SingleLoginTreas.jsp");
        array.add(obj2);
    }

    response.getWriter().println(array.toString());
%>
