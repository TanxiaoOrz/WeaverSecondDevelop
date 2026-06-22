<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%@ page import="weaver.general.Util,weaver.hrm.HrmUserVarify" %>
<%@ page import="rundo.util.Console" %>
<%@ page import="rundo.quote.*" %>
<%
    try {
        String type = Util.null2String(request.getParameter("type"));
        String subFormName = Util.null2String(request.getParameter("subFormName"));
        int aimNodeId = Util.getIntValue(Util.null2String(request.getParameter("aimNodeId")));

        int mainRequestId = Util.getIntValue(Util.null2String(request.getParameter("mainRequestId")));
        String currentReferFileIds = Util.null2String(request.getParameter("currentReferFileIds"));

        String BDSs = Util.null2String(request.getParameter("BDSs"));
        int userId = HrmUserVarify.getUser(request, response).getUID();

//        QuoteRequestWithdrawCmdV16 withdrawCmd = QuoteRequestWithdrawCmdV16.getCachedMainRequestData(subFormName,
//                aimNodeId,
//                mainRequestId,
//                currentReferFileIds,
//                BDSs);
        QuoteRequestWithdrawCmdV16 withdrawCmd = new QuoteRequestWithdrawCmdV16(subFormName, mainRequestId, currentReferFileIds);
        boolean result;
        if ("BDS".equals(type)) {
            result = withdrawCmd.hasSubProcessSubmitted();
            out.print(result ? 0 : 1);
        } else if ("Approval".equals(type)) {
            result = withdrawCmd.isAllSubmitted();
            out.print(result ? 1 : 0);
        } else {
            out.print("错误的请求参数,请联系管理员");
            return;
        }
    } catch (Exception e) {
        Console.log(e.getMessage());
        out.print("执行异常,请联系管理员");
    }
%>
