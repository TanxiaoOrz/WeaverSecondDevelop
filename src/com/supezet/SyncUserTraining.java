package com.supezet;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huawei.shade.org.joda.time.LocalDate;
import hotswap.VersionControl;
import okhttp3.*;
import org.json.JSONException;
import org.json.JSONObject;
import weaver.conn.RecordSet;
import weaver.general.Util;
import weaver.interfaces.schedule.BaseCronJob;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author: 张骏山
 * @Date: 2024/3/27 9:23
 * @PackageName: com.supezet
 * @ClassName: SyncUserTraining
 * @Description: 将人员信息同步到培训系统中
 * @Version: 1.0
 **/
public class SyncUserTraining extends BaseCronJob implements VersionControl {
    /**
     * 支持访问文档
     * @Url https://tbc.21tb.com/open/platformDoc/index.do#/guide
     */
    public static final String CORP_CODE = "supezet";
    public static final String APP_KEY = "FAAF846F1EA5450E8D71B9B2E948AFC5";

    public static final String API = "https://v4.21tb.com/open/v1/uc/user/syncUsersVer2.html";

    public static final String REQUEST_URI = "/v1/uc/user/syncUsersVer2";
    public static final String FORBIDDEN = "FORBIDDEN";
    public static final String ENABLE = "ENABLE";


    private RecordSet recordSet;


    private JSONObject failMessages;
    private String errorMessage;
    private HashMap<Integer, User> users;

    public static void main(String[] args) {
        LocalDate localDate = LocalDate.now();
        LocalTime localTime =  LocalTime.now();
        System.out.println("localTime = " + localTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        System.out.println("localDate = " + localDate);
    }


    @Override
    public void execute() {
        supezetLog.log("开始同步培训系统人员");
        recordSet = new RecordSet();
        failMessages = null;
        errorMessage = null;
        int syncCount = getAddPeople() + getDeletePeople();
        supezetLog.log("待同步人员共有"+ syncCount +"个");
        if (syncCount > 0) {
            int successCount;
            try {
                successCount = syncPeople();
            } catch (Exception e) {
                supezetLog.log("同步结束");
                return;
            }
            supezetLog.log("成功同步人员共有" + successCount + "个");
            if (errorMessage != null) {
                supezetLog.log("同步出错,errorMessage=>" + errorMessage);
            } else {
                updateRecord();
            }
        } else {
            supezetLog.log("无需同步");
        }
    }

    private int getAddPeople() {
        final String sql = "select h.id,h.workcode,h.lastname,d.departmentcode from" +
                "(SELECT hrm.id,hrm.workcode,hrm.departmentid,hrm.lastname from HrmResource as hrm" +
                " left join uf_transLogin as login on hrm.id = login.ry where ry is null and status not in (4, 5))" +
                " as h left join hrmdepartment as d on h.departmentid = d.id";
        recordSet.execute(sql);
        users = new HashMap<>(recordSet.getCounts());
        while (recordSet.next()) {
            Integer id = Util.getIntValue(recordSet.getInt("id"));
            User user = new User(
                    Util.null2String(recordSet.getString("workcode")),
                    Util.null2String(recordSet.getString("lastname")),
                    Util.null2String(recordSet.getString("departmentcode")),
                    ENABLE
            );
            if (user.getEmployeeCode().equals(""))
                user.setEmployeeCode(id.toString());
            users.put(id,user);
        }
        return users.size();
    }

    private int getDeletePeople() {
        final String sql = "select h.id,h.workcode,h.lastname,d.departmentcode,h.departmentid from" +
                "(SELECT hrm.id,hrm.workcode,hrm.departmentid,hrm.lastname from HrmResource as hrm" +
                " left join uf_transLogin as login on hrm.id = login.ry where ry is not null and status in (4, 5))" +
                " as h left join hrmdepartment as d on h.departmentid = d.id";
        recordSet.execute(sql);
        //users = new HashMap<>(recordSet.getCounts());
        while (recordSet.next()) {
            Integer id = Util.getIntValue(recordSet.getInt("id"));
            User user = new User(
                    Util.null2String(recordSet.getString("workcode")),
                    Util.null2String(recordSet.getString("lastname")),
                    Util.null2String(recordSet.getString("departmentcode")),
                    FORBIDDEN
            );
            if (user.getEmployeeCode().equals(""))
                user.setEmployeeCode(id.toString());
            if (user.getOrganizeCode().equals(""))
                user.setOrganizeCode(Util.null2String(recordSet.getString("departmentid")));
            users.put(id,user);
        }
        return users.size();
    }

    private Integer syncPeople() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        String userString;
        try {
            userString = objectMapper.writeValueAsString(users.values());
        } catch (JsonProcessingException e) {
            supezetLog.log("字符串转换失败"+e.getMessage());
            throw e;
        }
        JSONObject json = new JSONObject();

        try {
            json.put("users",userString);
            json.put("updatePassword",false);
            json.put("appKey_", APP_KEY);
            json.put("sign_", SupezetTrainUtils.getSign(REQUEST_URI));
            json.put("timestamp_",String.valueOf(System.currentTimeMillis()));
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("application/json, charset=utf-8");
            RequestBody body = RequestBody.create(mediaType, json.toString());
            supezetLog.log("请求报文=>"+json.toString());
            Request request = new Request.Builder()
                    .url(API)
                    .method("POST", body)
                    .addHeader("Content-Type", "application/json")
                    .build();
            Response response = client.newCall(request).execute();
            JSONObject rtnJson = new JSONObject(response.body().string());
//
//            JSONObject rtnJson = new JSONObject(
//                    "{\n" +
//                            "    \"status\": \"DATA_INVALID\",\n" +
//                            "    \"success\": true,\n" +
//                            "    \"successCount\": 1,\n" +
//                            "    \"successData\": [\n" +
//                            "        \"zhangsan\"\n" +
//                            "    ],\n" +
//                            "    \"failData\": {\"employeeCode1\":[\"errorInfo1_1\",\"errorInfo1_2\"],\"employeeCode2\":[\"errorInfo2_1\",\"errorInfo2_2\"]}" +
//                            "}"
//            );

            supezetLog.log("接口调用成功返回=>"+rtnJson);
            String status = rtnJson.getString("status");
            int successCount = 0;
            try {
                successCount = rtnJson.getInt("successCount");
            } catch (Exception ignored) {}
            switch (status) {
                case "OK":
                    break;
                case "ERROR":
                    errorMessage = rtnJson.getJSONArray("errorMessage").toString();
                case "DATA_INVALID":
                    failMessages = rtnJson.getJSONObject("failData");
            }
            return successCount;
        } catch (Exception e) {
            supezetLog.log("json添加失败"+e.getMessage());
            throw e;
        }
    }

    private void updateRecord() {
        LocalDate localDate = LocalDate.now();
        LocalTime localTime =  LocalTime.now();
        String timeFormat = localTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        String dateFormat = localDate.toString();
        AtomicInteger updateCount = new AtomicInteger(0);
        AtomicInteger deleteCount = new AtomicInteger(0);
        users.forEach((id, user) -> {
            String name = user.getLoginName();
            String employeeCode = user.getEmployeeCode();
            String accountStatus = user.getAccountStatus();
            if (failMessages == null || !failMessages.has(employeeCode)) {
                if (accountStatus.equals(ENABLE)) {
                    String sql = "INSERT INTO [uf_transLogin] ([formmodeid], [modedatacreater], [modedatacreatertype], [modedatacreatetime], [modedatacreatedate], [ry], [dlm]) VALUES (116, 1, 0, '"+timeFormat+"', '"+dateFormat+"', " + id + ", '" + name + "')";
                    supezetLog.log("人员编号=>" + id + "姓名=>" + name + "sql语句=>" + sql);
                    recordSet.executeUpdate(sql);
                    updateCount.addAndGet(1);
                } else if (accountStatus.equals(FORBIDDEN)){
                    String sql = "delete from [uf_transLogin] where ry = " + id;
                    supezetLog.log("人员编号=>" + id + "姓名=>" + name + "sql语句=>" + sql);
                    recordSet.execute(sql);
                    deleteCount.addAndGet(1);
                }
            } else {
                try {
                    supezetLog.log("人员编号=>"+id+"姓名=>"+name+"同步失败原因=>"+failMessages.getJSONArray(employeeCode).toString());
                } catch (JSONException e) {
                    supezetLog.log(e.getMessage());
                }
            }
        });
        supezetLog.log("总计添加"+updateCount.toString()+"条数据");
        supezetLog.log("总计删除"+deleteCount.toString()+"条数据");
    }

    @Override
    public String getVersion() {
        return "TEST-CLEAN-4";
    }


    public static class User {
        /**
         * 必填部分属性
         */
        String employeeCode;
        String corpCode;
        String userName;
        String loginName;
        String accountStatus;
        String organizeCode;

        /**
         * 非必填部分属性
         */
//        String positionCode;
//        String superiorEmployeeCode;
//        String email;
//        String mobile;
//        String officeTel;
//        String rank;
//        String onBoarding;

        public User(String employeeCode, String lastname, String organizeCode, String accountStatus) {
            this.employeeCode = employeeCode;
            this.corpCode = CORP_CODE;
            this.userName = lastname;
            this.loginName = lastname;
            this.accountStatus = accountStatus;
            this.organizeCode = organizeCode;
        }

        public String getEmployeeCode() {
            return employeeCode;
        }

        public User setEmployeeCode(String employeeCode) {
            this.employeeCode = employeeCode;
            return this;
        }

        public String getCorpCode() {
            return corpCode;
        }

        public User setCorpCode(String corpCode) {
            this.corpCode = corpCode;
            return this;
        }

        public String getUserName() {
            return userName;
        }

        public User setUserName(String userName) {
            this.userName = userName;
            return this;
        }

        public String getLoginName() {
            return loginName;
        }

        public User setLoginName(String loginName) {
            this.loginName = loginName;
            return this;
        }

        public String getAccountStatus() {
            return accountStatus;
        }

        public User setAccountStatus(String accountStatus) {
            this.accountStatus = accountStatus;
            return this;
        }

        public String getOrganizeCode() {
            return organizeCode;
        }

        public User setOrganizeCode(String organizeCode) {
            this.organizeCode = organizeCode;
            return this;
        }
    }


}
