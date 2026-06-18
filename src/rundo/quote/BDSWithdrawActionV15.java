package rundo.quote;

import rundo.util.Console;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.Property;
import weaver.soa.workflow.request.RequestInfo;

import java.util.Arrays;

@SuppressWarnings("unused")
public class BDSWithdrawActionV15 implements weaver.interfaces.workflow.action.Action {

    String subFormName;
    String currentReferFileIdsColumn;
    String judgeNode;


    @Override
    public String execute(RequestInfo requestInfo) {
        String mainRequestId = String.valueOf(requestInfo.getRequestid());
        int node = requestInfo.getRequestManager().getNodeid();
        Console.log("BDS强制撤回,询价单,当前节点" + node + "生效节点" + judgeNode);

        if (Integer.parseInt(judgeNode) == node) {

            Property[] mainEntries = requestInfo.getMainTableInfo().getProperty();

            //noinspection OptionalGetWithoutIsPresent
            String currentReferFileIds = Arrays.stream(mainEntries).filter(mainEntry -> mainEntry.getName().equals(currentReferFileIdsColumn)).findFirst().get().getValue();
//            QuoteRequestWithdrawCmdV16 withdrawCmd = QuoteRequestWithdrawCmdV16.getCachedMainRequestData(subFormName,
//                    Integer.parseInt(aimNodeId),
//                    Integer.parseInt(mainRequestId),
//                    currentReferFileIds,
//                    BDSs);

            QuoteRequestWithdrawCmdV16 withdrawCmd = new QuoteRequestWithdrawCmdV16(subFormName,
                    Integer.parseInt(mainRequestId),
                    currentReferFileIds
            );

            if (withdrawCmd.hasSubProcessSubmitted()) {
                requestInfo.getRequestManager().setMessagecontent("存在已填写分发审核流程,不允许撤回");
                Console.log("存在已填写分发审核流程,不允许撤回");
                return Action.FAILURE_AND_CONTINUE;
            }
            if (!withdrawCmd.withdrawMainRequest(requestInfo.getRequestManager().getUser().getUID())) {
                requestInfo.getRequestManager().setMessagecontent("清理分发审核流程失败,请联系管理员");
                Console.log("清理分发审核流程失败,请联系管理员");
                return Action.FAILURE_AND_CONTINUE;
            }
        }
        return Action.SUCCESS;

    }
}
