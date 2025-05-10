package ISS.util;

/**
 * @Author: 张骏山
 * @Date: 2025/5/10 16:38
 * @PackageName: ISS.util
 * @ClassName: ConfigUtil
 * @Description: E10的配置信息
 * @Version: 1.0
 */
public class ConfigUtil {
    private static String E10_URL = "https://oasandbox.oneiss.cn";
    private static String E10_APP_KEY = "3b184b8775f89a270eb2283ce42bfa73";
    private static String E10_APP_SECURITY = "a74bc2fccef07db9b9ba9c61a78afb7c";
    private static String E10_CORP_ID = "483160fc5581589b398c3438fcbb7f68";

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
