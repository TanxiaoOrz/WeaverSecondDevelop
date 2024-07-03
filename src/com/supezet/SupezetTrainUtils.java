package com.supezet;

import hotswap.VersionControl;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @Author: 张骏山
 * @Date: 2024/7/1 14:04
 * @PackageName: com.supezet
 * @ClassName: SupezetTrainUtils
 * @Description: 卓然培训系统辅助工具类
 * @Version: 1.0
 **/
public class SupezetTrainUtils implements VersionControl {

    /**
     *
     * 企业ID:supezet
     * APPKEY:FAAF846F1EA5450E8D71B9B2E948AFC5
     * APPSECRET(密钥):2F6B226E1A114163A07CA5F18026FCB3
     * 单点密钥：supezet_sso_123
     */

    public static final String APP_SECRET = "2F6B226E1A114163A07CA5F18026FCB3";

    private static final char[] HEX_DIGITS = {'0', '1', '2', '3', '4', '5', '6',
            '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};


    /**
     * 获取同步接口的认证sign_
     *
     * @param requestURI 访问接口uri
     * @return 认证字符串
     */
    public static String getSign(String requestURI) throws NoSuchAlgorithmException {
        String plainText = APP_SECRET + "|" + requestURI + "|" + APP_SECRET;
        String sign_ = md5(plainText).toUpperCase();
        supezetLog.log("明文:"+plainText+"密文"+sign_);
        return sign_;
    }

    /**
     * md5加密
     * @param input 明文
     * @return md5密文
     * @throws NoSuchAlgorithmException 一般不会出现
     */
    public static String md5(String input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(input.getBytes(StandardCharsets.UTF_8));
        return toHaxString(md.digest());
    }

    /**
     * 将byte数组转化成16进制字符串
     * @param digest 字符串byte数组
     * @return 十六进制字符串
     */
    public static String toHaxString(byte[] digest) {
        StringBuilder s = new StringBuilder(digest.length * 2);
        for (byte b:
                digest) {
            s.append(HEX_DIGITS[b >> 4 & 0x0F] );
            s.append(HEX_DIGITS[b & 0x0F]);
        }
        return s.toString();
    }

    @Override
    public String getVersion() {
        return "DEVELOP-1";
    }
}
