package com.customization.commons;



import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.general.GCONST;
import weaver.general.Util;
import weaver.hrm.company.DepartmentComInfo;
import weaver.hrm.resource.ResourceComInfo;
import weaver.soa.workflow.request.*;
import weaver.workflow.request.RequestManager;
import weaver.workflow.workflow.WorkflowRequestComInfo;

import java.io.*;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * Created by liutaihong on 16-06-17.
 * 描述：日志及取值工具类
 */
public class CustomAction extends BaseBean {
 


    protected HashMap<String, String> mainMap;//主字段的值
    protected HashMap<String, HashMap <String, HashMap<String, String>>> detailMap ;//所有明细表的值

    protected WSBaseInfo wsBaseInfo ;
    protected int wf_creater ;
    protected int wf_formid  ;
    protected int wf_isbill ;
    protected String formtablename ;
    protected int wf_user ;


    public CustomAction(){
        mainMap = new HashMap<String, String>();//主字段的值
        detailMap = new HashMap<String, HashMap<String, HashMap<String, String>>>();//所有明细表的值
        wsBaseInfo = new WSBaseInfo();
        wf_creater = 0;
        wf_formid = 0;
        wf_isbill = 0;
        formtablename = "";
        wf_user = 0;
    }





    /**
     * 获取主表数据
     * 在流程action里面初始化后只需要通过Map就能快速的取到对应的主表的值
     * <p>
     * 适用场景：
     * 1.通过获取表名，然后使用SQL语句查询你要的主表数据(升级之后表名还会出错)。
     * 2.通过遍历requestInfo.getMainTableInfo().getProperty()做判断匹配值。
     * <p>
     * 使用：
     * 获取流程名称：map.get("requestName");
     * 获取流程requestid：map.get("requestId");
     * 获取流程创建人：map.get("creatorId");
     * 获取流程类型id：map.get("workflowId");
     *
     * @param requestInfo 流程对象
     * @return mainMap 主表的列名和值
     */
    public synchronized void getWorkflowDataValue(RequestInfo requestInfo) {
        /*
        Console.log(" 执行：getWorkflowDataValue");
        long waittime=ProcessingUtil.start();
        Console.log(requestInfo.getRequestid()+"等待"+waittime+"ms");
        */

        RequestManager rm = requestInfo.getRequestManager();

        //System.out.println("select * from formtable_main_"+Math.abs(rm.getFormid())+" where requestid='"+rm.getRequestid()+"'");
        RequestManager RequestManager = requestInfo.getRequestManager();
        wf_formid = RequestManager.getFormid();//流程表单id
        wf_creater = RequestManager.getCreater();//流程创建人
        wf_isbill = RequestManager.getIsbill();//是否为单据

        wf_user = RequestManager.getUser().getUID();//当前操作者


        mainMap.put("requestname", requestInfo.getDescription());//流程标题
        mainMap.put("requestid", requestInfo.getRequestid());//流程id
        mainMap.put("creatorid", requestInfo.getCreatorid());//流程创建人ID
        mainMap.put("workflowid", requestInfo.getWorkflowid());


        try {
            ResourceComInfo rci = new ResourceComInfo();
            DepartmentComInfo dci = new DepartmentComInfo();


            wsBaseInfo.setCreater_departmentcode(dci.getDepartmentCode(rci.getDepartmentID("" + wf_creater)));
            wsBaseInfo.setCreater_departmentid(rci.getDepartmentID("" + wf_creater));
            wsBaseInfo.setCreater_departmentname(dci.getDepartmentname(rci.getDepartmentID("" + wf_creater)));
            wsBaseInfo.setCreater_department_codeAndName(dci.getDepartmentCode(rci.getDepartmentID("" + wf_creater)) + "|" + dci.getDepartmentname(rci.getDepartmentID("" + wf_creater)));


            wsBaseInfo.setCreater_lastname(rci.getLastname("" + wf_creater));
            wsBaseInfo.setCreater_workcode(rci.getWorkcode("" + wf_creater));
            wsBaseInfo.setCreater_workcodeAndName(rci.getWorkcode("" + wf_creater) + "|" + rci.getLastname("" + wf_creater));


            wsBaseInfo.setCurrent_departmentcode(dci.getDepartmentname(rci.getDepartmentID("" + wf_user)));
            wsBaseInfo.setCurrent_departmentid(rci.getDepartmentID("" + wf_user));
            wsBaseInfo.setCurrent_departmentname(dci.getDepartmentCode(rci.getDepartmentID("" + wf_user)));
            wsBaseInfo.setCurrent_department_codeAndName(dci.getDepartmentname(rci.getDepartmentID("" + wf_user)) + "|" + dci.getDepartmentCode(rci.getDepartmentID("" + wf_user)));

            wsBaseInfo.setCurrent_lastname(rci.getLastname("" + wf_user));
            wsBaseInfo.setCurrent_workcode(rci.getWorkcode("" + wf_user));
            wsBaseInfo.setCurrent_workcodeAndName(rci.getWorkcode("" + wf_user) + "|" + rci.getLastname("" + wf_user));

            wsBaseInfo.setFormid(Math.abs(rm.getFormid()));


            //************ 这部部分非30项目全新的项目可以去掉

            mainMap.put("createrworkcode", rci.getWorkcode("" + wf_creater));//流程创建人工号
            mainMap.put("createrlastname", rci.getLastname("" + wf_creater));//流程创建人姓名
            mainMap.put("createrdepartmentid", rci.getDepartmentID("" + wf_creater));//流程创建人部门ID
            mainMap.put("createrdepartmentname", dci.getDepartmentname(rci.getDepartmentID("" + wf_creater)));//流程创建人部门名称
            mainMap.put("createrdepartmentcode", dci.getDepartmentCode(rci.getDepartmentID("" + wf_creater)));//流程创建人部门编号

            mainMap.put("currentworkcode", rci.getWorkcode("" + wf_user));//当前操作人工号
            mainMap.put("currentlastname", rci.getLastname("" + wf_user));//当前操作人姓名
            mainMap.put("currentdepartmentid", rci.getDepartmentID("" + wf_user));//当前操作人部门ID
            mainMap.put("currentdepartmentname", dci.getDepartmentname(rci.getDepartmentID("" + wf_user)));//当前操作人部门名称
            mainMap.put("currentdepartmentcode", dci.getDepartmentCode(rci.getDepartmentID("" + wf_user)));//当前操作人部门编号


        } catch (Exception e) {
            System.out.println("流程基础信息获取:" + e.toString());
        }
        // System.out.println(".......>"+wsBaseInfo.getSys());


        System.out.println("requestid:" + requestInfo.getRequestid());
        Property[] properties = requestInfo.getMainTableInfo().getProperty();// 获取表单主字段信息
        for (int i = 0; i < properties.length; i++) {// 主表数据
            String name = properties[i].getName().toLowerCase();
            String value = Util.null2String(properties[i].getValue());
            //System.out.println("formtable_main_"+Math.abs(wf_formid)+"."+name+"："+ Util.null2String(value));
            mainMap.put(name, Util.null2String(value));
            //writeLog(name + ":" + value);
        }

        DetailTable[] detailtable = null;
        try {
            detailtable = requestInfo.getDetailTableInfo().getDetailTable();// 获取明细表
            if (detailtable.length > 0) {

                for (int i = 1; i <= detailtable.length; i++) {//遍历明细表
                    HashMap<String, HashMap<String, String>> rowList = new LinkedHashMap<String, HashMap<String, String>>();
                    DetailTable dt = detailtable[i-1];
                    Row[] s = dt.getRow();

                    for (int j = 1; j <= s.length; j++) {
                        HashMap<String, String> rowMap = new LinkedHashMap<String, String>();
                        Row r = s[j-1];

                        Cell[] c = r.getCell();
                        for (int k = 0; k < c.length; k++) {
                            Cell c1 = c[k];
                            String name = c1.getName().toLowerCase();
                            String value = c1.getValue();

                            //System.out.println(i+""+"-"+name+":"+value);
                            rowMap.put(name, Util.null2String(value));
                        }
                        if (rowMap.size() > 0) {
                            rowList.put(r.getId(), rowMap);
                        }

                    }

                    detailMap.put(dt.getTableDBName(), rowList);
                }
            }

        } catch (Exception e) {
          
        }

        Console.log("-------------requestid:"+requestInfo.getRequestid()+"--------------");
        Console.log("mainMap:"+mainMap.toString());
        Console.log("detailMap:"+detailMap.toString());
       
    }

    private String getSeletItemValue(String fieldname, int formid, String selectvalue, Integer index) {
        String name = "";

        try {
            RecordSet recordSet = new RecordSet();
            if (selectvalue.equals("null") || selectvalue == null) {
                selectvalue = "";
            }

            String detailtable = "";
            String fname = fieldname;
            String billid = "";
            if (fieldname.contains(".")) {
                detailtable = fieldname.split("\\.")[0];
                fname = fieldname.split("\\.")[1];
            }

            String sqls = "select * from workflow_billfield where billid= '" + formid + "' and fieldname='" + fname + "' ";
            if (detailtable.equals("")) {
                sqls = sqls + " and detailtable is null ";
            } else {
                sqls = sqls + " and detailtable ='" + detailtable + "'";
            }

            recordSet.execute(sqls);
            if (recordSet.first()) {
                billid = recordSet.getString("id");
            }

            sqls = "select * from WORKFLOW_SELECTITEM where SELECTvalue='" + selectvalue + "' and FIELDID='" + billid + "'";
            recordSet.execute(sqls);
            if (recordSet.first()) {
                name = recordSet.getString("selectName");
            }

            if (index != null && !name.isEmpty() && name.contains("|")) {
                name = name.split("\\|")[index];
            }
        } catch (Exception var11) {
        }

        return name.trim();
    }

    public static String getSeletItemValue(String fieldname, int formid, String selectvalue) {
        return new CustomAction().getSeletItemValue(fieldname, formid, selectvalue, (Integer)null);
    }

    public static String getSeletItemValueBefore(String fieldname, int formid, String selectvalue) {
        return new CustomAction().getSeletItemValue(fieldname, formid, selectvalue, 0);
    }

    public static String getSeletItemValueAfter(String fieldname, int formid, String selectvalue) {
        return new CustomAction().getSeletItemValue(fieldname, formid, selectvalue, 1);
    }


    /**
     * 触发一个集成流程报错的提醒
     * @param requestid
     */
    public static void sendActionErrorInfo(int requestid){

        Boolean istrue = true;
        String sendErrorWfCode = "," + Util.null2String(new BaseBean().getPropValue("customPropSet", "sendErrorWf"), "0") + ",";
        String sqlstr = "select WORKFLOWID from  workflow_requestbase where requestid='" + requestid + "'";
        RecordSet recordSet = new RecordSet();
        recordSet.execute(sqlstr);
        if (recordSet.next()) {
            if (sendErrorWfCode.contains("," + recordSet.getString("WORKFLOWID") + ",")) {
                //当为设置的流程wrokflow时
                istrue = false;
            }
        }
        if(sendErrorWfCode.equals(",0,")){
            istrue = true;
        }
        if (istrue) {
            new Thread((new Runnable() {
                @Override
                public void run() {
                    String remarkfs = ""; //发送报文内容
                    try {

                        WorkflowRequestComInfo requestComInfo = new WorkflowRequestComInfo();
                        String requestname =  "【流程Action集成报错】：相关请求\"" + requestComInfo.getRequestName("" + requestid) + "(" + requestid + ")\"";
                        String remark = requestname + "</br>操作时间:" + CustomUtil.getStringDate("yyyy-MM-dd HH:mm:ss") + "";
                        remark += "</br>相关请求:<a target=\"_blank\" href=\"/spa/workflow/static4form/index.html?#/main/workflow/req?requestid=" + requestid + "&ismonitor=1\">" + requestid + "</a>";

                        InetAddress addr = InetAddress.getLocalHost();
                        String ip = addr.getHostAddress().toString(); //获取本机ip
                        String hostName = addr.getHostName().toString(); //获取本机计算机名称
                        remark += "</br>执行服务器IP:" + ip;
                        remark += "</br>服务主机名:" + hostName;


                        int resourceid = Util.getIntValue(Util.null2String(new BaseBean().getPropValue("customPropSet", "actionResourceid")), 1);
                        setSysRemindInfo(requestname, resourceid, remark);
                        // 设置主表信息


                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            })).start();
        }

    }

    /**
     * 项目提醒工作流
     * @param requestname 工作流标题
     * @param resource  提醒人
     * @param remark 备注
     * @throws Exception
     */
    public static void setSysRemindInfo(String requestname,int resource,String remark) throws Exception{
        new weaver.system.SysRemindWorkflow().setSysRemindInfo(requestname,0,0,0,0,resource,""+resource,remark);
    }


}

