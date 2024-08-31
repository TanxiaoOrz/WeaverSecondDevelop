package csj.fileDecode;


import com.system.file.decrypt.DecryptFile;
import org.apache.commons.io.FileUtils;
import weaver.file.util.FileDeleteUtil;

import java.io.File;

/**
 * @Author: 张骏山
 * @Date: 2024/8/31 21:10
 * @PackageName: csj.fileDecode
 * @ClassName: DecodeFile
 * @Description: 蓝凌文件解密类
 * @Version: 1.0
 */
public class DecodeFile {



    public static void main(String[] args) {
        // 原物理文件路径：/home/resource/2023/11/4/18bfa97f8d70d364be880344f529b331
        File srcFile = new File("/home/resource/2023/11/4/18bfa97f8d70d364be880344f529b331");

        if (srcFile.exists()) {
            // 解密后物理文件
            File decryptFile = null;
            try {
                // 原文件加密时，返回的文件路径：/home/resource/2023/11/4/18bfa97f8d70d364be880344f529b331
                // 原文件为加密时，返回的文件路径：/home/resource/2023/11/4/18bfa97f8d70d364be880344f529b331_DecryptWkj
                decryptFile = DecryptFile.decryptFile1(srcFile);

                // ===============================具体逻辑项目开发自行处理，以下仅为示例===================================

                // 拷贝解密后的文件到指定路径，例：/home/newfile/01 双碳政策与规范专项汇编报告.pdf
                // 注意：只做复制文件，不做移动文件
                File newFile = new File("/home/newfile/01 双碳政策与规范专项汇编报告.pdf");
                File parentDir = newFile.getParentFile();
                // 如果父目录不存在，则创建它
                if (parentDir != null && !parentDir.exists()) {
                    parentDir.mkdirs();
                }
                FileUtils.copyFile(decryptFile, newFile);

                // ===============================具体逻辑项目开发自行处理，以上仅为示例===================================

            } catch (Exception e) {
                System.out.println("===文件解密失败===filePath: " + srcFile.getAbsolutePath());
                e.printStackTrace();
            } finally {
                if (decryptFile.getName().endsWith("_DecryptWkj")) {
                    new FileDeleteUtil().deleteFile(decryptFile);
                }
            }
        }
    }

    public String[]

}

class FileBefore {
    String fd_model_id;
    String fd_id;
    String fd_name;
    String fd_file_path;

    static FileBefore[] initialFromIds(String[] fd_model_ids) {
        String sqlQuery = "select m.fd_model_id,fd_file_name,m.fd_id,f.fd_file_path from SYS_ATT_MAIN m left join sys_att_file f on m.fd_file_id = f.fd_id";
    }
}
