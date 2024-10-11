package csj.news;

import csj.utils.Console;
import weaver.conn.RecordSet;
import weaver.interfaces.schedule.BaseCronJob;

/**
 * @Author: 张骏山
 * @Date: 2024/10/11 18:38
 * @PackageName: csj.news
 * @ClassName: UpdateViewCounts
 * @Description: 更新阅读量
 * @Version: 1.0
 **/
public class UpdateViewCounts extends BaseCronJob {
    @Override
    public void execute() {
        RecordSet recordSet = new RecordSet();
        String news = "update uf_xw set cycs = ifnull(c,0) + llydl from  (select docid,count(id) as c from docdetaillog GROUP BY docid) as c where c.docid = uf_xw.xgwd";

        String principle = "update uf_gzzdwjb set ckcs = ifnull(c,0)  + llydl from (select docid,count(id) as c from docdetaillog GROUP BY docid) as c where c.docid = uf_gzzdwjb.xgwd";
        recordSet.execute(news);
        recordSet.execute(principle);
        Console.log("执行完成=>");
        Console.log(news);
        Console.log(principle);
    }
}
