package com.supezet;

import hotswap.VersionControl;
import weaver.conn.RecordSet;
import weaver.interfaces.schedule.BaseCronJob;


/**
 * @Author: 张骏山
 * @Date: 2024/7/11 19:28
 * @PackageName: com.supezet
 * @ClassName: UpdateFoundReward
 * @Description: 更新资金计划的评论
 * @Version: 1.0
 */
public class UpdateFoundReward extends BaseCronJob implements VersionControl {

    @Override
    public String getVersion() {
        return "test";
    }

    @Override
    public void execute() {
        String sql = "update uf_zbzjjhx set pl = (select nr from vw_reply_zjjxpj as r where r.rqid = uf_zbzjjhx.id) where modedatacreatedate > DATEADD(MONTH, -3, getdate()) ";
        RecordSet recordSet = new RecordSet();
        recordSet.executeUpdate(sql);
        int updateCount = recordSet.getUpdateCount();
        supezetLog.log("更新成功,执行语句=>"+sql+"可能的更新数量=>"+updateCount);
    }
}
