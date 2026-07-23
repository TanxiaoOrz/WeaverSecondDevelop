package csj.E10;

import weaver.conn.RecordSet;
import weaver.general.Util;

import java.util.Map;
import java.util.HashMap;

/**
 * @Author: 张骏山
 * @Date: 2025/11/25 11:47
 * @PackageName: csj.E10
 * @ClassName: E10Config.properties
 * @Description: 长三角e10配置类
 * @Version: 1.0
 */
public class E10Config {

    // Map模式 按code维护不同实例
    private static Map<String, E10Config> instanceMap = new HashMap<>();

    public static E10Config getInstance(String code) {
        if (code == null) {
            code = "";
        }
        if (!instanceMap.containsKey(code)) {
            instanceMap.put(code, new E10Config(code));
        }
        return instanceMap.get(code);
    }

    // 无参获取兼容为空字符串
    public static E10Config getInstance() {
        return getInstance("");
    }

    private String code;

    private String E10_URL = "http://223.167.85.43:8128/";
    private String E10_APP_KEY = "266849111ef138e557341f5082b627bc";
    private String E10_APP_SECURITY = "df84cee6c465219ddb0f4110b8dd1dfa";
    private String E10_CORP_ID = "cb95b1479996ded0986076afb814c4bd";
    private String authType = "loginID";

    private RecordSet rs;

    /**
     * 构造函数,使用默认值
     */
    public E10Config() {
        this("");
    }

    /**
     * 构造函数,指定code
     * @param code 配置前缀
     */
    public E10Config(String code) {
        this.code = (code != null) ? code : "";
        readConfig();
    }

    /**
     * 获取配置属性值，若为空则返回默认值
     * @param propName 属性名
     * @param defaults 默认值
     * @return 属性值
     */
    private String getPropsWithDefault(String propName, String defaults) {
        // propName增加code前缀
        String prefixedPropName = (code != null && !code.isEmpty()) ? code + propName : propName;
        String value = rs.getPropValue("E10Config", prefixedPropName);
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
        E10_URL = getPropsWithDefault("E10_URL", E10_URL);
        authType = getPropsWithDefault("authType", authType);
        E10_APP_KEY = getPropsWithDefault("E10_APP_KEY", E10_APP_KEY);
        E10_APP_SECURITY = getPropsWithDefault("E10_APP_SECURITY", E10_APP_SECURITY);
        E10_CORP_ID = getPropsWithDefault("E10_CORP_ID", E10_CORP_ID);
    }

    public static void init() {
        getInstance().readConfig();
    }

    public static void init(String code) {
        getInstance(code).readConfig();
    }


    public static String getE10AppKey(String code) {
        return getInstance(code).E10_APP_KEY;
    }

    public static String getE10AppSecurity(String code) {
        return getInstance(code).E10_APP_SECURITY;
    }


    public static String getE10CorpId(String code) {
        return getInstance(code).E10_CORP_ID;
    }


    public static String getE10Url(String code) {
        if (getInstance(code).E10_URL.endsWith("/")) {
            getInstance(code).E10_URL = getInstance(code).E10_URL.substring(0, getInstance(code).E10_URL.length() - 1);
        }
        return getInstance(code).E10_URL;
    }

    /**
     * 获取认证类型
     * @param code 配置前缀
     * @return 认证类型
     */
    public static String getAuthType(String code) {
        return getInstance(code).authType;
    }

    // ===== 非静态实例方法,供持有E10Config实例的调用方使用 =====

    public String getE10Url() {
        if (this.E10_URL.endsWith("/")) {
            this.E10_URL = this.E10_URL.substring(0, this.E10_URL.length() - 1);
        }
        return this.E10_URL;
    }

    public String getE10AppKey() {
        return this.E10_APP_KEY;
    }

    public String getE10AppSecurity() {
        return this.E10_APP_SECURITY;
    }

    public String getE10CorpId() {
        return this.E10_CORP_ID;
    }

    public String getAuthType() {
        return this.authType;
    }

}