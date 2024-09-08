package csj.humanArchives;

import weaver.conn.RecordSet;
import weaver.general.Util;

import java.util.Date;

/**
 * @Author: 张骏山
 * @Date: 2024/9/8 20:38
 * @PackageName: csj.humanArchives
 * @ClassName: UpdateHumanInformation
 * @Description: TODO
 * @Version: 1.0
 */
public class UpdateHumanInformation {

    private int daId;

    private RecordSet recordSet = new RecordSet();

    /**
     * 由档案向卡片同步
     * 头像 rytp=>resourceimageid
     * 移动电话 sjhm=>mobile
     */
    public void updateCardInformation() {
        recordSet.execute("update hrmResource as h set resourceimageid = rytp from uf_rsda as a where a.ry = h.id and a.id = " + daId);
        recordSet.execute("update hrmResource as h set mobile = sjhm from uf_rsda as a where a.ry = h.id and a.id = " + daId);
        recordSet.execute("update hrmResource as h set locationid = bgdd from uf_rsda as a where a.ry = h.id and a.id = " + daId);
    }

    /**
     * 更新档案内部自动计算信息
     */
    public void updateArchiveInformation() {
        //  本企业工龄\连续工龄(年)
        recordSet.execute("select dbdwrq,csrq from uf_rsda where id =" + daId);
        if (recordSet.next()) {
            String rzrq = Util.null2String(recordSet.getString("dbdwrq"));
            if (rzrq.equals("")) {
                recordSet.execute("update uf_rsda set bqygl = '0年0个月' where id =" + daId);
                recordSet.execute("update uf_rsda set lxgl = '0' where id =" + daId);
            } else {
                long distance = new Date().getTime() - new Date(rzrq).getTime();
                long months =  (distance / (1000L * 60 * 60 * 24 * 30));
                long years = (distance / (1000L * 60 * 60 * 24 * 365));
                double year = (distance * 1.0 / (1000L * 60 * 60 * 24 * 365 * 1.0));
                String bqygl = years + "年" + months + "个月";
                recordSet.execute("update uf_rsda set bqygl = '"+bqygl+"' where id =" + daId);
                recordSet.execute("update uf_rsda set lxgl = '"+year+"' where id =" + daId);
            }
            String csrq = Util.null2String(recordSet.getString("csrq"));
            if (!csrq.equals("")) {
                long distance = new Date().getTime() - new Date(csrq).getTime();
                long years = (distance / (1000L * 60 * 60 * 24 * 365));
                recordSet.execute("update uf_rsda set nl = '"+years+"' where id =" + daId);
            }
        }



    }

    public UpdateHumanInformation(int daId) {
        this.daId = daId;
    }

    public static void updateArchiveInformationAll() {
        RecordSet recordSet = new RecordSet();
        // 已确认档案关联查缺补漏
        recordSet.execute("update uf_rsda set ry = (select id from hrmResource where lastname = uf_rsda.rzyg) where dazt = 1 and ry is null");
        recordSet.execute("select id from uf_rsda where dazt = 1 and ry is not null");
        //  任职信息 -  任职结束日期 - 任职状态
        recordSet.execute("update uf_rsda_dt5 as o set jsrq =Substring(TO_CHAR((select min(d) from  (select ( TO_DATE(ksrq)) - integer '1' as d from uf_rsda_dt5 as n where n.mainid = o.mainid and n.ksrq > o.ksrq ) as t)),10) where jsrq is null");
        recordSet.execute("update uf_rsda_dt5 set rzzt = if(CURRENT_DATE>TO_DATE(jsrq),2,if(CURRENT_DATE > TO_DATE(ksrq), 1,0) )");
        // 合同信息 合同状态
        recordSet.execute("update uf_rsda_dt6 set htzt = if(CURRENT_DATE>TO_DATE(htjssj),2,if(CURRENT_DATE > TO_DATE(htkssj), 1,0) )");
        while (recordSet.next()) {
            new UpdateHumanInformation(Util.getIntValue(recordSet.getString("id"))).updateArchiveInformation();
        }
    }


}
