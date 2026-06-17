package rundo.quote;

import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.Property;
import weaver.soa.workflow.request.RequestInfo;

import java.util.Arrays;

@SuppressWarnings("unused")
public class ApproverWithdrawAction implements weaver.interfaces.workflow.action.Action {

    String subFormName;
    String aimNodeId;
    String currentReferFileIdsColumn;
    String BDSColumn;
    String judgeNode;
    String recordFormName;
    String submittedFileColumns;
    String submittedFileIdsColumn;


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
            //noinspection OptionalGetWithoutIsPresent
            String submittedFileIds = Arrays.stream(mainEntries).filter(mainEntry -> mainEntry.getName().equals(submittedFileIdsColumn)).findFirst().get().getValue();

            QuoteRequestWithdrawCmd withdrawCmd = new QuoteRequestWithdrawCmd(subFormName,
                    Integer.parseInt(aimNodeId),
                    Integer.parseInt(requestId),
                    currentReferFileIds,
                    BDSs);
            if (withdrawCmd.isAllSubmitted()) {
                requestInfo.getRequestManager().setMessagecontent("无法撤回,已全部提交,请联系bds重新分发");
                return Action.SUCCESS;
            }
            if (!withdrawCmd.withDrawSubRequest(submittedFileIds, recordFormName, submittedFileColumns)) {
                requestInfo.getRequestManager().setMessagecontent("撤回审核记录失败,请联系管理员");
                return Action.FAILURE_AND_CONTINUE;
            }
        }
        return Action.SUCCESS;

    }
}