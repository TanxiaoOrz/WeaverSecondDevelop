package csj.news;

import csj.utils.Console;
import weaver.conn.RecordSet;
import weaver.interfaces.schedule.BaseCronJob;

/**
 * @Author: 张骏山
 * @Date: 2024/10/17 10:46
 * @PackageName: csj.news
 * @ClassName: DropOutNews
 * @Description: 废弃失效内容
 * @Version: 1.0
 */
public class DropOutNews extends BaseCronJob {
    @Override
    public void execute() {
        RecordSet recordSet = new RecordSet();
        String newsDoc = "update docdetail set docstatus = 7 where id in (\n" +
                "select xgwd from uf_xw where xxyxrq is not null and TO_DATE(xxyxrq) < CURRENT_DATE\n" +
                ")";
        String news = "update uf_xw set wdzt = 40 where xxyxrq is not null and TO_DATE(xxyxrq) < CURRENT_DATE\n";
        String principleDoc = "update docdetail set docstatus = 7 where id in (\n" +
                "select xgwd from uf_gzzdwjb where gqsj is not null and TO_DATE(gqsj) < CURRENT_DATE\n" +
                ")";
        String principle = "update uf_gzzdwjb set sfsx =  1 where gqsj is not null and TO_DATE(gqsj) < CURRENT_DATE";
        recordSet.execute(news);
        recordSet.execute(newsDoc);
        recordSet.execute(principle);
        recordSet.execute(principleDoc);
        Console.log("执行完成=>");
        Console.log(news);
        Console.log(newsDoc);
        Console.log(principle);
        Console.log(principleDoc);
    }
}
