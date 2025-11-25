package csj.E10;

import weaver.conn.RecordSet;
import weaver.general.Util;

/**
 * @Author: 张骏山
 * @Date: 2025/11/25 11:47
 * @PackageName: csj.E10
 * @ClassName: E10Config.properties
 * @Description: 长三角e10配置类
 * @Version: 1.0
 */
public class E10Config {

    private static E10Config instance;

    private static E10Config getInstance() {
        if (instance == null) {
            instance = new E10Config();
        }
        return instance;
    }
    private String E10_URL = "http://10.244.2.46:10600/";
    private String E10_APP_KEY = "266849111ef138e557341f5082b627bc";
    private String E10_APP_SECURITY = "df84cee6c465219ddb0f4110b8dd1dfa";
    private String E10_CORP_ID = "cb95b1479996ded0986076afb814c4bd";
    private String authType = "loginID";

    private RecordSet rs;

    /**
     * 构造函数,使用默认值
     */
    public E10Config() {

    }

    /**
     * 获取配置属性值，若为空则返回默认值
     * @param propName 属性名
     * @param defaults 默认值
     * @return 属性值
     */
    private String  getPropsWithDefault(String propName, String defaults) {
        String value = rs.getPropValue("E10Config.properties", propName);
        if (Util.null2String(value).equals("")) {
            value = defaults;
        }
        return value;
    }

    /**
     * 读取配置文件
     */
    private void readConfig() {
        rs = new RecordSet();
        E10_URL = getPropsWithDefault( "E10_URL", E10_URL);
        authType = getPropsWithDefault( "authType", authType);
        E10_APP_KEY = getPropsWithDefault( "E10_APP_KEY", E10_APP_KEY);
        E10_APP_SECURITY = getPropsWithDefault( "E10_APP_SECURITY", E10_APP_SECURITY);
        E10_CORP_ID = getPropsWithDefault( "E10_CORP_ID", E10_CORP_ID);
    }

    public static void init() {
        getInstance().readConfig();
    }


    public static String getE10AppKey() {
        return getInstance().E10_APP_KEY;
    }

    public static String getE10AppSecurity() {
        return getInstance().E10_APP_SECURITY;
    }

    public static String getE10CorpId() {
        return getInstance().E10_CORP_ID;
    }

    public static String getE10Url() {
        if (getInstance().E10_URL.endsWith("/")) {
            getInstance().E10_URL = getInstance().E10_URL.substring(0, getInstance().E10_URL.length() - 1);
        }
        return getInstance().E10_URL;
    }

    /**
     * 获取认证类型
     * @return 认证类型
     */
    public static String getAuthType() {
        return getInstance().authType;
    }


}
