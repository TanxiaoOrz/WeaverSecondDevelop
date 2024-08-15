<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%@ page import="weaver.general.Util"%>
<%@ page import="weaver.hrm.HrmUserVarify" %>

<jsp:useBean id="rs" class="weaver.conn.RecordSet" scope="page" />
<jsp:useBean id="rsHuman" class="weaver.conn.RecordSet" scope="page" />
<%
    int billId = Util.getIntValue(Util.null2String(request.getParameter("billId")));
    String alertString;
    rs.execute("select kczt,pxrq from uf_pxhy where id = '"+billId+"'");
    if (rs.next()) {
        int kczt = rs.getInt("kczt");
        if (kczt == 2) {    // 培训中
            rs.execute("update uf_pxhy set kczt = 3 where id = '" + billId + "'");
            rs.execute("select * from uf_pxhy_dt1 where mainid = '" + billId + "'");
            while (rs.next()) {    // 遍历报名明细
                int ry = rs.getInt("bmchry");
                out.println(ry);
                rsHuman.execute("select id from uf_ygxs where ygmc =" + ry);
                boolean has = rsHuman.next();
                String ygxsId = "";
                if (has)
                    ygxsId = rsHuman.getString("id");
                if (!has) {
                    String sql ="INSERT INTO \"uf_ygxs\" ( \"requestid\", \"formmodeid\", \"modedatacreater\", \"modedatacreatertype\", \"modedatacreatedate\", \"modedatacreatetime\", \"modedatamodifier\", \"modedatamodifydatetime\", \"form_biz_id\", \"modeuuid\", \"ygmc\", \"zxs\" )\n" +
                            "VALUES" +
                            "(" +
                            "NULL," +
                            "37," +
                            "1," +
                            "0," +
                            "CURRENT_DATE," +
                            "LOCALTIME ( 0 )," +
                            "NULL," +
                            "NULL," +
                            "'1000200'," +
                            "'8cad5a17-a44f-4d3e-8211-560c71b64cba'," +
                            ry+"," +
                            "(SELECT sum(pxsc) as pxsc from uf_pxhy as m LEFT JOIN uf_pxhy_dt1 as dt on m.id = dt.mainid where m.kczt = 3 and dt.sfqd = 2 and dt.bmchry = " + ry +
                            "))";
                    out.println(sql);
                    rsHuman.execute(sql);
                } else {
                    rsHuman.execute("update uf_ygxs set zxs = "+"(SELECT sum(pxsc) as pxsc from uf_pxhy as m LEFT JOIN uf_pxhy_dt1 as dt on m.id = dt.mainid where m.kczt = 3 and dt.sfqd = 2 and dt.bmchry = " + ry+") where id = " + ygxsId);
//                    out.println("update uf_ygxs set zxs = "+pxsc+" where id = " + ygxsId);
                }
                int qdzt = rs.getInt("sfqd");
                if (qdzt != 2) {    // 未签到
                    rsHuman.execute("update uf_pxhy_dt1 set sfqd = 3 where mainid = " + billId + " and bmchry = " +  ry );
//                    out.println("update uf_pxhy_dt1 set sfqd = 3 where mainid = " + billId + " and bmchry = " +  ry );
                }
                rsHuman.execute("update uf_ygxs set zxs = 0 where zxs is null" );

            }
            alertString = "培训完成";
        } else
            alertString = "课程不在培训中或未知课程编号";
    } else {
        alertString = "课程不在培训中或未知课程编号";
    }
%>
<script>
    alert('<%= alertString%>')
    window.history.back();
</script>