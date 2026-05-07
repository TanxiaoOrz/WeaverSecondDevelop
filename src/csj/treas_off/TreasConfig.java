package csj.treas_off;

import weaver.conn.RecordSet;
import weaver.general.Util;

/**
 * @Author: 付金明
 * @Date: 2026/5/6 11:47
 * @PackageName: csj.treas_off
 * @ClassName: TreasConfig
 * @Description: 司库单点登录
 * @Version: 1.0
 */
public class TreasConfig {
    //单例类 保证数据唯一性
    private static TreasConfig instance;

    private static TreasConfig getInstance() {
        if (instance == null) {
            instance = new TreasConfig();
        }
        return instance;
    }
    //集团客户号
    private String CIF_SEQ = "2000001188";
    private String URL = "http://38.240.1.122:8080";


    private RecordSet rs;

    /**
     * 构造函数,使用默认值
     */
    private TreasConfig() {
        readConfig();
    }

    /**
     * 获取配置属性值，若为空则返回默认值
     * @param propName 属性名
     * @param defaults 默认值
     * @return 属性值
     */
    private String  getPropsWithDefault(String propName, String defaults) {
        String value = rs.getPropValue("TreasConfig", propName);
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
        CIF_SEQ = getPropsWithDefault( "CIF_SEQ", CIF_SEQ);
        URL = getPropsWithDefault( "URL", URL);
    }

    public static void init() {
        getInstance().readConfig();
    }

    public static String getCifSeq() {
        return getInstance().CIF_SEQ;
    }


    public static String getUrl() {
        if (getInstance().URL.endsWith("/")) {
            getInstance().URL = getInstance().URL.substring(0, getInstance().URL.length() - 1);
        }
        return getInstance().URL;
    }


    


}
