package rundo.quote;

import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.RequestInfo;

public class TestAction implements Action {
    @Override
    public String execute(RequestInfo requestInfo) {
        requestInfo.getRequestManager().setMessagecontent("测试123");
        requestInfo.getRequestManager().setMessage("ceshi34");
        return Action.FAILURE_AND_CONTINUE;
    }
}
