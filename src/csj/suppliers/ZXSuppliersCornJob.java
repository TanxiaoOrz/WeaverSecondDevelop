package csj.suppliers;

import csj.utils.Console;
import weaver.conn.RecordSet;
import weaver.interfaces.schedule.BaseCronJob;

import java.util.Date;

/**
 * @Author: 张骏山
 * @Date: 2025/05/23 13:55
 * @PackageName: csj.suppliers
 * @ClassName: ZXSuppliersCornJob
 * @Description: 赵巷年度评审校验触发
 * @Version: 1.0
 */
public class ZXSuppliersCornJob extends BaseCronJob {
    @Override
    public void execute() {
        RecordSet rs = new RecordSet();
        String month = rs.getPropValue("supplierYearReview", "ZXmonth");
        String day = rs.getPropValue("supplierYearReview", "ZXday");
        Date date = new Date();
        Console.log("CSJ year supplier review asked month => " + month + " asked day => day " + day);
        if (date.getDate() == Integer.parseInt(day) && date.getMonth() +1 == Integer.parseInt(month)) {
            Console.log("Today is asked Day start");
            SupplierYearReview zx = new SupplierYearReview(10, date.getYear());
            int successCount = zx.createRequests();
            Console.log("End with SuccessCount => " + successCount);
        } else {
            Console.log("Today isn't asked Day End");
        }
    }
}
