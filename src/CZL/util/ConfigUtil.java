package CZL.util;

/**
 * @Author: 张骏山
 * @Date: 2025/5/10 16:38
 * @PackageName: ISS.util
 * @ClassName: ConfigUtil
 * @Description: E10的配置信息
 * @Version: 1.0
 */
public class ConfigUtil {
    private static String E10_URL = "https://oa.cdlshx.com/";
    private static String E10_APP_KEY = "7d29fecfd02495bae9e18e2b7bf5f9df";
    private static String E10_APP_SECURITY = "4fe292a891bd4facd6d1b446c1779f17";
    private static String E10_CORP_ID = "41f08863fb7af78f2225850b99ee4403";

    public static String getE10Url() {
        if (E10_URL.endsWith("/")) {
            E10_URL = E10_URL.substring(0, E10_URL.length() - 1);
        }
        return E10_URL;
    }

    public static String getE10AppKey() {
        return E10_APP_KEY;
    }

    public static String getE10AppSecurity() {
        return E10_APP_SECURITY;
    }

    public static String getE10CorpId() {
        return E10_CORP_ID;
    }
}
