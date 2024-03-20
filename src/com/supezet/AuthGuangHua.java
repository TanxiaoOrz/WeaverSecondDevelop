package com.supezet;

import org.jetbrains.annotations.NotNull;
import weaver.conn.RecordSet;
import weaver.general.Util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @Author: 张骏山
 * @Date: 2024/2/29 11:27
 * @PackageName: com.supezet
 * @ClassName: AuthGuangHua
 * @Description: 培训系统时代光华单点登录
 * @Version: 1.0
 **/
public class AuthGuangHua {



    private static final char[] HEX_DIGITS = {'0', '1', '2', '3', '4', '5', '6',
            '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};


    private final String secret;

    private final String corpCode;


    public AuthGuangHua(String secret, String corpCode) {
        this.secret = secret;
        this.corpCode = corpCode;
    }

    /**
     * 通过oa用户编号获取培训系统用户名(人员姓名)
     * @param userId oa用户编号
     * @return 培训系统用户名字符串
     */
    public String getUserName(int userId) {
        RecordSet set = new RecordSet();
        String userName = "";
        String sql = "select dlm from uf_transLogin where  ry=" + userId;
        set.execute(sql);
        if (set.next()) {
            userName = Util.null2String(set.getString(1));
            return userName;
        }
        return "";
    }

    /**
     * 通过用户编号生成访问链接的参数
     * @param userId oa用户编号
     * @return 带连接符的访问来链接字符串
     */
    public String getParam(int userId) {
        try {
            supezetLog.log("用户编号 => "+userId+" 获取访问培训系统参数");
            String userName = getUserName(userId);
            return getParam(userName);
        } catch (Exception e) {
            supezetLog.log("生成param失败,验证异常:"+e.getMessage());
            return "";
        }

    }

    /**
     * 通过用户名生成访问链接的参数
     * @param userName 培训系统用户名字符串
     * @return 带连接符的访问来链接字符串
     * @throws UnsupportedEncodingException 一般不会出现
     * @throws NoSuchAlgorithmException 一般不会出现
     */
    @NotNull
    private String getParam(String userName) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        String timeStamp = String.valueOf(System.currentTimeMillis());
        String plaintext = secret + "|sso|" + corpCode + "|" + userName + "|" + timeStamp + "|" + secret;
        String ciphertext = md5(plaintext);
        String param = "?userName=" + URLEncoder.encode(userName,"utf-8") + "&timestamp=" + timeStamp + "&corpCode=" + corpCode + "&sign=" + ciphertext;
        supezetLog.log("用户名 = "+userName+" 获取访问培训系统参数");
        supezetLog.log("时间戳 = "+timeStamp);
        supezetLog.log("明文 = " + plaintext);
        supezetLog.log("密文 = " + ciphertext);
        supezetLog.log("携带参数 = " + param);
        return param;
    }

    /**
     * md5加密
     * @param input 明文
     * @return md5密文
     * @throws NoSuchAlgorithmException 一般不会出现
     */
    private static String md5(String input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(input.getBytes(StandardCharsets.UTF_8));
        return toHaxString(md.digest());
    }

    /**
     * 将byte数组转化成16进制字符串
     * @param digest 字符串byte数组
     * @return 十六进制字符串
     */
    private static String toHaxString(byte[] digest) {
        StringBuilder s = new StringBuilder(digest.length * 2);
        for (byte b:
             digest) {
            s.append(HEX_DIGITS[b >> 4 & 0x0F] );
            s.append(HEX_DIGITS[b & 0x0F]);
        }
        return s.toString();
    }

}
