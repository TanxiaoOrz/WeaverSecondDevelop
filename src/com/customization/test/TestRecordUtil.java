package com.customization.test;


import com.customization.commons.Console;
import org.junit.Test;
import weaver.conn.RecordSet;

/**
 * @author liutaihong
 * @version 1.0.0
 * @ClassName TestRecordUtil.java
 * @Description 本地调用服务器的实例,
 * @createTime 2020-04-28 14:31:00
 */
public class TestRecordUtil extends BaseTest {


    @Test
    public void with() {
        String sql = "select COMPANYNAME,LICENSE,EXPIREDATE,CVERSION from  license ";
        RecordSet rs = new RecordSet();
        rs.executeQuery(sql);
        if (rs.next()) {
            System.out.println("公司名称:" + rs.getString(1));
            System.out.println("LICENSE:" + rs.getString(2));
            System.out.println("授权到期日期:" + rs.getString(3));
            System.out.println("版本:" + rs.getString(4));
            //Console.log(sql);
        }
    }


}
