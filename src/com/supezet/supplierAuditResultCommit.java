package com.supezet;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONObject;
import weaver.conn.RecordSet;
import weaver.general.TimeUtil;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.RequestInfo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @Author 张骏山
 * @Date 2023-8-25 11:36:00
 * @PackageName: com.customization.action
 * @ClassName: syncCompanyAudit
 * @Description: 将供应商审批的信息回传至erp系统
 * @Version 1.0
 */
public class supplierAuditResultCommit implements Action{

    @Override
    public String execute(RequestInfo requestInfo) {
        // http参数声明
        String auditType;
        String companyCode;
        String auditNo;
        String auditResultCode;
        String auditResultMsg;
        String auditTime;

        RecordSet rs = new RecordSet();
        rs.execute(getSearchInformation(requestInfo));
        supezetUtil supUtil = new supezetUtil();
        String domain = supUtil.getDomain();
        String token = supUtil.getToken();
        String api = "/external-api/user-center/external/syncCompanyAudit";
        final String empty = "";
        if (rs.next()) {
            supezetLog.log("y");
            auditType = String.valueOf(rs.getInt("auditType"));
            companyCode = rs.getString("companyId");
        } else {
            supezetLog.log("n");
            auditType = empty;
            companyCode = empty;
        }
        auditNo = requestInfo.getRequestid();
        auditResultCode = "1";
        auditResultMsg = removeHtmlTag(requestInfo.getRequestManager().getRemark());
        auditTime = TimeUtil.getCurrentTimeString();

        JSONObject param = new JSONObject();
        JSONObject rtnJson;

        try {
            param.put("auditType",auditType);
            param.put("companyCode",companyCode);
            param.put("auditNo",auditNo);
            param.put("auditResultCode", auditResultCode);
            param.put("auditResultMsg",auditResultMsg);
            param.put("auditTime",auditTime);

            OkHttpClient client = new OkHttpClient().newBuilder().build();
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, param.toString());
            Request requestPost = new Request.Builder().url(domain + api).method("POST", body)
                    .addHeader("mall-sso-token", token)
                    .addHeader("Content-Type", "application/json").build();
            Response response = client.newCall(requestPost).execute();

            supezetLog.log("请求报文=>"+param);
            rtnJson = new JSONObject(response.body().string());
            supezetLog.log("接口调用成功返回=>"+rtnJson);

            boolean success = rtnJson.getBoolean("success");
            String code = rtnJson.getString("code");
            String message = rtnJson.getString("message");

            if(success && code.equals("200")) {
                return Action.SUCCESS;
            }else {
                requestInfo.getRequestManager().setMessagecontent(message);
                return Action.FAILURE_AND_CONTINUE;
            }

        } catch (Exception e) {
            supezetLog.log("异常："+e);
            requestInfo.getRequestManager().setMessagecontent("异常："+e);
            return Action.FAILURE_AND_CONTINUE;
        }
    }

    /**
     * @Description: 获取审批流相关信息
     * @return 数据库查询结果
     */
    private String getSearchInformation(RequestInfo requestInfo) {
        String sql = "select companyId,auditType from ? where requestid = ? ";
        String tableName = requestInfo.getRequestManager().getBillTableName();
        String requestId = requestInfo.getRequestid();
        String auditType = "auditType";
        String s = replaceString(sql, new String[]{tableName, requestId, auditType});
        supezetLog.log("sql=>"+s);
        return s;
    }

    private String replaceString(String sql,String[] args) {
        if (!sql.endsWith(";")) {
            sql = sql.split(";")[0];
        }

        String[] sqlGroup = sql.split("\\?");

        if (sqlGroup.length < args.length + 1) {
            StringBuilder sqlConcat = new StringBuilder("");
            int i;
            for (i = 0; i < sqlGroup.length-1; i++) {
                sqlConcat.append(sqlGroup[i]);
                sqlConcat.append(args[i]);
            }
            sqlConcat.append(sqlGroup[i]);
            supezetLog.log("sql=>"+sqlConcat);
            if (sql.endsWith("?")) {
                sqlConcat.append(args[i]);
            }
            return sqlConcat.toString();
        } else
            return null;
    }

    private String removeHtmlTag(String s) {
        String html = "<[^>]+>";
        Pattern pHtml = Pattern.compile(html,Pattern.CASE_INSENSITIVE);
        Matcher mHtml = pHtml.matcher(s);
        return mHtml.replaceAll("");
    }

//    public static void main(String[] args) {
//        System.out.println(new supplierAuditResultCommit().removeHtmlTag("<p>tongyi</p>"));
//    }
}
