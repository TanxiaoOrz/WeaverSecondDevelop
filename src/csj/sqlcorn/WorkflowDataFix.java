package csj.sqlcorn;

import weaver.conn.RecordSet;
import weaver.interfaces.schedule.BaseCronJob;

/**
 * @Author: 张骏山
 * @Date: 2025/3/1 15:19
 * @PackageName: csj.sqlcorn
 * @ClassName: SyncHrmStatusToArchieve
 * @Description: 同步人员状态
 * @Version: 1.0
 */
public class WorkflowDataFix extends BaseCronJob {
    @Override
    public void execute() {
        RecordSet recordSet = new RecordSet();
        String arch = "delete from workflow_node_flowtime where nodeoperator =558 and requestid = 326190";
        String arch2 = "delete from workflow_node_fix_flowtime where nodeoperator =558 and requestid = 326190";
        System.out.println("arch = " + arch);
        System.out.println("arch2 = " + arch2);
        recordSet.execute(arch);
        recordSet.execute(arch2);
    }
}