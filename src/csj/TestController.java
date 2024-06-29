package csj;

import com.alibaba.fastjson.JSON;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

/**
 * @Author: 张骏山
 * @Date: 2024/6/18 13:23
 * @PackageName: csj
 * @ClassName: TestController
 * @Description: 控制器路径测试类
 * @Version: 1.0
 **/
@Path("/test")
public class TestController {

    @Path("/test")
    @Produces(MediaType.TEXT_PLAIN)
    public String test(@Context HttpServletRequest request, @Context HttpServletResponse response) {
        String test = "{test:\"test\"}";
        return test;
    }
}
