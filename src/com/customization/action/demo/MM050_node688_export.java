package com.customization.action.demo;

import com.customization.commons.Console;
import com.customization.commons.CustomAction;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.RequestInfo;

/**
 * 命名规则：
 * 当一只流程只有一个和SAP场景交互时，接口命规则为：
 * 流程编号+SAP接口场景编号 例如：MM050_SC_1230_FI_DocCreate_eg
 *
 * 当一只流程在不同节有多个SAP交互场景交互式，接口命名规则为：
 * 流程编号_流程调用节点_nodeid 前(before)/后(after)/出口(export)/(归档前)end
 *
 */
public class MM050_node688_export extends CustomAction implements Action {


    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String execute(RequestInfo requestInfo) {
        Console.log("type:"+this.getType());
        requestInfo.getRequestManager().setMessageid("90002");
        requestInfo.getRequestManager().setMessagecontent("MM050_node688_before");
        return Action.FAILURE_AND_CONTINUE;
    }
}
