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

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author: 张骏山
 * @Date: 2024/3/27 9:23
 * @PackageName: com.supezet
 * @ClassName: SyncDepartAndSectionTraining
 * @Description: TODO
 * @Version: 1.0
 **/
public class SyncDepartAndSectionTraining extends BaseCronJob implements VersionControl {
    /**
     * 支持访问文档
     * @Url <a href="https://tbc.21tb.com/open/platformDoc/index.do#/guide">...</a>
     */

    public static final String CORP_CODE = "supezet";
    public static final String APP_KEY = "FAAF846F1EA5450E8D71B9B2E948AFC5";
    public static final String API = "https://v4.21tb.com/open/v1/uc/organize/syncOrganizeVer2.html";
    public static final String REQUEST_URI = "/v1/uc/organize/syncOrganizeVer2";

    public static final String ROOT_CODE = "*"; // 根组织代号自己定即可,测试时练习卓然人员修改

    private HashMap<Integer, Organization> organizations;
    private RecordSet recordSetD;
    private RecordSet recordSetS;

    private JSONObject failMessages;
    private String errorMessage;

    @Override
    public void execute() {
        supezetLog.log("开始同步培训系统人员");
        recordSetD = new RecordSet();
        recordSetS = new RecordSet();
        failMessages = null;
        errorMessage = null;
        int syncCount = getAddOrganization();
        supezetLog.log("待同步组织共有"+ syncCount +"个");
        if (syncCount > 0) {
            int successCount;
            try {
                successCount = syncOrganization();
            } catch (Exception e) {
                supezetLog.log("同步结束");
                return;
            }
            supezetLog.log("成功同步组织共有" + successCount + "个");
            if (errorMessage != null) {
                supezetLog.log("同步出错,errorMessage=>" + errorMessage);
            } else {
                updateRecord();
            }
        } else {
            supezetLog.log("无需同步");
        }
    }

    private int syncOrganization() throws JSONException, IOException, NoSuchAlgorithmException {
        ObjectMapper objectMapper = new ObjectMapper();
        String organizes;
        try {
            organizes = objectMapper.writeValueAsString(organizations.values());
        } catch (JsonProcessingException e) {
            supezetLog.log("字符串转换失败"+e.getMessage());
            throw e;
        }
        JSONObject json = new JSONObject();

        try {
            json.put("organizes",organizes);
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
        organizations.forEach((idType,organization) -> {
            int type = idType % 2;
            int id = idType / 2;
            String organizeCode = organization.getOrganizeCode();
            String organizeName = organization.getOrganizeName();
            String sql;
            if (failMessages == null || !failMessages.has(organizeCode)) {
                switch (type) {
                    case 1: // 部门
                        sql = "insert into uf_trainSectionLog ([formmodeid], [modedatacreater], [modedatacreatertype], [modedatacreatetime], [modedatacreatedate], [fb], [sj]) values (127,1,0,'"+timeFormat+"', '"+dateFormat+"', " + id + ", '" + dateFormat + "')";
                        supezetLog.log("分部编号=>" + id + "名称=>" + organizeName + "sql语句=>" + sql);
                        break;
                    case 0: // 分部
                        sql = "insert into uf_trainDepartLog ([formmodeid], [modedatacreater], [modedatacreatertype], [modedatacreatetime], [modedatacreatedate], [bm], [sj]) values (126,1,0,'"+timeFormat+"', '"+dateFormat+"', " + id + ", '" + dateFormat + "')";
                        supezetLog.log("部门编号=>" + id + "名称=>" + organizeName + "sql语句=>" + sql);
                        break;
                    default:
                        sql = "";
                }
                recordSetD.execute(sql);
                updateCount.addAndGet(1);
            } else {
                try {
                    supezetLog.log("编号=>"+id+"姓名=>"+organizeName+"同步失败原因=>"+failMessages.getJSONArray(organizeCode).toString());
                } catch (JSONException e) {
                    supezetLog.log(e.getMessage());
                }
            }
        });
        supezetLog.log("总计添加"+updateCount.toString()+"条数据");
    }

    public int getAddOrganization() {
        final String sqlDepart =
                "SELECT\n" +
                        "    departInformtaion.* \n" +
                        "FROM\n" +
                        "    (\n" +
                        "    SELECT\n" +
                        "        depart.id,\n" +
                        "        depart.departmentname,\n" +
                        "        sub.subcompanycode,\n" +
                        "        depart.leadDcode,\n" +
                        "        depart.departmentcode,\n" +
                        "        depart.supdepid ,\n" +
                        "        depart.subcompanyid1 \n" +
                        "    FROM\n" +
                        "        (\n" +
                        "        SELECT\n" +
                        "            md.id,\n" +
                        "            md.departmentname,\n" +
                        "            md.subcompanyid1,\n" +
                        "            subd.departmentcode AS leadDcode,\n" +
                        "            md.departmentcode,\n" +
                        "            md.supdepid \n" +
                        "        FROM\n" +
                        "            HrmDepartment AS md\n" +
                        "            LEFT JOIN HrmDepartment AS subd ON md.supdepid = subd.id \n" +
                        "        WHERE\n" +
                        "            md.canceled != 1 \n" +
                        "            OR md.canceled IS NULL \n" +
                        "        ) AS depart\n" +
                        "        LEFT JOIN HrmSubCompany AS sub ON depart.subcompanyid1 = sub.id \n" +
                        "    ) AS departInformtaion\n" +
                        "    LEFT JOIN uf_trainDepartLog AS logs ON departInformtaion.id = logs.bm \n" +
                        "WHERE\n" +
                        "    logs.id IS NULL";
        recordSetD.execute(sqlDepart);

        String sqlSection =
                "SELECT\n" +
                        "    sections.* \n" +
                        "FROM\n" +
                        "    (\n" +
                        "    SELECT\n" +
                        "        ms.id,\n" +
                        "        ms.subcompanyname,\n" +
                        "        ms.subcompanycode,\n" +
                        "        ss.subcompanycode AS leadCode,\n" +
                        "        ms.supsubcomid \n" +
                        "    FROM\n" +
                        "        hrmsubcompany AS ms\n" +
                        "        LEFT JOIN hrmsubcompany AS ss ON ms.supsubcomid = ss.id \n" +
                        "    WHERE\n" +
                        "        ms.canceled != 1 \n" +
                        "        OR ms.canceled IS NULL \n" +
                        "    ) AS sections\n" +
                        "    LEFT JOIN uf_trainSectionLog AS logs ON sections.id = logs.fb \n" +
                        "WHERE\n" +
                        "    logs.id IS NULL";
        recordSetS.execute(sqlSection);
        organizations = new HashMap<>(recordSetD.getCounts() + recordSetS.getCounts());

        while (recordSetS.next()) {
            Integer id =Util.getIntValue(recordSetS.getInt("id"))  * 2 +1;
            String leadCode = Util.null2String(recordSetS.getString("supsubcomid"));
            Organization organization = new Organization(
                    id.toString(),
                    Util.null2String(recordSetS.getString("subcompanyname")),
                    leadCode.equals("0")?ROOT_CODE:String.valueOf(Util.getIntValue(recordSetS.getInt("supsubcomid"))  * 2 +1)
            );
            organizations.put(id,organization);
        }

        while (recordSetD.next()) {
            Integer id = Util.getIntValue(recordSetD.getInt("id")) * 2;
            String leadCode = Util.null2String(recordSetD.getString("supdepid"));
            Organization organization = new Organization(
                    id.toString(),
                    Util.null2String(recordSetD.getString("departmentname")),
                    leadCode.equals("0")?String.valueOf(Util.getIntValue(recordSetD.getInt("subcompanyid1"))  * 2 +1):String.valueOf(Util.getIntValue(recordSetD.getInt("supdepid"))  * 2)
            );

            organizations.put(id,organization);
        }

        return organizations.size();
    }

    @Override
    public String getVersion() {
        return "TEST-DEV-1";
    }

    /**
     * 组织类
     */
    class Organization {
        /**
         * 	部门编码
         */
        String organizeCode;
        /**
         * 部门名称
         * 长度不超过50字符
         */
        String organizeName;
        /**
         * 上级部门编号
         * 指定此部门的上级部门， 如果上级部门编号不存在，则此部门同步失败。长度限制为64字符以内，新增时必填，更新时选填，为空表示不更新上级部门。使用*表示根节点。
         */
        String parentCode;
        /**
         * 	和开通的平台账号的企业ID一致
         */
        String corpCode;

        public Organization(String organizeCode, String organizeName, String parentCode) {
            this.organizeCode = organizeCode;
            this.organizeName = organizeName;
            this.parentCode = parentCode;
            this.corpCode = CORP_CODE;
        }

        public String getOrganizeCode() {
            return organizeCode;
        }

        public Organization setOrganizeCode(String organizeCode) {
            this.organizeCode = organizeCode;
            return this;
        }

        public String getOrganizeName() {
            return organizeName;
        }

        public Organization setOrganizeName(String organizeName) {
            this.organizeName = organizeName;
            return this;
        }

        public String getParentCode() {
            return parentCode;
        }

        public Organization setParentCode(String parentCode) {
            this.parentCode = parentCode;
            return this;
        }

        public String getCorpCode() {
            return corpCode;
        }

        public Organization setCorpCode(String corpCode) {
            this.corpCode = corpCode;
            return this;
        }
    }

    public static void main(String[] args) throws IOException {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("application/json, charset=utf-8");
        RequestBody body = RequestBody.create(mediaType, "{\"timestamp_\":\"1719997663629\",\"sign_\":\"20820D5B33CB7DC3C086F65AF7E6C96A\",\"appKey_\":\"FAAF846F1EA5450E8D71B9B2E948AFC5\",\"organizes\":\"[{\\\"organizeCode\\\":\\\"586\\\",\\\"organizeName\\\":\\\"测试部门\\\",\\\"parentCode\\\":\\\"ZH050500\\\",\\\"corpCode\\\":\\\"supezet\\\"}]\"}");
        Request request = new Request.Builder()
                .url("https://v4.21tb.com/open/v1/uc/organize/syncOrganizeVer2.html")
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .build();
        Response response = client.newCall(request).execute();
        System.out.println("response.body().string() = " + response.body().string());
    }
}
