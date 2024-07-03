package hotswap;

import java.lang.management.ManagementFactory;

/**
 * @Author: 张骏山
 * @Date: 2024/7/3 13:48
 * @PackageName: hotswap
 * @ClassName: HotSwapUtils
 * @Description: 热更新工具类
 * @Version: 1.0
 **/
public class HotSwapUtils {

    public static String locateCurrentPID() {
        return ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
    }
}
