package com.customization.action.demo;


import com.customization.commons.Console;
import com.customization.commons.CustomAction;
import com.customization.commons.CustomUtil;
import weaver.general.Util;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.RequestInfo;
import weaver.workflow.request.RequestManager;

/**
 * 命名规则：
 * 当一只流程只有一个和SAP场景交互时，接口命规则为：
 * 流程编号+SAP接口场景编号 例如：MM050_SC_1230_FI_DocCreate_eg
 *
 * 当一只流程在不同节有多个SAP交互场景交互式，接口命名规则为：
 * 流程编号_流程调用节点_nodeid 前(before)/后(after)/出口(export)/(归档前)end

 *  type来源于流程接口里面配置的参数
 */
public class MM050_node688_after extends CustomAction implements Action {

    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String execute(RequestInfo requestInfo) {
        RequestManager rm = requestInfo.getRequestManager();
        int requestid=rm.getRequestid();
        try {

            String srcString = Util.null2String(rm.getSrc());//submit 提交  reject 退回
            if (srcString.equals("submit")) {
                //获取流程的数据
                super.getWorkflowDataValue(requestInfo);
                System.out.println("主表数据："+mainMap);
                System.out.println("明细表数据："+detailMap);

                Console.log("type:" + this.getType());
                //todo 在这里写相关业务逻辑


                if(!true){
                    String errorinfo="我是测试信息";
                    requestInfo.getRequestManager().setMessageid(""+requestid);
                    requestInfo.getRequestManager().setMessage("Action集成报错");
                    requestInfo.getRequestManager().setMessagecontent("请求ID:" + rm.getRequestid() + ";<br/>操作时间:"+ CustomUtil.getStringDate("yyyy-MM-dd HH:mm:ss") +";<br/><font style=\"color:#6e6e6e;font-size: 12px; font-weight: 700;\">和异构系统交互时返回错误!" +
                            "参考信息具体如下:</font><br/>" +errorinfo);
                    super.sendActionErrorInfo(requestid);
                    return Action.FAILURE_AND_CONTINUE;
                }
            }

        }catch (Exception e){
            e.printStackTrace();
            //在这里编写业务逻辑代码，调用往SAP系统写入值，如果需要阻止流程继续流转，参考下方catch里面信息处理。
            requestInfo.getRequestManager().setMessageid(""+requestid);
            requestInfo.getRequestManager().setMessage("Action集成报错");
            requestInfo.getRequestManager().setMessagecontent("请求ID:" + rm.getRequestid() + ";<br/>操作时间:"+ CustomUtil.getStringDate("yyyy-MM-dd HH:mm:ss") +";<br/><font style=\"color:#6e6e6e;font-size: 12px; font-weight: 700;\">和异构系统交互时返回错误!" +
                    "参考信息具体如下:</font><br/>" +e.toString());
            super.sendActionErrorInfo(requestid);
            return Action.FAILURE_AND_CONTINUE;
        }

      return  Action.SUCCESS;
    }

}
