package csj.suppliers;

import csj.utils.Console;
import weaver.conn.RecordSet;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @Author: 张骏山
 * @Date: 2024/11/30 18:41
 * @PackageName: csj.suppliers
 * @ClassName: SupplierYearReview
 * @Description: 供应商年度评审自动发起类
 * @Version: 1.0
 */
public class SupplierYearReview {
    /*
        CREATE TABLE sd_TriggerSupplierReview (
            id serial,
            creater integer,
            dept integer,
            year integer,
            counts integer,
            allcounts integer,
            company integer,
            orders integer,
            PRIMARY key (id)
        );
        CREATE TABLE sd_TriggerSupplierReview_dt1 (
            mainid integer,
            id serial,
            supplier integer,
            PRIMARY key (id)
        );
    */
    private static String MAIN_TRIGGER_TABLE = "sd_TriggerSupplierReview";
    private static String DETAIL_TRIGGER_TABLE = "sd_TriggerSupplierReview_dt1";
    private static String SUPPLIER_TABLE = "uf_gysxx";
    private static String MAX_DETAIL_COUNTS = "50";
    public int maxDetailCount = Integer.parseInt(MAX_DETAIL_COUNTS);

    int company;
    int year;

    RecordSet recordSet = new RecordSet();

    public SupplierYearReview(int company, int year) {
        this.company = company;
        this.year = year;
    }

    /**
     * 创建指定年份/公司的供应商年度评审
     *
     * @return 创建主表数量
     */
    public int createRequests() {
        int successCount = 0;
        int agentCount = 0;
        Console.log("Start Create Supplier Year Review Task with companyId = " + company);
        String agentQuerySql = String.format(
                "select jbr,count(1) as supplierCounts,cast((floor(count(1)/%d) +1) as INTEGER) as requestCounts  from %s where ssgs = %d and jbr is not null GROUP BY jbr",
                maxDetailCount, SUPPLIER_TABLE, company);
        recordSet.execute(agentQuerySql);
        Console.log("agentQuerySql => " + agentQuerySql);
        try {
            while (recordSet.next()) {
                AgentSupplier agentSupplier = new AgentSupplier(
                        recordSet.getInt("jbr"),
                        recordSet.getInt("supplierCounts"),
                        recordSet.getInt("requestCounts")
                );
                RecordSet agentRecordSet = new RecordSet();
                agentSupplier.setSuppliers(agentRecordSet);
                successCount += agentSupplier.createRequest(agentRecordSet);
                agentCount += 1;
            }
        } catch (Exception e) {
            Console.log(e.getMessage());
            Arrays.stream(e.getSuppressed()).forEach(throwable -> {
                        Console.log(throwable.getMessage());
                    }
            );
        }

        Console.log("Finish Create Supplier Year Review Task with companyId = " + company);
        Console.log("successRequestCount =>" + successCount + " ; successAgentCount => " + agentCount + " ;");
        return successCount;
    }


    /**
     * @Description: 内部经办人创建供应商评审流程实现类
     */
    private class AgentSupplier {
        private final Integer agentId;

        private final Integer suppliersCounts;

        private final Integer requestCounts;

        private ArrayList<Integer> suppliers;

        public AgentSupplier(Integer agentId, Integer suppliersCounts, Integer requestCounts) {
            this.agentId = agentId;
            this.suppliersCounts = suppliersCounts;
            this.requestCounts = requestCounts;
            suppliers = new ArrayList<>();
        }

        public AgentSupplier setSuppliers(ArrayList<Integer> suppliers) {
            this.suppliers = suppliers;
            return this;
        }

        public AgentSupplier setSuppliers(RecordSet recordSet) {
            String querySql = "SELECT id from " + SUPPLIER_TABLE + " where jbr = " + agentId;
            recordSet.execute(querySql);
            while (recordSet.next())
                suppliers.add(recordSet.getInt("id"));
            return this;
        }


        public int createRequest(RecordSet recordSet) throws Exception {
            int successCount = 0;

            int dept = -1;
            String deptQuerySql = "select departmentid from hrmresource where id = 108";
            recordSet.execute(deptQuerySql);
            if (recordSet.next())
                dept = recordSet.getInt("departmentid");
            Console.log(String.format("Start Agent's Suppliers insert.\n year=>%d,%s", year, this.toString()));


            for (int order = 0; order < requestCounts; order++) {
                Console.log("start order = " + order);

                String insertMainSql = String.format("insert into " + MAIN_TRIGGER_TABLE + " (creater,dept,year,counts,allcounts,company,orders) " +
                        "VALUES (%d,%d,%d,%d,%d,%d,%d)", agentId, dept, year, requestCounts, suppliersCounts, company, order);
                recordSet.execute(insertMainSql);
                Console.log("insertMainSql => " + insertMainSql);
                String queryMainIdSql = String.format("select max(id) as mainid from " + MAIN_TRIGGER_TABLE + " where creater = %d and YEAR = %d and orders = %d",
                        agentId, year, order);
                recordSet.execute(queryMainIdSql);
                Console.log("queryMainIdSql => " + queryMainIdSql);
                recordSet.next();
                int mainid = recordSet.getInt("mainid");
                for (int supplierOrder = 0; supplierOrder < maxDetailCount && order * maxDetailCount + supplierOrder < suppliers.size(); supplierOrder++) {
                    String insertDetailSql = String.format("insert into sd_TriggerSupplierReview_dt1 (mainid,supplier) VALUES (%d,%d)",
                            mainid, suppliers.get(order * maxDetailCount + supplierOrder));
                    recordSet.execute(insertDetailSql);
                    Console.log("insertDetailSql => " + insertDetailSql);
                }
                successCount++;
            }
            Console.log("Finish Agent's Suppliers insert of id =>" + agentId);
            return successCount;
        }

        @Override
        public String toString() {
            return "AgentSupplier{" +
                    "agentId=" + agentId +
                    ", suppliersCounts=" + suppliersCounts +
                    ", requestCounts=" + requestCounts +
                    ", suppliers=" + suppliers +
                    '}';
        }
    }

}
