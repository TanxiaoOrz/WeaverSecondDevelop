package com.supezet;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.json.JSONException;
import org.json.JSONObject;
import weaver.conn.RecordSet;
import weaver.general.Util;
import weaver.interfaces.schedule.BaseCronJob;

import java.io.IOException;
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
public class SyncUserTraining extends BaseCronJob {
    /**
     * 支持访问文档
     * @Url https://tbc.21tb.com/open/platformDoc/index.do#/guide
     */
    public static final String CORP_CODE = "supezet";
    public static final String APP_KEY = "FAAF846F1EA5450E8D71B9B2E948AFC5";
    public static final String SIGN = "2F6B226E1A114163A07CA5F18026FCB3";
    public static final String api = "https://supezet.21tb.com"+"/v1/uc/user/syncUsersVer2";


    private RecordSet recordSet;
    private int successCount;


    private JSONObject failMessages;
    private String errorMessage;
    private HashMap<Integer, User> users;


    @Override
    public void execute() {
        supezetLog.log("开始同步培训系统人员");
        recordSet = new RecordSet();
        failMessages = null;
        errorMessage = null;
        int syncCount = getUnSyncPeople();
        supezetLog.log("待同步人员共有"+ syncCount +"个");
        try {
            successCount = syncPeople();
        } catch (IOException|JSONException e) {
            supezetLog.log("同步结束");
            return;
        }
        supezetLog.log("成功同步人员共有"+ successCount +"个");
        if (successCount > 0) {
            updateRecord();
        }
    }

    private int getUnSyncPeople() {
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
                    Util.null2String(recordSet.getString("departmentcode"))
            );
            users.put(id,user);
        }
        return users.size();
    }

    private Integer syncPeople() throws IOException, JSONException {
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
            json.put("sign_", SIGN);
            json.put("timestamp_",String.valueOf(System.currentTimeMillis()));
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, json.toString());
            Request request = new Request.Builder().url(api)
                    .method("Post",body)
                    .addHeader("Content-Type", "application/json").build();

            Response response = new OkHttpClient().newBuilder().build().newCall(request).execute();
            JSONObject rtnJson = new JSONObject(response.body().string());
            supezetLog.log("请求报文=>"+json.toString());
            supezetLog.log("接口调用成功返回=>"+rtnJson);
            String status = rtnJson.getString("status");
            int successCount = rtnJson.getInt("successCount");
            switch (status) {
                case "OK":
                    break;
                case "ERROR":
                    errorMessage = rtnJson.getString("errorMessage");
                case "DATA_INVALID":
                    failMessages = rtnJson.getJSONObject("failCount");
            }
            return successCount;
        } catch (JSONException | IOException e) {
            supezetLog.log("json添加失败"+e.getMessage());
            throw e;
        }
    }

    private void updateRecord() {
        AtomicInteger updateCount = new AtomicInteger();
        users.forEach((id, user) -> {
            if (failMessages == null || !failMessages.has(user.getEmployeeCode())) {
                String name = user.getLoginName();
                String sql = "INSERT INTO [uf_transLogin] ([formmodeid], [modedatacreater], [modedatacreatertype], [modedatacreatedate], [modedatacreatetime], [ry], [dlm]) VALUES (116, 1, 0, 03 20 2024, 03 20 20, " + id + ", '"+ name +"')";
                supezetLog.log("更新人员编号=>"+id+"姓名=>"+name+"sql语句=>"+sql);
                recordSet.execute(sql);
                updateCount.addAndGet(recordSet.getUpdateCount());
            }
        });
        supezetLog.log("总计添加"+updateCount.toString()+"条数据");
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

        public User(String employeeCode, String lastname, String organizeCode) {
            this.employeeCode = employeeCode;
            this.corpCode = CORP_CODE;
            this.userName = lastname;
            this.loginName = lastname;
            this.accountStatus = "ENABLE";
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
