package com.customization.test;

import com.customization.commons.LocalTestAction;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author liutaihong
 * @version 1.0.0
 * @ClassName TestAction.java
 * @Description TODO
 * @createTime 2020-05-15 10:55:00
 */
public class TestAction  extends BaseTest {

    /**
     * 测试流程接口
     * @throws Exception
     */
    @Test
    public void testMM050_node688_after() throws Exception {
        LocalTestAction test = new LocalTestAction(2116237, "com.customization.action.demo.MM050_node688_after");
        test.setLastOperator(63);
        test.setSrc("submit");
        test.setRemark("系统管理员本地测试提交流程");
        Assert.assertEquals(test.execute(), "1");
    }


    /**
     * 测试流程接口
     * @throws Exception
     */
    @Test
    public void testAngelHybrisIT14Action() throws Exception {
        LocalTestAction test = new LocalTestAction(2242342, "weaver.angel.action.AngelHybrisIT14Action");
        test.setLastOperator(236);
        test.setSrc("submit");
        test.setRemark("系统管理员本地测试提交流程");
        Assert.assertEquals(test.execute(), "1");
    }
}
