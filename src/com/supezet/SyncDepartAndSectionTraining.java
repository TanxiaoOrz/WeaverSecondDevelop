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
    public static final String SIGN = "2F6B226E1A114163A07CA5F18026FCB3";
    public static final String api = "https://supezet.21tb.com/open/v1/uc/organize/syncOrganizeVer2.html";
    public static final String FORBIDDEN = "FORBIDDEN";

    public static final String ROOT_CODE = "ZR";

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
            } else if (successCount > 0) {
                updateRecord();
            }
        } else {
            supezetLog.log("无需同步");
        }
    }

    private int syncOrganization() throws JSONException, IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        String userString;
        try {
            userString = objectMapper.writeValueAsString(organizations.values());
        } catch (JsonProcessingException e) {
            supezetLog.log("字符串转换失败"+e.getMessage());
            throw e;
        }
        JSONObject json = new JSONObject();

        try {
            json.put("users",userString);
            json.put("appKey_", APP_KEY);
            json.put("sign_", SIGN);
            json.put("timestamp_",String.valueOf(System.currentTimeMillis()));
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, json.toString());
            Request request = new Request.Builder().url(api)
                    .method("POST",body)
                    .addHeader("Content-Type", "application/json").build();
            supezetLog.log("api:"+api);
            supezetLog.log("请求报文=>"+json.toString());
            Response response = new OkHttpClient().newBuilder().build().newCall(request).execute();
            JSONObject rtnJson = new JSONObject(response.body().string());



            supezetLog.log("接口调用成功返回=>"+rtnJson);
            String status = rtnJson.getString("status");
            int successCount = rtnJson.getInt("successCount");
            switch (status) {
                case "OK":
                    break;
                case "ERROR":
                    errorMessage = rtnJson.getJSONArray("errorMessage").toString();
                case "DATA_INVALID":
                    failMessages = rtnJson.getJSONObject("failCount");
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
                    case 0: // 部门
                        sql = "insert into uf_trainSectionLog ([formmodeid], [modedatacreater], [modedatacreatertype], [modedatacreatedate], [modedatacreatetime], [fb], [sj]) values (126,1,0,'"+timeFormat+"', '"+dateFormat+"', " + id + ", '" + dateFormat + "')";
                        supezetLog.log("分部编号=>" + id + "名称=>" + organizeName + "sql语句=>" + sql);
                        break;
                    case 1: // 分部
                        sql = "insert into uf_trainDepartLog ([formmodeid], [modedatacreater], [modedatacreatertype], [modedatacreatedate], [modedatacreatetime], [bm], [sj]) values (126,1,0,'"+timeFormat+"', '"+dateFormat+"', " + id + ", '" + dateFormat + "')";
                        supezetLog.log("部门编号=>" + id + "名称=>" + organizeName + "sql语句=>" + sql);
                        break;
                    default:
                        sql = "";
                }
                recordSetD.executeUpdate(sql);
                updateCount.addAndGet(1);
            } else {
                try {
                    supezetLog.log("编号=>"+id+"姓名=>"+organizeName+"同步失败原因=>"+failMessages.getJSONArray(organizeCode).toString());
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
            supezetLog.log("总计添加"+updateCount.toString()+"条数据");
        });
    }

    public int getAddOrganization() {
        final String sqlDepart =
                """
                SELECT
                    departInformtaion.*\s
                FROM
                    (
                    SELECT
                        depart.id,
                        depart.departmentname,
                        sub.subcompanycode,
                        depart.leadDcode,
                        depart.departmentcode\s
                    FROM
                        (
                        SELECT
                            md.id,
                            md.departmentname,
                            md.subcompanyid1,
                            subd.departmentcode AS leadDcode,
                            md.departmentcode\s
                        FROM
                            HrmDepartment AS md
                            LEFT JOIN HrmDepartment AS subd ON md.supdepid = subd.id\s
                        WHERE
                            md.canceled != 1\s
                            OR md.canceled IS NULL\s
                        ) AS depart
                        LEFT JOIN HrmSubCompany AS sub ON depart.subcompanyid1 = sub.id\s
                    ) AS departInformtaion
                    LEFT JOIN uf_trainDepartLog AS logs ON departInformtaion.id = logs.bm\s
                WHERE
                    logs.id IS NULL        
                """;
        recordSetD.execute(sqlDepart);

        String sqlSection = """
                SELECT
                	sections.*\s
                FROM
                	(
                	SELECT
                		ms.id,
                		ms.subcompanyname,
                		ms.subcompanycode,
                		ss.subcompanycode AS leadCode\s
                	FROM
                		hrmsubcompany AS ms
                		LEFT JOIN hrmsubcompany AS ss ON ms.supsubcomid = ss.id\s
                	WHERE
                		ms.canceled != 1\s
                		OR ms.canceled IS NULL\s
                	) AS sections
                	LEFT JOIN uf_trainSectionLog AS logs ON sections.id = logs.fb\s
                WHERE
                	logs.id IS NULL
                """;
        recordSetS.execute(sqlSection);
        organizations = new HashMap<>(recordSetD.getCounts() + recordSetS.getCounts());

        while (recordSetS.next()) {
            Integer id = Util.getIntValue(recordSetS.getInt("id")) * 2 +1;
            String leadCode = Util.null2String(recordSetS.getString("leadCode"));
            Organization organization = new Organization(
                    Util.null2String(recordSetS.getString("subcompanycode")),
                    Util.null2String(recordSetS.getString("subcompanyname")),
                    leadCode.equals("")?ROOT_CODE:leadCode
            );
            if (organization.getOrganizeCode().equals(""))
                supezetLog.log("分部id=>"+id+"名称"+organization.getOrganizeName()+"的分部编号为空,请维护");
            else
                organizations.put(id,organization);
        }

        while (recordSetD.next()) {
            Integer id = Util.getIntValue(recordSetD.getInt("id")) * 2;
            String leadCode = Util.null2String(recordSetD.getString("leadDCode"));
            Organization organization = new Organization(
                    Util.null2String(recordSetD.getString("departmentcode")),
                    Util.null2String(recordSetD.getString("departmentname")),
                    leadCode.equals("")?Util.null2String(recordSetD.getString("subcompanycode")):leadCode
            );
            if (organization.getOrganizeCode().equals(""))
                supezetLog.log("部门id=>"+id+"名称"+organization.getOrganizeName()+"的部门编号为空,请维护");
            else
                organizations.put(id,organization);
        }

        return organizations.size();
    }

    @Override
    public String getVersion() {
        return "develop";
    }


//    public int getDeleteOrganization() {
//
//    }

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
            if (Util.null2String(parentCode).equals(""))
                this.parentCode = parentCode;
            else
                this.parentCode = "*";
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
}
