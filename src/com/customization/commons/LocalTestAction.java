package com.customization.commons;

import weaver.conn.RecordSet;
import weaver.general.Util;
import weaver.hrm.User;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.RequestInfo;
import weaver.soa.workflow.request.RequestService;
import weaver.workflow.request.RequestManager;
import weaver.workflow.webservices.WorkflowRequestInfo;
import weaver.workflow.webservices.WorkflowServiceImpl;


import java.util.HashMap;
import java.util.Map;

/**
 * Action测试 类。
 * @author
 */
public class LocalTestAction {

	private Integer requestId;
	private String actionClass;
	private Integer lastOperator;
	private String src;

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	private String remark;

	private String mainFields;
	private Map<String, String> dtFields;

	public LocalTestAction(Integer requestId, String actionClass) {
		this.requestId = requestId;
		this.actionClass = actionClass;
	}

	public Integer getRequestId() {
		return requestId;
	}

	public void setRequestId(Integer requestId) {
		this.requestId = requestId;
	}

	public String getActionClass() {
		return actionClass;
	}

	public void setActionClass(String actionClass) {
		this.actionClass = actionClass;
	}

	public Integer getLastOperator() {
		return lastOperator;
	}

	public void setLastOperator(Integer lastOperator) {
		this.lastOperator = lastOperator;
	}

	public String getSrc() {
		return src;
	}

	public void setSrc(String src) {
		this.src = src;
	}

	public String execute() throws Exception {
		String result = null;
		Action action = (Action) Class.forName(this.actionClass).newInstance();



		RequestService requestService = new RequestService();
		RequestInfo req = requestService.getRequest(requestId);

		req.setLastoperator(this.lastOperator == null ? null : this.lastOperator.toString());
		//req.setRequestManager(new RequestManager());
		req.getRequestManager().setSrc(this.src);
		req.getRequestManager().setFormid(Util.getIntValue(getByRequestId(requestId).get("FORMID").toString()));
		req.getRequestManager().setCreater(Util.getIntValue(getByRequestId(requestId).get("CREATER").toString()));
		req.getRequestManager().setLastoperator(this.lastOperator);
		req.getRequestManager().setBillid(Util.getIntValue(getByRequestId(requestId).get("FORMID").toString()));
		req.getRequestManager().setIsbill(Util.getIntValue(getByRequestId(requestId).get("ISBILL").toString()));


		req.getRequestManager().setRequestid(this.requestId);
		req.getRequestManager().setRemark(this.remark);



		User user=new User(1);
		req.getRequestManager().setUser(user);
		result = action.execute(req);
		if (CustomUtil.isNotBlank(req.getRequestManager().getMessageid())) {
			throw new Exception(req.getRequestManager().getMessageid() + ": " + req.getRequestManager().getMessagecontent());
		}
		System.out.println("执行结果："+result);

		/*
		if("1".equals(result)) {
			WorkflowRequestInfo responseInfo = new WorkflowServiceImpl().getWorkflowRequest(this.requestId, this.lastOperator, this.requestId);
			String resp = new WorkflowServiceImpl().submitWorkflowRequest(responseInfo, this.requestId, this.lastOperator, this.src, this.remark);
			System.out.println("流程提交结果："+resp);
		}
		*/

		return result;
	}


	private static Map<String, Object> getByRequestId(int requestId) {
		RecordSet rs = new RecordSet();
		String sql = "SELECT T1.REQUESTID, T1.WORKFLOWID, T1.REQUESTNAME,T2.ISBILL, "
				+ " T1.CREATER, T2.FORMID FROM WORKFLOW_REQUESTBASE T1"
				+ " INNER JOIN WORKFLOW_BASE T2 ON T2.ID=T1.WORKFLOWID"
				+ " WHERE T1.REQUESTID=?";
		Map<String, Object> data = new HashMap<String, Object>();
		if (rs.executeQuery(sql, requestId) && rs.next()) {
			data.put("REQUESTID", rs.getInt("REQUESTID"));
			data.put("WORKFLOWID", rs.getInt("WORKFLOWID"));
			data.put("REQUESTNAME", rs.getString("REQUESTNAME"));
			data.put("CREATER", rs.getInt("CREATER"));
			data.put("FORMID", rs.getInt("FORMID"));
			data.put("ISBILL", rs.getInt("ISBILL"));
		}
		return data;
	}
}
