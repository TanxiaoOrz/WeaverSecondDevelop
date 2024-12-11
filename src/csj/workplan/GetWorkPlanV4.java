package csj.workplan;

import java.util.ArrayList;
import java.util.List;

import weaver.conn.RecordSet;
import weaver.general.TimeUtil;
import weaver.general.Util;
import weaver.hrm.resource.ResourceComInfo;

public class GetWorkPlanV4
{
    public String getWP(String type, String date, String datetime, String subcomid, String deptid, String ispc) throws Exception {
        RecordSet rs = new RecordSet();
        ResourceComInfo rci = new ResourceComInfo();
        String sql = "select * from vw_ldrc where ID IN ( SELECT rcid FROM vw_ldrc_id ) ";
        String billid = "";
        String rcksrq = "";
        String rckssj = "";
        String rcjsrq = "";
        String rcjssj = "";
        String bm = "";
        String fb = "";
        String hddd = "";
        String hdnr = "";
        String ryxm = "";
        String pcdz = "";
        String yddz = "";
        String url = "";
        String resourceid = "";
        String othermembers = "";
        List idlist = new ArrayList();

        String returnStr = "";
        if (type.equals("am")) {
            sql = sql + " and (" +
//                    "'" + date + " 00:00' between concat(rcksrq , ' ' , rckssj) and concat(rcjsrq , ' ' , rcjssj)" +
//                    " or '" + date + " 12:00' between concat(rcksrq , ' ' , rckssj) and concat(rcjsrq , ' ' , rcjssj)" +
                    "  concat(rcksrq , ' ' , rckssj) between '" + date + " 00:00' and '" + date + " 12:00'" +
//                    " or concat(rcjsrq , ' ' , rcjssj) between '" + date + " 00:00' and '" + date + " 12:00'" +
                    " ) ";
        }
        else if (type.equals("pm")) {
            sql = sql + " and (" +
//                    "'" + date + " 12:01' between concat(rcksrq , ' ' , rckssj) and concat(rcjsrq , ' ' , " +
//                    "rcjssj)" +
//                    " or '" + date + " 23:59' between concat(rcksrq , ' ' , rckssj) and concat(rcjsrq , ' ' , rcjssj)" +
                    "  concat(rcksrq , ' ' , rckssj) between '" + date + " 12:01' and '" + date + " 23:59'" +
//                    " or concat(rcjsrq , ' ' , rcjssj) between '" + date + " 12:01' and '" + date + " 23:59'" +
                    " ) ";
        }

        if ((!subcomid.equals("")) && (subcomid != null)) {
            sql = sql + " and concat(',',fb,',')  like '%," + subcomid + ",%' ";
        }
        if ((!deptid.equals("")) && (deptid != null)) {
            sql = sql + " and concat(',',bm,',')  like '%," + deptid + ",%' ";
        }
        rs.execute(sql);

        while (rs.next())
        {
            billid = Util.null2String(rs.getString("id"));
            rcksrq = Util.null2String(rs.getString("rcksrq"));
            rckssj = Util.null2String(rs.getString("rckssj"));
            rcjsrq = Util.null2String(rs.getString("rcjsrq"));
            rcjssj = Util.null2String(rs.getString("rcjssj"));
            bm = "," + Util.null2String(rs.getString("bm")) + ",";
            fb = "," + Util.null2String(rs.getString("fb")) + ",";
            hddd = Util.null2String(rs.getString("hddd"));
            hdnr = Util.null2String(rs.getString("hdnr"));
            ryxm = Util.null2String(rs.getString("ryxm"));
            pcdz = Util.null2String(rs.getString("pcljdz"));
            yddz = Util.null2String(rs.getString("ydljdz"));
            resourceid = Util.null2String(rs.getString("resourceid"));
            idlist = Util.TokenizerString(resourceid, ",");
            othermembers = Util.null2String(rs.getString("othermembers"));

            for(int i=0;i<idlist.size();i++){
                ryxm += "、"+rci.getLastname(Util.null2String(idlist.get(i)));
            }
            if (!othermembers.equals(""))
                ryxm += ("、" + othermembers);

            if(ispc.equals("0")){
                url = yddz;
            }else if(ispc.equals("1")){
                url = pcdz;
            }

            if ((!subcomid.equals("")) &&
                    (fb.indexOf("," + subcomid + ",") == -1)) {
                return returnStr;
            }

            if ((!deptid.equals("")) &&
                    (bm.indexOf("," + deptid + ",") == -1)) {
                return returnStr;
            }

            if(ispc.equals("0") && hdnr.length()>19){
                hdnr = hdnr.substring(0,18)+"...";
            }

            returnStr += "<tr><td>"
                    + "<a href='" + url + "' target='_blank'>"
                    + "<font style='font-size:16px' blod >" + hdnr + "</font><br/>"
                    + "<font color='#666'>" + rcksrq + " " + rckssj + " ~ " + rcjsrq + " " + rcjssj + "</font" +
                    "><br/>"
                    + "<font color='#666'>人员：	" + ryxm.substring(1) + "</font><br/>"
                    + "<font color='#666'>地点：	" + hddd + "</font><br/>"
                    + "</a>"
                    + "</td></tr>";
        }

        return returnStr;
    }

    public static void main(String[] args) {
        String fb = "10";
        String fbstr = ",1,10,14,";
        System.out.println(fbstr.indexOf("," + fb + ",") == -1);

        String time1 = "2024-08-26 10:00:00";
        String time2 = "2024-08-26 12:00:00";
        long time = TimeUtil.timeInterval(time1, time2);
        System.out.println("time=>" + time);
        String zfc = "话说这里的字符有多少个呢大概二十来个吧哈哈哈哈哈asfasf";
        System.out.println(zfc.length());
        if(zfc.length()>20) {
            zfc = zfc.substring(0, 18) + "...";
        }
        System.out.println(zfc);
    }


}