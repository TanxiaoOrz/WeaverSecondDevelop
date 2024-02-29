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

    private static final String SECRET = "";

    private static final String CORP_CODE = "";

    private static final String DEFAULT_USERNAME = "";

    private static final String USERNAME_FIELD = "";

    /**
     * 通过oa用户编号获取培训系统用户名
     * @param userId oa用户编号
     * @return 培训系统用户名字符串
     */
    public String getUserName(int userId) {
        RecordSet set = new RecordSet();
        String userName = "";
        String sql = "select " + USERNAME_FIELD + " from cus_fielddata where scope='HrmCustomFieldByInfoType' and scopeid=-1 and id=" + userId;
        set.execute(sql);
        if (set.next()) {
            userName = Util.null2String(set.getString(1));
        }
        return "".equals(userName)?DEFAULT_USERNAME:userName;
    }

    /**
     * 通过用户编号生成访问链接的参数
     * @param userId oa用户编号
     * @return 带连接符的访问来链接字符串
     */
    public String getParam(int userId) {
        try {
            String userName = getUserName(userId);
            return getParam(userName);
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
            supezetLog.log("生成param失败,验证异常:"+e.getMessage());
            throw new RuntimeException(e);
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
    private static String getParam(String userName) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        String timeStamp = String.valueOf(System.currentTimeMillis());
        String plaintext = SECRET + "|sso|" + CORP_CODE + "|" + userName + "|" + timeStamp + "|" + SECRET;
        String ciphertext = md5(plaintext);
        String param = "?userName=" + URLEncoder.encode(userName,"utf-8") + "&timestamp=" + timeStamp + "&corpCode=" + CORP_CODE + "&sign=" + ciphertext;
        supezetLog.log("用户");
        return "";
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
            s.append(HEX_DIGITS[b] >> 4 & 0x0F);
            s.append(HEX_DIGITS[b] & 0x0F);
        }
        return s.toString();
    }

}
