package com.customization.proxy;

import com.customization.commons.Console;
import com.engine.core.cfg.annotation.CommandDynamicProxy;
import com.engine.core.interceptor.AbstractCommandProxy;
import com.engine.core.interceptor.Command;
import com.engine.cube.cmd.resource.ResourceViewCmd;

import java.util.Map;

/**
 * @author liutaihong
 * @version 1.0.0
 * @ClassName CustomResourceViewCmd.java
 * @Description TODO
 * @createTime 2020-04-29 11:51:00
 */
@CommandDynamicProxy(target = ResourceViewCmd.class, desc="重写resourceView.cmd")
public class CustomResourceViewCmd extends AbstractCommandProxy<Map<String,Object>> {
    @Override
    public Map<String, Object> execute(Command<Map<String, Object>> targetCommand) {
        System.out.println(getClass().getName() + "command 执行之前做一些事");

        //获取到被代理对象
        ResourceViewCmd rmCmd = (ResourceViewCmd) targetCommand;
        //获取被代理对象的参数
         Map<String, Object> params = rmCmd.getParams();
       /* params.put("sqlwhere","id = t2.sourceid and (t2.sourceid in " +
                "(select sbmc from uf_gxsboayy where ((startdate >= '2020-02-02' and startdate <= '2020-02-08') " +
                "or (startdate >= '2020-02-02' and enddate <= '2020-02-08' )or (enddate >= '2020-02-02' and enddate <= '2020-02-08') " +
                "or (startdate <= '2020-02-02' and enddate >= '2020-02-08')))or t2.sourceid in (select id from uf_yfsb where zyjb =0 )) " );

      */  Console.log(params.toString());

        //对参数做预处理
        //TODO
        //参数回写
        //rmCmd.setParams(params);
        //执行标准的业务处理
        Map<String, Object> result = nextExecute(targetCommand);

        //对返回值做加工处理
        result.put("我是Test的key", "我是");
        result.put("params", params);


        System.out.println(getClass().getName() + "command 执行之后做一些事");

        return result;
    }
}
