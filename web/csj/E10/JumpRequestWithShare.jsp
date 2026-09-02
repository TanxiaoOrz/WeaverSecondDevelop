<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%@ page import="weaver.general.Util"%>
<%@ page import="weaver.hrm.HrmUserVarify" %>
<%@ page import="org.json.JSONObject" %>
<%@ page import="okhttp3.MediaType" %>
<%@ page import="okhttp3.OkHttpClient" %>
<%@ page import="okhttp3.Request" %>
<%@ page import="okhttp3.RequestBody" %>
<%@ page import="okhttp3.Response" %>
<jsp:useBean id="rs" class="weaver.conn.RecordSet" scope="page" />
<%
//    调用 /api/workflow/share/addshare 将流程共享给当前登录用户,输出接口返回信息,由调用方决定后续跳转
//    调用方跳转页面: /spa/workflow/static4form/index.html#/main/workflow/req?requestid=${requestId}

    int userId = HrmUserVarify.getUser(request, response).getUID();
    int requestId = Util.getIntValue(Util.null2String(request.getParameter("requestId")));
    if (requestId <= 0) {
        out.println("requestId不能为空");
        return;
    }

    // 查询wfid
    int wfid = 0;
    rs.execute("select workflowid from workflow_requestbase where requestid = " + requestId);
    if (rs.next()) {
        wfid = rs.getInt("workflowid");
    }

    // 组装共享参数
    JSONObject params = new JSONObject();
    params.put("userid", userId);
    params.put("requestid", requestId);
    params.put("wfid", wfid);
    params.put("permissiontype", "5");
    params.put("iscanread", "1");

    // 组装接口地址
    String contextPath = Util.null2String(request.getContextPath());
    int port = request.getServerPort();
    String apiUrl = request.getScheme() + "://" + request.getServerName() + (port == 80 ? "" : ":" + port) + contextPath + "/api/workflow/share/addshare";

    try {
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        MediaType mediaType = MediaType.parse("application/json; charset=UTF-8");
        RequestBody body = RequestBody.create(mediaType, params.toString());
        Request httpRequest = new Request.Builder()
                .url(apiUrl)
                .method("POST", body)
                .addHeader("Content-Type", "application/json; charset=UTF-8")
                // 携带浏览器会话cookie,保证接口以当前登录用户身份执行
                .addHeader("Cookie", Util.null2String(request.getHeader("Cookie")))
                .build();
        Response httpResponse = client.newCall(httpRequest).execute();
        String rtnStr = httpResponse.body().string();
        out.println(rtnStr);
    } catch (Exception e) {
        out.println("共享接口调用失败:" + e.getMessage());
    }
%>