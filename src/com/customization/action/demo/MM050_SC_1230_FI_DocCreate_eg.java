package com.customization.action.demo;


import com.customization.commons.Console;
import com.customization.commons.CustomAction;
import weaver.general.Util;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.RequestInfo;
import weaver.workflow.request.RequestManager;

/**
 * 命名规则：
 * 当一只流程只有一个和SAP场景交互时，接口命规则为：
 * 流程编号+SAP接口场景编号 例如：MM050_SC_1230_FI_DocCreate_eg
 * ********************************************
 * 当一只流程在不同节有多个SAP交互场景交互式，接口命名规则为：
 * 流程编号_流程调用节点_nodeid 前(before)/后(after)/出口(export)/(归档前)end
 *
 *
 */
public class MM050_SC_1230_FI_DocCreate_eg extends CustomAction implements Action {

    @Override
    public String execute(RequestInfo request) {

        //封装流程表单的值
        super.getWorkflowDataValue(request);
        writeLog("execute action MM050_SC_1230_FI_DocCreate_eg");//打印日志
        RequestManager rm = request.getRequestManager();
        String requestid = request.getRequestid();
        try {
            String srcString = Util.null2String(rm.getSrc());
            writeLog("srcString:"+srcString);
            //当提交时执行
            if (srcString.equals("submit")) {

                //在这里编写业务逻辑代码，调用往SAP系统写入值，如果需要阻止流程继续流转，参考下方catch里面信息处理。

                Console.log( "mainFieldValuesMap"+mainMap);
                Console.log( "detailFieldValuesMap"+detailMap);

                System.out.println("selectvalue:"+super.getSeletItemValue("cgsqlxjms",rm.getFormid(), Util.null2String(mainMap.get("cgsqlxjms"))));


            }

        }catch (Exception e){
            //异常报错是填写异常信息，阻止流程继续流转
            request.getRequestManager().setMessageid("90001");
            request.getRequestManager().setMessagecontent("-"+rm.getRequestid()+" 系统异常终止流程提交！");
        }
        return Action.SUCCESS;
    }
}
