package rundo.quote;

import rundo.util.Console;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.Property;
import weaver.soa.workflow.request.RequestInfo;

import java.util.Arrays;

@SuppressWarnings("unused")
public class ApproverWithdrawActionV16 implements weaver.interfaces.workflow.action.Action {

    String judgeNode;
    String recordFormName;


    @Override
    public String execute(RequestInfo requestInfo) {

        int node = requestInfo.getRequestManager().getNodeid();
        Console.log("审批人强制撤回,询价单,当前节点" + node + "生效节点" + judgeNode);
        if (Integer.parseInt(judgeNode) == node) {

            Property[] mainEntries = requestInfo.getMainTableInfo().getProperty();
            String subFormName = requestInfo.getRequestManager().getBillTableName();
            //noinspection OptionalGetWithoutIsPresent
            String currentReferFileIds = Arrays.stream(mainEntries).filter(mainEntry -> mainEntry.getName().equals("bjckwj")).findFirst().get().getValue();
            //noinspection OptionalGetWithoutIsPresent
            String submittedFileIds = Arrays.stream(mainEntries).filter(mainEntry -> mainEntry.getName().equals("txwcwj")).findFirst().get().getValue();
            //noinspection OptionalGetWithoutIsPresent
            String mainRequestId = Arrays.stream(mainEntries).filter(mainEntry -> mainEntry.getName().equals("zlcrequestid")).findFirst().get().getValue();
            QuoteRequestWithdrawCmdV16 withdrawCmd = new QuoteRequestWithdrawCmdV16(subFormName,
                    Integer.parseInt(mainRequestId),
                    currentReferFileIds
            );
            Console.log("信息:开始撤回" + ",subFormName" + subFormName + ",currentReferFileIds" + currentReferFileIds + ",mainRequestId" + mainRequestId);
            if (withdrawCmd.isAllSubmitted()) {
                requestInfo.getRequestManager().setMessagecontent("无法撤回,已全部提交,请联系bds重新分发");
                Console.log("信息:无法撤回,已全部提交,请联系bds重新分发");
                return Action.FAILURE_AND_CONTINUE;
            }
            if (!withdrawCmd.withDrawSubRequest(submittedFileIds, recordFormName)) {
                requestInfo.getRequestManager().setMessagecontent("撤回审核记录失败,请联系管理员");
                Console.log("错误:撤回审核记录失败,请联系管理员");
                return Action.FAILURE_AND_CONTINUE;
            }
        }
        return Action.SUCCESS;
    }
}
