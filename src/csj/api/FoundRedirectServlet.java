package csj.api;

import csj.utils.Console;
import weaver.conn.RecordSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @Author: 张骏山
 * @Date: 2024/8/16 13:29
 * @PackageName: csj.api
 * @ClassName: FoundRedirectServlet
 * @Description: 资产路径转发
 * @Version: 1.0
 **/
public class FoundRedirectServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String fdId = request.getParameter("fdId");
        RecordSet rs = new RecordSet();
        Console.log("fdId=>"+fdId);
        String sql = "select id,mark from cptcapital where fdId = '" + fdId + "'";
        Console.log("执行语句=>"+sql);
        rs.execute(sql);
        String id;
        String mark;
        if (rs.next()) {
            id = rs.getString("id");
            mark = rs.getString("mark");
        } else {
            id = "null";
            mark = "null";
        }
        Console.log("执行结果:id=>"+id+",mark:"+mark);
        response.getWriter().println(mark);
        response.sendRedirect("/spa/cpt/index.html#/main/cpt/cptcard?capitalid="+id);
    }
}
