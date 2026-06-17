package rundo.quote;

import rundo.util.ApiUtil;
import rundo.util.Console;
import weaver.conn.RecordSet;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description: 子流程干预并删除 — 判断是否有已提交的子流程，并对子流程进行干预操作后删除
 * @Author: 张骏山
 * @Date: 2026/6/17
 * @PackageName: rundo.quote
 * @ClassName: QuoteRequestWithdrawCmd
 * @Version: 1.0
 * <p>
 * 配置说明（在 QuoteConfig 中配置以下属性）：
 * subProcessFormName — 子流程对应的表单名称
 * subProcessBDSColumn  — 子流程表单中存储BDS人员的列名
 * subProcessAimNodeColumn — 子流程目标节点列名
 * subProcessRequestIdColumn — 子流程requestId列名
 * subProcessCustomId — 子流程表单对应的customId
 */
public class QuoteRequestWithdrawCmd {

    /**
     * 子流程表单名称
     */
    private final String subFormName;
    /**
     * 当前流程bds人员
     */
    private final String BDSs;
    /**
     * 干预备注
     */
//    private String withDrawMark = "退回重启-子流程处理";
    private final int mainId;

    private final int aimNodeId;
    private final String currentReferFileIds;
    private final RecordSet rs;

    public QuoteRequestWithdrawCmd(String subFormName,
                                   int aimNodeId,
                                   int mainRequestId,
                                   String currentReferFileIds,
                                   String BDSs) {
        this.subFormName = subFormName;
        this.BDSs = BDSs;
        this.aimNodeId = aimNodeId;
        this.mainId = mainRequestId;
        this.currentReferFileIds = currentReferFileIds;
//        withDrawMark = ApiUtil.getPropsWithDefault("withDrawMark",withDrawMark);
        rs = new RecordSet();
    }


    /**
     * 判断是否有已提交的子流程
     * <p>
     * 通过查询 workflow_requestbase 表，判断指定主流程下是否存在子流程请求。
     * 子流程通过 parentrequestid 字段关联到主流程的 requestId，
     * 且子流程的 currentstatus 表示其处于已提交/运行中状态。
     * </p>
     *
     * @return true 存在已提交的子流程, false 不存在
     */
    public boolean hasSubProcessSubmitted() {
        Console.log("检查是否存在已提交的子流程, mainRequestId = " + mainId);

        if (mainId < 0) {
            Console.log("mainRequestId 无效, 无法检查子流程");
            return false;
        }
        String querySubmitSql = "select requestid, id "
                + "from  " + subFormName
                + "where mainid = " + mainId
                + "and bjckwj= '" + currentReferFileIds + "'"
                + "and (gdrq is null or gdrq = '') ";
        Console.log("查询子流程SQL: " + querySubmitSql);
        rs.executeQuery(querySubmitSql);

        boolean hasSubmitted = rs.next();
        Console.log("是否存在已提交的子流程: " + hasSubmitted);
        return hasSubmitted;
    }

    /**
     * 对子流程进行干预操作后删除
     * <p>
     * 1. 查询所有已提交的子流程
     * 2. 对每个子流程执行干预操作（退回到指定节点）
     * 3. 删除每个子流程对应的表单数据
     * </p>
     *
     * @param uid 操作人
     * @return true 全部处理成功, false 存在失败
     */
    public boolean withdrawMainRequest(int uid) {
        Console.log("开始干预并删除子流程, mainRequestId = " + mainId
                + ", BDSs = " + BDSs + ", aimNodeId = " + aimNodeId + ", uid = " + uid);

        if (mainId < 0) {
            Console.log("mainRequestId 无效, 无法处理子流程");
            return false;
        }

        // 1. 查询所有当前轮次子流程
        List<Integer> subList = new ArrayList<>();
        String sql = "select requestid, id "
                + "from  " + subFormName
                + "where mainid = " + mainId
                + "and bjckwj= '" + currentReferFileIds + "'";
        Console.log("查询子流程SQL: " + sql);
        rs.executeQuery(sql);

        while (rs.next()) {
            subList.add(rs.getInt("requestid"));
        }

        Console.log("共找到 " + subList.size() + " 个子流程");


        // 2. 逐个处理子流程：删除
        for (Integer sub : subList) {
            if (!ApiUtil.getInstance().deleteRequest(String.valueOf(sub))) {
                Console.log("删除子流程失败, requestId = " + sub);
                return false;
            }
        }

//        // 3. 干预主流程回到分发节点
//        if (!ApiUtil.getInstance().interventionRequest(mainRequestId, BDSs, aimNodeId, uid, withDrawMark)) {
//            Console.log("干预主流程失败, mainRequestId = " + mainRequestId);
//            return false;
//        }


        return true;
    }

    public boolean isAllSubmitted() {
        Console.log("检查所有子流程是否均已处理完毕, mainRequestId = " + mainId + " currentReferFileIds = " + currentReferFileIds);

        if (mainId < 0) {
            Console.log("mainRequestId 无效, 无法检查子流程");
            return false;
        }

        String querySubmitSql = "select id, requestid  "
                + "from  " + subFormName
                + "where mainid = " + mainId
                + "and bjckwj= '" + currentReferFileIds + "'"
                + "and ";
        Console.log("查询子流程SQL: " + querySubmitSql);
        rs.executeQuery(querySubmitSql);

        if (rs.next()) {
            int cnt = rs.getInt("cnt");
            Console.log("当前轮次子流程数量: " + cnt);
            return cnt == 0;
        }

        return true;
    }

    public boolean withDrawSubRequest(String submittedFileIds, String recordFormName, String submittedFileColumns) {
        Console.log("开始撤销审批记录, mainRequestId = " + mainId + ", submittedFileIds = " + submittedFileIds + ", recordFormName = " + recordFormName);
        String deleteSql = "delete from " + recordFormName + " where " + submittedFileColumns + " = '" + submittedFileIds + "' and mainid = " + mainId;
        Console.log("删除审批记录SQL: " + deleteSql);
        rs.executeUpdate(deleteSql);
        Console.log("撤销审批记录成功, mainRequestId = " + mainId + ", submittedFileIds = " + submittedFileIds);
        return true;
    }


//    /** 缓存 TTL：1 小时（毫秒） */
//    private static final long CACHE_TTL_MS = 3600 * 1000L;
//
//    /** mainRequestId → 缓存条目 */
//    private static final ConcurrentHashMap<Integer, CacheEntry> mainRequestCache = new ConcurrentHashMap<>();
//
//    /**
//     * 缓存条目，记录存储时间、关联的 bds 值及实际数据
//     */
//    private static class CacheEntry {
//        final String bds;
//        final long expiryTime;
//        final QuoteRequestWithdrawCmd data;
//
//        CacheEntry(String bds, QuoteRequestWithdrawCmd data) {
//            this.bds = bds;
//            this.data = data;
//            this.expiryTime = System.currentTimeMillis() + CACHE_TTL_MS;
//        }
//
//        boolean isExpired() {
//            return System.currentTimeMillis() > expiryTime;
//        }
//    }
//
//    /**
//     * 从 mainRequestId 缓存中获取数据
//     * <p>
//     * 仅当缓存命中、未过期且 bds 与存入时一致时返回缓存对象；
//     * 若 bds 不一致则主动清除缓存（视为废弃），返回 null。
//     * </p>
//     *
//     * @param mainRequestId 主流程 requestId
//     * @param BDSs           当前 bds 值（用于校验一致性）
//     * @return 缓存的数据对象，或 null（未命中/过期/bds 变更）
//     */
//    public static QuoteRequestWithdrawCmd getCachedMainRequestData(String subFormName,
//                                                                   int aimNodeId,
//                                                                   int mainRequestId,
//                                                                   String currentReferFileIds,
//                                                                   String BDSs) {
//        CacheEntry entry = mainRequestCache.get(mainRequestId);
//        // 无缓存 过期 失效
//        if (entry == null||entry.isExpired()||!entry.bds.equals(BDSs)) {
//            QuoteRequestWithdrawCmd data = new QuoteRequestWithdrawCmd(subFormName, aimNodeId, mainRequestId, currentReferFileIds, BDSs);
//            mainRequestCache.put(mainRequestId,new CacheEntry(BDSs,data));
//            return data;
//        }
//        return entry.data;
//    }

}