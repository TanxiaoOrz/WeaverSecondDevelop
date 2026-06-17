package rundo.quote;

import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.Property;
import weaver.soa.workflow.request.RequestInfo;

import java.util.Arrays;

@SuppressWarnings("unused")
public class BDSWithdrawAction implements weaver.interfaces.workflow.action.Action {

    String subFormName;
    String aimNodeId;
    String currentReferFileIdsColumn;
    String BDSColumn;
    String judgeNode;


    @Override
    public String execute(RequestInfo requestInfo) {
        String requestId = String.valueOf(requestInfo.getObjid());
        int node = requestInfo.getRequestManager().getNodeid();
        if (Integer.parseInt(judgeNode) == node) {

            Property[] mainEntries = requestInfo.getMainTableInfo().getProperty();

            //noinspection OptionalGetWithoutIsPresent
            String currentReferFileIds = Arrays.stream(mainEntries).filter(mainEntry -> mainEntry.getName().equals(currentReferFileIdsColumn)).findFirst().get().getValue();
            //noinspection OptionalGetWithoutIsPresent
            String BDSs = Arrays.stream(mainEntries).filter(mainEntry -> mainEntry.getName().equals(BDSColumn)).findFirst().get().getValue();
//            QuoteRequestWithdrawCmd withdrawCmd = QuoteRequestWithdrawCmd.getCachedMainRequestData(subFormName,
//                    Integer.parseInt(aimNodeId),
//                    Integer.parseInt(requestId),
//                    currentReferFileIds,
//                    BDSs);

            QuoteRequestWithdrawCmd withdrawCmd = new QuoteRequestWithdrawCmd(subFormName,
                    Integer.parseInt(aimNodeId),
                    Integer.parseInt(requestId),
                    currentReferFileIds,
                    BDSs);

            if (withdrawCmd.hasSubProcessSubmitted()) {
                requestInfo.getRequestManager().setMessagecontent("存在已填写分发审核流程,不允许撤回");
                return Action.FAILURE_AND_CONTINUE;
            }
            if (!withdrawCmd.withdrawMainRequest(requestInfo.getRequestManager().getUser().getUID())) {
                requestInfo.getRequestManager().setMessagecontent("清理分发审核流程失败,请联系管理员");
                return Action.FAILURE_AND_CONTINUE;
            }
        }
        return Action.SUCCESS;

    }
}
