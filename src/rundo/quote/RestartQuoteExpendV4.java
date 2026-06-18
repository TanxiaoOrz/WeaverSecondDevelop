package rundo.quote;

import rundo.util.ApiUtilV6;
import rundo.util.Console;
import weaver.conn.RecordSet;
import weaver.formmode.customjavacode.AbstractModeExpandJavaCodeNew;
import weaver.general.Util;
import weaver.hrm.User;
import weaver.soa.workflow.request.RequestInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: 张骏山
 * @Date: 2026/5/22 14:08
 * @PackageName: rundo.quote
 * @ClassName: RestartQuoteExpendV4
 * @Description: 报价重启按钮页面拓展
 * @Version: 1.0
 */

public class RestartQuoteExpendV4 extends AbstractModeExpandJavaCodeNew {

    /**
     * 部署前执行内容说明
     * 1. 注册appid : INSERT INTO ECOLOGY_BIZ_EC(ID,APPID,NAME) VALUES('1','rundo','润东医疗Ecology');
     * 2. 生成公钥和私钥对 /api/ec/dev/auth/regist {header:{"appid":"rundo", "cpk":"${RSA.getPublicKeyBase64()}"}}
     */

    private String formName;
    private String BDSColumnName;
    private String aimNodeColumn;
    private String requestIdColumn;
    private String interventionRemark = "退回重启";
    private int customId;

    public RestartQuoteExpendV4() {
        super();
        formName = ApiUtilV6.getPropsWithDefault("formName", formName);
        BDSColumnName = ApiUtilV6.getPropsWithDefault("BDSColumnName", BDSColumnName);
        aimNodeColumn = ApiUtilV6.getPropsWithDefault("aimNodeColumn", aimNodeColumn);
        requestIdColumn = ApiUtilV6.getPropsWithDefault("requestIdColumn", requestIdColumn);
        customId = Util.getIntValue(ApiUtilV6.getPropsWithDefault("customId", String.valueOf(customId)));
    }


    /**
     * 删除本行数据, 干预流程回到BDS分发节点
     *
     * @param map param包含(但不限于)以下数据
     *            user 当前用户
     *            importtype 导入方式(仅在批量导入的接口动作会传输) 1 追加，2覆盖,3更新，获取方式(int)param.get("importtype")
     *            导入链接中拼接的特殊参数(仅在批量导入的接口动作会传输)，比如a=1，可通过param.get("a")获取参数值
     *            页面链接拼接的参数，比如b=2,可以通过param.get("b")来获取参数
     * @return 执行结果
     */
    @Override
    public Map<String, String> doModeExpand(Map<String, Object> map) {
        try {
            User user = (User) map.get("user");
            int uid = user.getUID();
            RequestInfo requestInfo = (RequestInfo) map.get("RequestInfo");
            if (requestInfo == null) {
                return ApiUtilV6.rtnError("RequestInfo 为空, 请联系管理员");
            }
            int billid = Util.getIntValue(requestInfo.getRequestid());
            int modeid = Util.getIntValue(requestInfo.getWorkflowid());
            if (billid < 0 || modeid < 0 || uid < 0) {
                return ApiUtilV6.rtnError("关键数据获取失败, 请联系管理员",
                        "关键数据获取失败, billid, modeid, uid 分别为: " + billid + ", " + modeid + ", " + uid);
            }

            RecordSet recordSet = new RecordSet();
            recordSet.executeQuery("select " + BDSColumnName + ", " + aimNodeColumn + ", " + requestIdColumn + " from " + formName + " where id = " + billid);
            Console.log("select " + BDSColumnName + ", " + aimNodeColumn + ", " + requestIdColumn + " from " + formName + " where id = " + billid);
            if (recordSet.next()) {
                String BDSs = recordSet.getString(BDSColumnName);
                int aimNodeId = recordSet.getInt(aimNodeColumn);
                int requestId = recordSet.getInt(requestIdColumn);
                if (BDSs.isEmpty()) {
                    return ApiUtilV6.rtnError("BDS为空, 请联系管理员");
                }
                if (aimNodeId < 0) {
                    return ApiUtilV6.rtnError("目标节点为空, 请联系管理员");
                }
                if (requestId < 0) {
                    return ApiUtilV6.rtnError("对应流程为空, 请联系管理员");
                }

                if (!ApiUtilV6.getInstance().interventionRequest(requestId, BDSs, aimNodeId, uid, interventionRemark)) {
                    return ApiUtilV6.rtnError("干预流程回到BDS分发节点失败, 请联系管理员");
                }
                if (!ApiUtilV6.getInstance().deleteData(billid, modeid, uid, customId)) {
                    return ApiUtilV6.rtnError("删除本条数据失败, 请联系管理员");
                }
            }
        } catch (Exception e) {
            return ApiUtilV6.rtnError("代码执行报错, 请联系管理员", "代码执行报错 => " + e.getMessage());
        }
        return new HashMap<>();
    }

}
