<%@page contentType="text/html; charset=UTF-8"%>
<%@ page import="com.cloudstore.dev.api.util.Util_TableMap" %>
<%@ page import="weaver.general.Util" %>
<%@ page import="com.alibaba.fastjson.JSONObject" %>
<%@ page import="com.cloudstore.dev.api.bean.SplitPageBean" %>
<%@ page import="weaver.conn.RecordSet" %>
<%@ page import="com.cloudstore.dev.api.util.Util_public" %>
<%@ page import="com.alibaba.fastjson.JSON" %>
<%@ page import="com.weaver.formmodel.mobile.utils.TextUtil" %>
<%@ page import="javax.xml.parsers.DocumentBuilderFactory" %>
<%@ page import="javax.xml.parsers.DocumentBuilder" %>
<%@ page import="java.io.StringReader" %>
<%@ page import="org.xml.sax.InputSource" %>
<%
	String dataKey = Util.null2String(request.getParameter("dataKey"));
	JSONObject jo = new JSONObject();
	jo.put("status", false);
	try {
		if (!"".equals(dataKey)) {
			//AndyZhang 解决IP地址切换的导致的数据获取不到问题
			String xmlString = Util.null2String(Util_TableMap.getVal(dataKey));

			boolean isxml=false;
			try {
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				DocumentBuilder builder = factory.newDocumentBuilder();
				builder.parse(new InputSource(new StringReader(xmlString)));
				isxml= true;
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (!"".equals(xmlString) && isxml) {
				jo.put("xmlString", TextUtil.escapeHtml(xmlString));
				SplitPageBean bean = new SplitPageBean(request, dataKey, "head", "sql");
				String sortParams = Util.null2String(request.getParameter("sortParams"));
				RecordSet rs = new RecordSet();

				JSONObject sqlObj = bean.getSql();
				String tFields = Util.null2String(sqlObj.getString("backfields"));
				String tFrom = Util.toSqlForSplitPage(Util.null2String(sqlObj
						.getString("sqlform")));
				String tWhere = Util.toSqlForSplitPage(Util.null2String(sqlObj
						.getString("sqlwhere")));
				String tOrder = Util.null2String(sqlObj.getString("sqlorderby"));
				String tOrderWay = Util.null2String(sqlObj.getString("sqlsortway"));
				String tDistinct = Util.null2String(sqlObj.getString("sqlisdistinct"));

				StringBuilder sb = new StringBuilder();

				sb.append(" select " + ("true".equals(tDistinct) ? "distinct " : "")
						+ tFields);
				sb.append(" ");
				if (!tFrom.startsWith("from"))
					sb.append("from");
				sb.append(" ").append(tFrom);
				sb.append(" ");
				if (!tWhere.startsWith("where") && !"".equals(tWhere))
					sb.append(" where");
				sb.append(" ").append(tWhere);
				if (!"".equals(tOrder))
					sb.append(" order by ").append(tOrder);
				if (!"".equals(tOrderWay))
					sb.append("  ").append(tOrderWay);

				String sql = sb.toString();



				jo.put("sql", sql);
				jo.put("status", true);
			} else {
				jo.put("msg", "xmlString "+weaver.systeminfo.SystemEnv.getHtmlLabelName(18622,weaver.general.ThreadVarLanguage.getLang())+"");
			}
		} else {
			jo.put("msg", "dataKey "+weaver.systeminfo.SystemEnv.getHtmlLabelName(18622,weaver.general.ThreadVarLanguage.getLang())+"");
		}
	} catch (Exception e) {
		//e.printStackTrace();
		jo.put("msg", Util_public.getErrorInfoFromException(e));
	}
	out.println(JSON.toJSONString(jo));

%>