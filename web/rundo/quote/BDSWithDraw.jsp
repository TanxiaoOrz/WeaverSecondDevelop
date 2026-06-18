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

        if ("check".equals(type)) {
            boolean result = withdrawCmd.hasSubProcessSubmitted();
            out.print(result ? 0 : 1);
        } else if ("withdraw".equals(type)) {
            if (withdrawCmd.hasSubProcessSubmitted()) {
                out.print("当前存在子流程已填写提交,禁止撤回");
                return;
            } else if (withdrawCmd.withdrawMainRequest(userId)) {
                out.print(1);
            } else {
                out.print("撤回失败,请联系管理员");
            }
        } else {
            out.print("错误的请求参数");
            return;
        }
    } catch (Exception e) {
        Console.log(e.getMessage());
        out.print("执行异常,请联系管理员");
    }
%>
