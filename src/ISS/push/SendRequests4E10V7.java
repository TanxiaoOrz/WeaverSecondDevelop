package ISS.push;


import ISS.util.AccessTokenUtil;
import ISS.util.ConfigUtil;
import ISS.util.Console;
import okhttp3.*;
import weaver.conn.RecordSet;
import weaver.general.Util;
import weaver.hrm.User;
import weaver.workflow.request.todo.DataObj;
import weaver.workflow.request.todo.RequestStatusObj;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

/**
 * @Author: 张骏山
 * @Date: 2025/5/10 17:51
 * @PackageName: ISS.push
 * @ClassName: SendRequests4E10V7
 * @Description: 统一待办推送E10
 * @Version: 1.1
 */
public class SendRequests4E10V7 {

    /**
     * 后台设置id
     */
    public String id;
    /**
     * 设置的系统编号
     */
    public String syscode;
    /**
     * 服务器URL
     */
    public String serverurl;
    /**
     * 流程白名单
     */
    public ArrayList<String> workflowwhitelist;
    /**
     * 人员白名单
     */
    public ArrayList<String> userwhitelist;

    private final static String TODO = "TODO";

    private final static String DONE = "DONE";

    private final static String DELETE = "DELETE";


    public String getId() {
        return id;
    }

    public String getSyscode() {
        return syscode;
    }

    public String getServerurl() {
        return serverurl;
    }

    public ArrayList<String> getWorkflowwhitelist() {
        return workflowwhitelist;
    }

    public ArrayList<String> getUserwhitelist() {
        return userwhitelist;
    }

    /**
     * 实现消息推送的具体方法
     *
     * @param datas 传入的请求数据对象数据集
     */
    public void SendRequestStatusData(ArrayList<DataObj> datas) {

        for (DataObj dobj : datas) {
            ArrayList<RequestStatusObj> tododatas = dobj.getTododatas();
            if (tododatas.size() > 0) {//处理推送的待办数据
                Console.log("TODO");
                for (RequestStatusObj rso : tododatas) {//遍历当前发送的待办数据
                    if (checkWhite(rso)) {
                        Console.log("人员或流程不在白名单");
                        continue;
                    }
                    JSONObject param = getParam(rso,TODO);
                    Console.log((param.toString()));
                    if (!pushRequest(param)) {
                        Console.log("error-push:");
                    }
                }

            }
            ArrayList<RequestStatusObj> donedatas = dobj.getDonedatas();
            if (donedatas.size() > 0) {//处理推送的已办数据
                Console.log("Done");
                for (RequestStatusObj rso : donedatas) {//遍历当前发送的已办数据
                    if (checkWhite(rso)) {
                        Console.log("人员或流程不在白名单");
                        continue;
                    }
                    JSONObject param = getParam(rso,DONE);
                    Console.log((param.toString()));
                    if (!pushRequest(param)) {
                        Console.log("error-push:");
                    }
                }

            }
            ArrayList<RequestStatusObj> deldatas = dobj.getDeldatas();
            if (deldatas.size() > 0) {//处理推送的删除数据
                Console.log("Delete");
                for (RequestStatusObj rso : deldatas) {//遍历当前发送的删除数据
                    if (checkWhite(rso)) {
                        Console.log("人员或流程不在白名单");
                        continue;
                    }
                    JSONObject param = getParam(rso, DELETE);
                    Console.log((param.toString()));
                    if (!pushRequest(param)) {
                        Console.log("error-push:");
                    }
                }
            }
        }

    }

    private boolean checkWhite(RequestStatusObj rso) {
        if (userwhitelist != null &&userwhitelist.size()>0) {
            if (!userwhitelist.contains(String.valueOf(rso.getUser().getUID()))) {
                return true;
            }
        }
        if (workflowwhitelist != null &&workflowwhitelist.size()>0) {
            return !workflowwhitelist.contains(String.valueOf(rso.getWorkflowid()));
        }
        return false;
    }

    private String getUserLogin(User user) {
        String mobile = user.getMobile();
        if (Util.null2String(mobile).equals("")) {
            return user.getLoginid();
        } else
            return mobile;
    }


    private JSONObject getParam(RequestStatusObj rso, String type) {
        JSONObject param = new JSONObject();
        try {
            param.put("syscode", syscode);
            param.put("flowid", rso.getRequestid());
            param.put("requestname", rso.getRequestnamenew());
            param.put("workflowcode", rso.getWorkflowid());
            param.put("workflowname", rso.getWorkflowname().replaceAll("\\u00A0", " ").replaceAll("<[^>]`>", ""));
            param.put("receivernodename", rso.getNodename().replaceAll("\\u00A0", " ").replaceAll("<[^>]`>", ""));
            param.put("nodename", rso.getNodename().replaceAll("\\u00A0", " ").replaceAll("<[^>]`>", ""));
            param.put("pcurl", "/spa/workflow/static4form/index.html?_rdm=1746871206719#/main/workflow/req?requestid=" + rso.getRequestid());
            param.put("appurl", "/spa/workflow/static4mobileform/index.html?_random=1746871275492#/req?requestid=" + rso.getRequestid());
            param.put("creator", getUserLogin(rso.getCreator()));
            param.put("createdatetime", rso.getCreatedate() + " " + rso.getCreatetime());
            param.put("receiver", getUserLogin(rso.getUser()));
            param.put("receivedatetime", rso.getReceivedate() + " " + rso.getReceivetime());
            switch (rso.getIsremark()) {
                /*
                0：未操作;1：转发;2：已操作;4：归档;5：超时;8：抄送(不需提交);9：抄送(需提交);11:传阅;6:自动审批（审批中）
                 */
                case "0":
                case "1":
                case "5":
                    param.put("isremark", "0"); // 待办
                    break;
                case "2":
                case "6":
                    param.put("isremark", "2"); // 已办
                    break;
                case "4":
                    param.put("isremark", "4"); //办结
                    break;
                case "8":
                case "9":
                case "11":
                    param.put("isremark", "8"); // 待阅
                    break;
            }
            param.put("viewtype", rso.getViewtype().equals("1") ? "1" : "0");
            param.put("receivets", new Date().getTime());
            param.put("access_token", new AccessTokenUtil().getAccessToken());
            switch (type) {
                case DELETE:
                    param.put("isremark", -1);
                    break;
                case TODO: {
                    RecordSet recordSet = new RecordSet();
                    recordSet.execute("select currentnodetype from workflow_requestbase where requestid =  " + rso.getRequestid());
                    if (recordSet.next()) {
                        String currentnodetype = Util.null2String(recordSet.getString("currentnodetype"));
                        if (!"".equals(currentnodetype)) {
                            if (currentnodetype.equals("0"))
                                param.put("requestStatus", "-1");
                            else
                                param.put("requestStatus", currentnodetype);
                        }
                    }
                    break;
                }
                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Console.log(e.getMessage());
        }
        return param;
    }


    private boolean pushRequest(JSONObject param) {
        try {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, param.toString());
            Request request = new Request.Builder()
                    .url(ConfigUtil.getE10Url() + "/papi/openapi/api/open_intunifytodo/server_engine/receiveRequestInfo")
                    .method("POST", body)
                    .addHeader("Content-Type", "application/json")
                    .build();
            Response response = client.newCall(request).execute();
            org.json.JSONObject rtnJson = new org.json.JSONObject(response.body().string());
            if (rtnJson.getJSONObject("message").getString("errcode").equals("200")) {
                org.json.JSONObject data = rtnJson.getJSONObject("data");
                if (data.getString("operResult").equals("1")) {
                    System.out.println("rtnJson = " + data.getString("message"));
                    Console.log(rtnJson.toString());
                    return true;
                }
            }
            System.out.println("rtnJson = " + (rtnJson.toString()));
            Console.log(rtnJson.toString());
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            Console.log(e.getMessage());
            return false;
        }

    }


    public static void main(String[] args) throws Exception{
        JSONObject param = new JSONObject();
        param.put("syscode","E9TEST");
        param.put("flowid","5055448");
        param.put("requestname","杜邦CTC-异常分析PDF报告-系统管理员-2025-05-08");
        param.put("workflowcode","635");
        param.put("workflowname","杜邦CTC-异常分析PDF报告");
        param.put("receivernodename","结束");
        param.put("nodename","结束");
        param.put("pcurl","/spa/workflow/static4form/index.html?_rdm=1746871206719#/main/workflow/req?requestid=5055448");
        param.put("appurl","/spa/workflow/static4mobileform/index.html?_random=1746871275492#/req?requestid=5055448");
        param.put("creator","sysadmin");
        param.put("createdatetime","2025-05-08 16:42:30");
        param.put("receiver","haochuan.xu");
        param.put("receivedatetime","2025-05-09 16:50:12");
        param.put("isremark","0");
        param.put("viewtype","0");
        param.put("receivets",new Date().getTime());
        param.put("access_token",new AccessTokenUtil().getAccessToken());
        param.put("syscode","change");
        System.out.println("param = " + param.toString());
    }

}