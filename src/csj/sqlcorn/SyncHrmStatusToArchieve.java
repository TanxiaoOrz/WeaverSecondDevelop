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
public class SyncHrmStatusToArchieve extends BaseCronJob {
    @Override
    public void execute() {
        RecordSet recordSet = new RecordSet();
        String arch = "update uf_rsda set rzygzt = hrmresource.status from hrmresource where uf_rsda.id in (\n" +
                "select uf_rsda.id from hrmresource LEFT JOIN uf_rsda on hrmresource.id = uf_rsda.ry where ry is not null and status != rzygzt \n" +
                ") and uf_rsda.ry = hrmresource.id and hrmresource.id is not null";
        System.out.println("arch = " + arch);
        recordSet.execute(arch);
    }
}