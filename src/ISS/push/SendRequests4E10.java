package ISS.push;


import ISS.util.AccessTokenUtil;
import ISS.util.ConfigUtil;
import ISS.util.Console;
import okhttp3.*;
import weaver.workflow.request.todo.DataObj;
import weaver.workflow.request.todo.RequestStatusObj;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

/**
 * @Author: 张骏山
 * @Date: 2025/5/10 17:51
 * @PackageName: ISS.push
 * @ClassName: SendRequests4E10
 * @Description: 统一待办推送E10
 * @Version: 1.0
 */
public class SendRequests4E10 {

    /**
     * 后台设置id
     */
    public String id ;
    /**
     * 设置的系统编号
     */
    public String syscode ;
    /**
     * 服务器URL
     */
    public String serverurl ;
    /**
     * 流程白名单
     */
    public ArrayList<String> workflowwhitelist ;
    /**
     * 人员白名单
     */
    public ArrayList<String> userwhitelist ;


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
     * @param datas 传入的请求数据对象数据集
     */
    public void SendRequestStatusData(ArrayList<DataObj> datas) {

        for(DataObj dobj : datas){
            JSONArray jsonArray = new JSONArray();
            JSONObject mainboject = new JSONObject();
            String requestname  = "";
            JSONObject todojson = new JSONObject();
            JSONObject donejson = new JSONObject();
            JSONObject deljson = new JSONObject();
            ArrayList<RequestStatusObj> tododatas = dobj.getTododatas();
            if(tododatas.size()>0){//处理推送的待办数据
                Console.log("TODO");
                for(RequestStatusObj rso : tododatas){//遍历当前发送的待办数据
                    JSONObject param = getParam(rso);
                    Console.log((param.toString()));
                    if(!pushRequest(param)){
                        Console.log("error-push:");
                    }
                }

            }
            ArrayList<RequestStatusObj> donedatas = dobj.getDonedatas();
            if(donedatas.size()>0){//处理推送的已办数据
                Console.log("Done");
                for(RequestStatusObj rso : donedatas){//遍历当前发送的已办数据
                    JSONObject param = getParam(rso);
                    Console.log((param.toString()));
                    if(!pushRequest(param)){
                        Console.log("error-push:");
                    }
                }

            }
            ArrayList<RequestStatusObj> deldatas = dobj.getDeldatas();
            if(deldatas.size()>0){//处理推送的删除数据
                Console.log("Delete");
                for(RequestStatusObj rso : deldatas){//遍历当前发送的删除数据
                    JSONObject param = getParam(rso,"-1");
                    Console.log((param.toString()));
                    if(!pushRequest(param)){
                        Console.log("error-push:");
                    }
                }
            }
        }

    }

    private JSONObject getParam(RequestStatusObj rso) {
        JSONObject param = new JSONObject();
        try {
            param.put("syscode",syscode);
            param.put("flowid",rso.getRequestid());
            param.put("requestname",rso.getRequestnamenew());
            param.put("workflowcode",rso.getWorkflowid());
            param.put("workflowname",rso.getWorkflowname());
            param.put("receivernodename",rso.getNodename());
            param.put("nodename",rso.getNodename());
            param.put("pcurl","/spa/workflow/static4form/index.html?_rdm=1746871206719#/main/workflow/req?requestid="+rso.getRequestid());
            param.put("appurl","/spa/workflow/static4mobileform/index.html?_random=1746871275492#/req?requestid="+rso.getRequestid());
            param.put("creator",rso.getCreator().getLoginid());
            param.put("createdatetime",rso.getCreatedate()+" "+rso.getCreatetime());
            param.put("receiver",rso.getUser().getLoginid());
            param.put("receivedatetime",rso.getReceivedate()+" "+rso.getReceivetime());
            param.put("isremark",rso.getIsremark());
            param.put("viewtype",rso.getViewtype().equals("1")?"1":"0");
            param.put("receivets",new Date().getTime());
            param.put("access_token",new AccessTokenUtil().getAccessToken());
        }catch (Exception e){
            e.printStackTrace();
            Console.log(e.getMessage());
        }
        return param;
    }

    private JSONObject getParam(RequestStatusObj rso, String isremark) {
        JSONObject param = new JSONObject();
        try {
            param.put("syscode",syscode);
            param.put("flowid",rso.getRequestid());
            param.put("requestname",rso.getRequestnamenew());
            param.put("workflowcode",rso.getWorkflowid());
            param.put("workflowname",rso.getWorkflowname());
            param.put("receivernodename",rso.getNodename());
            param.put("nodename",rso.getNodename());
            param.put("pcurl","/spa/workflow/static4form/index.html?_rdm=1746871206719#/main/workflow/req?requestid="+rso.getRequestid());
            param.put("appurl","/spa/workflow/static4mobileform/index.html?_random=1746871275492#/req?requestid="+rso.getRequestid());
            param.put("creator",rso.getCreator().getLoginid());
            param.put("createdatetime",rso.getCreatedate()+" "+rso.getCreatetime());
            param.put("receiver",rso.getUser().getLoginid());
            param.put("receivedatetime",rso.getReceivedate()+" "+rso.getReceivetime());
            param.put("isremark",isremark);
            param.put("viewtype",rso.getViewtype().equals("1")?"1":"0");
            param.put("receivets",new Date().getTime());
            param.put("access_token",new AccessTokenUtil().getAccessToken());
        }catch (Exception e){
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
            if (rtnJson.getJSONObject("message").getString("errcode").equals("200")){
                org.json.JSONObject data = rtnJson.getJSONObject("data");
                if (data.getString("operResult").equals("1")){
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


//    public static void main(String[] args) throws Exception{
//        JSONObject param = new JSONObject();
//        param.put("syscode","E9TEST");
//        param.put("flowid","5055448");
//        param.put("requestname","杜邦CTC-异常分析PDF报告-系统管理员-2025-05-08");
//        param.put("workflowcode","635");
//        param.put("workflowname","杜邦CTC-异常分析PDF报告");
//        param.put("receivernodename","结束");
//        param.put("nodename","结束");
//        param.put("pcurl","/spa/workflow/static4form/index.html?_rdm=1746871206719#/main/workflow/req?requestid=5055448");
//        param.put("appurl","/spa/workflow/static4mobileform/index.html?_random=1746871275492#/req?requestid=5055448");
//        param.put("creator","sysadmin");
//        param.put("createdatetime","2025-05-08 16:42:30");
//        param.put("receiver","haochuan.xu");
//        param.put("receivedatetime","2025-05-09 16:50:12");
//        param.put("isremark","0");
//        param.put("viewtype","0");
//        param.put("receivets",new Date().getTime());
//        param.put("access_token",new AccessTokenUtil().getAccessToken());
//        SendRequests4E10V4 sendRequestForE10 = new SendRequests4E10V4();
//        boolean b = sendRequestForE10.pushRequest(param);
//        System.out.println("b = " + b);
//    }

}