<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%@ page import="weaver.general.Util"%>
<%@ page import="weaver.hrm.HrmUserVarify" %>
<jsp:useBean id="rs" class="weaver.conn.RecordSet" scope="page" />
<%
//    页面加载后由前端浏览器调用 /api/workflow/share/addshare 将流程共享给当前登录用户(不再由服务端OkHttp转发请求)
//    请求完成后不检验接口返回值,直接跳转到流程请求页面:
//    /spa/workflow/static4form/index.html#/main/workflow/req?requestid=${requestId}

    int userId = HrmUserVarify.getUser(request, response).getUID();
    int requestId = Util.getIntValue(Util.null2String(request.getParameter("requestId")));
    if (requestId <= 0) {
        out.println("requestId不能为空");
        return;
    }

    // 是否移动端(参数isMobile为1或true时跳转移动端链接)
    String isMobileParam = Util.null2String(request.getParameter("isMobile"));
    boolean isMobile = "1".equals(isMobileParam) || "true".equalsIgnoreCase(isMobileParam);

    // 查询wfid、currentnodeid
    int wfid = 0;
    int currentNodeid = 0;
    rs.execute("select workflowid, currentnodeid from workflow_requestbase where requestid = " + requestId);
    if (rs.next()) {
        wfid = rs.getInt("workflowid");
        currentNodeid = rs.getInt("currentnodeid");
    }

    // 组装接口地址(与页面同源,浏览器自动携带会话Cookie,保证以当前登录用户身份执行)
    String apiUrl = "/api/workflow/share/addshare";
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>流程共享</title>
</head>
<body>
<script>
    // 由前端浏览器发起共享请求(页面内原生XMLHttpRequest调用,未引用外部js)
    // 同步请求:接口返回后再决定是否跳转,避免请求未完成页面就已跳走
    (function () {
        var params = {
            userid: <%= userId %>,
            requestid: <%= requestId %>,
            wfid: <%= wfid %>,
            permissiontype: 5,
            iscanread: 1,
            currentnodeid: <%= currentNodeid %>,
            f_weaver_belongto_userid: 1,
            f_weaver_belongto_usertype: 0
        };
        // 与E10原生请求一致:POST表单提交,参数放请求体,Content-Type为application/x-www-form-urlencoded
        var bodyStr = "";
        for (var key in params) {
            if (bodyStr.length > 0) {
                bodyStr += "&";
            }
            bodyStr += key + "=" + params[key];
        }
        var xhr = new XMLHttpRequest();
        xhr.open("POST", "<%= apiUrl %>", false);
        xhr.setRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
        try {
            xhr.send(bodyStr);
        } catch (e) {
            // 请求发送异常也继续跳转,不阻塞
        }
        // 不检验接口返回值,请求完成后直接跳转到流程请求页面(移动端跳mobileform链接)
        var redirectUrl = <%= isMobile %>
            ? "/spa/workflow/static4mobileform/index.html?#/req?requestid=" + params.requestid
            : "/spa/workflow/static4form/index.html#/main/workflow/req?requestid=" + params.requestid;
        window.location.href = redirectUrl;
    })();
</script>
</body>
</html>