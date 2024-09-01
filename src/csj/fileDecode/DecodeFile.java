package csj.fileDecode;


//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.system.file.decrypt.DecryptFile;
//import csj.utils.Console;
//import org.apache.commons.io.FileUtils;
//import weaver.conn.RecordSetDataSource;
//import weaver.file.util.FileDeleteUtil;
//import weaver.general.Util;
//
//import java.io.File;
//import java.text.MessageFormat;
//import java.util.ArrayList;
//import java.util.List;

/**
 * @Author: 张骏山
 * @Date: 2024/8/31 21:10
 * @PackageName: csj.fileDecode
 * @ClassName: DecodeFile
 * @Description: 蓝凌文件解密类
 * @Version: 1.0
 */
public class DecodeFile {

//    public static final String BASIC_DEC_PATH = "/home/resource";
//    public static final String BASIC_NEW_PATH = "/home/new";
//
//    public String typePath ;
//
//    public String idsQuerySql ;
//
//    public void executeJob() throws JsonProcessingException {
//        Console.log("Mission Start");
//        Console.log("typePath=>"+typePath);
//        Console.log("idsQueryString=>" + idsQuerySql);
//        List<String> ids = getFdModelIds();
//        Console.log("ids=>"+ids.toString());
//        List<FileBefore> fileBefores = initialFromIds(ids);
//        Console.log("fileBefores=>\n"+new ObjectMapper().writeValueAsString(fileBefores));
//        fileBefores.forEach(fileBefore -> {
//           if (translateFile(fileBefore.getFilePath(), fileBefore.getFileName())) {
//               Console.log("success:"+fileBefore.getFdId());
//           } else {
//               Console.log("failure:"+fileBefore.getFdId());
//           }
//        });
//        Console.log("Mission Finish");
//    }
//
//
//    public DecodeFile(String typePath,  String idsQuerySql) {
//        this.typePath = typePath;
//        this.idsQuerySql = idsQuerySql;
//    }
//
//    private List<String> getFdModelIds() {
//        RecordSetDataSource ekpTest = new RecordSetDataSource("ekpTest");
//        ekpTest.execute(idsQuerySql);
//        ArrayList<String> IDS = new ArrayList<>();
//        while (ekpTest.next()){
//            IDS.add(Util.null2String(ekpTest.getString("fd_id")));
//        }
//        return IDS;
//    }
//
//    private boolean translateFile(String filePath, String fileName) {
//        File srcFile = new File(BASIC_DEC_PATH + filePath);
//        if (srcFile.exists()) {
//            boolean returns = false;
//            // 解密后物理文件
//            File decryptFile = null;
//            try {
//                // 原文件加密时，返回的文件路径：/home/resource/2023/11/4/18bfa97f8d70d364be880344f529b331
//                // 原文件为加密时，返回的文件路径：/home/resource/2023/11/4/18bfa97f8d70d364be880344f529b331_DecryptWkj
//                decryptFile = DecryptFile.decryptFile1(srcFile);
//
//                // ===============================具体逻辑项目开发自行处理，以下仅为示例===================================
//
//                // 拷贝解密后的文件到指定路径，例：/home/newfile/01 双碳政策与规范专项汇编报告.pdf
//                // 注意：只做复制文件，不做移动文件
//                int counts = 0;
//                File newFile = new File(BASIC_NEW_PATH + typePath + fileName);
//                while (newFile.exists()) {
//                    counts ++;
//                    newFile = new File(BASIC_NEW_PATH + typePath + "("+counts+")" + fileName);
//                }
//                File parentDir = newFile.getParentFile();
//                // 如果父目录不存在，则创建它
//                if (parentDir != null && !parentDir.exists()) {
//                    parentDir.mkdirs();
//                }
//                FileUtils.copyFile(decryptFile, newFile);
//                returns = true;
//                // ===============================具体逻辑项目开发自行处理，以上仅为示例===================================
//
//            } catch (Exception e) {
//                System.out.println("===文件解密失败===filePath: " + srcFile.getAbsolutePath());
//                Console.log("===文件解密失败===filePath: " + srcFile.getAbsolutePath());
//                e.printStackTrace();
//                Console.log(e.getClass().getName() + ": " + e.getMessage());
//                StringBuilder sbException = new StringBuilder();
//                for (StackTraceElement ele : e.getStackTrace()) {
//                    sbException.append(MessageFormat.format("\tat {0}.{1}({2}:{3})\n",
//                            ele.getClassName(), ele.getMethodName(), ele.getFileName(), ele.getLineNumber()));;
//                }
//                Console.log(sbException.toString());
//            } finally {
//                if (decryptFile != null && decryptFile.getName().endsWith("_DecryptWkj")) {
//                    new FileDeleteUtil().deleteFile(decryptFile);
//                }
//            }
//            return returns;
//        }
//        return false;
//    }
//
//    private List<FileBefore> initialFromIds(List<String> fd_model_ids) {
//        String sqlQuery = "select m.fd_model_id,fd_file_name,m.fd_id + f.fd_id as fd_id,f.fd_file_path from SYS_ATT_MAIN m left join sys_att_file f on m.fd_file_id = f.fd_id where m.fd_model id in";
//        RecordSetDataSource ekpTest = new RecordSetDataSource("ekpTest");
//        String execute = sqlQuery + arrayToString(fd_model_ids);
//        ekpTest.execute(execute);
//        ArrayList<FileBefore> fileBefores = new ArrayList<>();
//        while (ekpTest.next()) {
//            fileBefores.add(new FileBefore(
//                    Util.null2String(ekpTest.getString("fd_model_id")),
//                    Util.null2String(ekpTest.getString("fd_id")),
//                    Util.null2String(ekpTest.getString("fd_name")),
//                    Util.null2String(ekpTest.getString("fd_file_path"))
//            ));
//        }
//        return fileBefores;
//    }

}
//
//class FileBefore {
//    String fd_model_id;
//    String fd_id;
//    String fd_name;
//    String fd_file_path;
//
//
//
//    static String arrayToString(List<String> a) {
//        if (a == null)
//            return "'null'";
//
//        int iMax = a.size() - 1;
//        if (iMax == -1)
//            return "()";
//
//        StringBuilder b = new StringBuilder();
//        b.append('(');
//        for (int i = 0; ; i++) {
//            b.append("'").append(String.valueOf(a.get(i))).append("'");
//            if (i == iMax)
//                return b.append(')').toString();
//            b.append(", ");
//        }
//    }
//
//    public FileBefore(String fd_model_id, String fd_id, String fd_name, String fd_file_path) {
//        this.fd_model_id = fd_model_id;
//        this.fd_id = fd_id;
//        this.fd_name = fd_name;
//        this.fd_file_path = fd_file_path;
//    }
//
//    public String getFilePath() {
//        return fd_file_path;
//    }
//
//    public String getFileName() {
//        return fd_name + "_" + fd_model_id;
//    }
//
//    public String getFdId() {
//        return fd_id;
//    }
//}
