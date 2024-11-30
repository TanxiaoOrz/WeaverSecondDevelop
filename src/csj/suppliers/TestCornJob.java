package csj.suppliers;

import weaver.interfaces.schedule.BaseCronJob;

/**
 * @Author: 张骏山
 * @Date: 2024/11/30 22:41
 * @PackageName: csj.suppliers
 * @ClassName: TestCornJob
 * @Description: 测试类
 * @Version: 1.0
 */
public class TestCornJob extends BaseCronJob {
    @Override
    public void execute() {
        SupplierYearReview supplierYearReview = new SupplierYearReview(1, 2024);
        int successCount = supplierYearReview.createRequests();
    }
}
