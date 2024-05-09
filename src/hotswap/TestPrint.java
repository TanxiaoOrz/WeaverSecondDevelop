package hotswap;

import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.RequestInfo;

/**
 * @Author: 张骏山
 * @Date: 2024/4/7 17:36
 * @PackageName: hotswap
 * @ClassName: TestPrint
 * @Description: TODO
 * @Version: 1.0
 **/
public class TestPrint implements Action,VersionControl {

    String test = "cssg";


    @Override
    public String getVersion() {
        return test;
    }

    @Override
    public String execute(RequestInfo request) {
        Console.log(test);
        request.getRequestManager().setMessagecontent("异常："+test);
        return Action.FAILURE_AND_CONTINUE;
    }
}
