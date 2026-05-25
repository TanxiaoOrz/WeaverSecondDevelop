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
public class CSJSuppliersCornJobNew extends BaseCronJob {

    private String supplierTable;
    private String month;
    private String day;

    @Override
    public void execute() {
        RecordSet rs = new RecordSet();
        Date date = new Date();
        if (supplierTable == null || supplierTable.isEmpty()) {
            supplierTable = "uf_gysxx";
        }
        Console.log("SUPPLIER_TABLE => " + supplierTable);
        Console.log("CSJ year supplier review asked month => " + month + " asked day => day " + day);
        if (date.getDate() == Integer.parseInt(day) && date.getMonth() +1 == Integer.parseInt(month)) {
            Console.log("Today is asked Day start");
            SupplierYearReviewNew csj = new SupplierYearReviewNew(1, date.getYear(), supplierTable);
            SupplierYearReviewNew xc = new SupplierYearReviewNew(3,date.getYear(), supplierTable);
            int successCount = csj.createRequests() + xc.createRequests();
            Console.log("End with SuccessCount => " + successCount);
        } else {
            Console.log("Today isn't asked Day End");
        }
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getSupplierTable() {
        return supplierTable;
    }

    public void setSupplierTable(String supplierTable) {
        this.supplierTable = supplierTable;
    }
}
