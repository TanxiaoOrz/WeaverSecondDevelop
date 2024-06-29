<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%@ page import="weaver.general.Util"%>

<jsp:useBean id="rs" class="weaver.conn.RecordSet" scope="page" />
<jsp:useBean id="rs1" class="weaver.conn.RecordSet" scope="page" />
<%
	String requestid = Util.null2String(request.getParameter("requestid"));
	String sql = "";
	String tablename = "";
	String version = "";
	String fjzd = request.getParameter("fjzd");
	String spfj = "";
	String logid = "";
	String remark = "";
	String operator = "";

//获取表名
	sql = "select c.tablename from workflow_requestbase a, workflow_base b, workflow_bill c "
			+ " where a.workflowid=b.id and b.formid=c.id "
			+ " and a.requestid="+requestid;
	rs.execute(sql);
	if(rs.next()){
		tablename = Util.null2String(rs.getString("tablename"));
	}
//获取字段值
	sql = "select requestversion,"+fjzd+" from "+tablename+" where requestid="+requestid;
	rs.execute(sql);
	if(rs.next()){
		version = Util.null2String(rs.getString("requestversion"));
		spfj = Util.null2String(rs.getString(2));
	}
//插入建模表 uf_lcyjbb
	sql = "insert into uf_lcyjbb(lc, logid, bb, yj, spr, spsj) "
			+ " select top 1 requestid, logid, "+version+", remark, operator, operatedate + ' ' + operatetime as time "
			+ " from workflow_requestLog where requestId="+requestid
			+ " order by operatedate desc, operatetime desc ";
	out.println(sql);
	rs.execute(sql);
	sql = "insert into uf_lcbbwj(lcid,wj,bb)"
			+" select "+requestid+",'"+spfj+"',"+version
			+" from "+tablename;
	rs.execute(sql);

%>

<script>
	console.log("start")
	let {requestid} = WfForm.getbaseInfo()
	if (requestid == -1) {
		WfForm.changeFieldValue(WfForm.convertFieldNameToId('requestversion'),{value:0})
	}
</script>
