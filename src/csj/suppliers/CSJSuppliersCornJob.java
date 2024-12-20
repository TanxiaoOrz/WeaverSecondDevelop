package csj.suppliers;

import csj.utils.Console;
import weaver.conn.RecordSet;
import weaver.interfaces.schedule.BaseCronJob;

import java.util.Date;

/**
 * @Author: 张骏山
 * @Date: 2024/11/30 22:39
 * @PackageName: csj.suppliers
 * @ClassName: CSJSuppliersCornJob
 * @Description: 长三角年度评审校验触发
 * @Version: 1.0
 */
public class CSJSuppliersCornJob extends BaseCronJob {
    @Override
    public void execute() {
        RecordSet rs = new RecordSet();
        String month = rs.getPropValue("supplierYearReview", "month");
        String day = rs.getPropValue("supplierYearReview", "day");
        Date date = new Date();
        Console.log("CSJ year supplier review asked month => " + month + " asked day => day " + day);
        if (date.getDate() == Integer.parseInt(day) && date.getMonth() +1 == Integer.parseInt(month)) {
            Console.log("Today is asked Day start");
            SupplierYearReview csj = new SupplierYearReview(1, date.getYear());
            SupplierYearReview xc = new SupplierYearReview(3,date.getYear());
            int successCount = csj.createRequests() + xc.createRequests();
            Console.log("End with SuccessCount => " + successCount);
        } else {
            Console.log("Today isn't asked Day End");
        }

    }
}
