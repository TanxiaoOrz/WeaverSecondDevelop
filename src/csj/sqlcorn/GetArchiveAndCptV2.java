package csj.sqlcorn;

import weaver.conn.RecordSet;
import weaver.interfaces.schedule.BaseCronJob;

/**
 * @Author: 张骏山
 * @Date: 2025/1/10 13:47
 * @PackageName: csj.sqlcorn
 * @ClassName: GetArchiveAndCptv2
 * @Description: 处理档案挂接,资产新建日期
 * @Version: 1.0
 **/
public class GetArchiveAndCptV2 extends BaseCronJob {
    @Override
    public void execute() {
        RecordSet recordSet = new RecordSet();
        String cpt = "update cptcapital set cjrqsj = concat(createdate,' ',createtime) where isdata = 2 and cjrqsj isnull";
        String arch = "update uf_rsda set ry = hrmresource.id from  hrmresource where hrmresource.loginid = uf_rsda.xtdlm and ry is null\n";
        System.out.println("cpt = " + cpt);
        System.out.println("arch = " + arch);
        recordSet.execute(cpt);
        recordSet.execute(arch);
    }
}
