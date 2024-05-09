package hotswap;

/**
 * @Author: 张骏山
 * @Date: 2024/4/15 14:58
 * @PackageName: hotswap
 * @ClassName: VersionControl
 * @Description: 版本获取接口
 * @Version: 1.0
 **/
public interface VersionControl {
    /**
     * 获取当前版本信息
     * @return 版本信息字符串
     */
    String getVersion();
}
