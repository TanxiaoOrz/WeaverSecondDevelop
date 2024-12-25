<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%@ page import="weaver.general.Util"%>
<%@ page import="java.util.Date" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.text.ParseException" %>
<%@ page import="weaver.hrm.HrmUserVarify" %>
<%@ page import="csj.utils.Console" %>

<jsp:useBean id="rs" class="weaver.conn.RecordSet" scope="page" />
<%
    int userId = HrmUserVarify.getUser(request, response).getUID();
    String sql = "SELECT MAX\n" +
            "\t( datetime ) \n" +
            "FROM\n" +
            "\t(\n" +
            "\tSELECT\n" +
            "\t\tconcat ( operatedate, ' ', operatetime ) AS datetime \n" +
            "\tFROM\n" +
            "\t\tecology_biz_log log\n" +
            "\t\tLEFT JOIN ECOLOGY_BIZ_LOG_TYPE lt ON log.logtype = lt.ID \n" +
            "\tWHERE\n" +
            "\t\t( isdetail = 0 OR isdetail IS NULL ) \n" +
            "\t\tAND log.logType = 9 \n" +
            "\t\tAND log.logSmallType = 15 \n" +
            "\t\tAND log.userId = "+userId+" \n" +
            "\t\tAND operatetype != 'VIEW' UNION\n" +
            "\tSELECT\n" +
            "\t\tifnull ( modifydatetime, concat ( createdate, ' ', createtime ) ) AS datetime \n" +
            "\tFROM\n" +
            "\t\tmeeting \n" +
            "\tWHERE\n" +
            "\t\tID IN (\n" +
            "\t\tSELECT DISTINCT ID \n" +
            "\t\tFROM\n" +
            "\t\t\t( SELECT ID, creater, regexp_split_to_table( hrmmembers, ',' ) AS hrmmembers FROM meeting ) AS T \n" +
            "\t\tWHERE\n" +
            "\t\t\tcreater = "+userId+" \n" +
            "\t\t\tOR hrmmembers = "+userId+" \n" +
            "\t\t) \n" +
            "\t) AS T\n";
    Long timeStamp = new Long(Util.null2String(request.getParameter("timestamp")));
    rs.execute(sql);
    if (rs.next()) {
        String s = Util.null2String(rs.getString("max"));
//        out.println(s);
        SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date;
        try {
            date = ft.parse(s);
            if (date.getTime() > timeStamp + 1000)
                out.println(1);
            else
                out.println(0);
        } catch (ParseException e) {
            out.println(e);
        }

    }
%>

