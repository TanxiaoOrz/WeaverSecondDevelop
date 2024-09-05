package csj.fileDecode;


import com.system.file.decrypt.DecryptFile;
import csj.utils.Console;
import org.apache.commons.io.FileUtils;
import weaver.conn.RecordSetDataSource;
import weaver.file.util.FileDeleteUtil;
import weaver.general.Util;
import weaver.interfaces.schedule.BaseCronJob;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;


/**
 * @Author: 张骏山
 * @Date: 2024/8/31 21:10
 * @PackageName: csj.fileDecode
 * @ClassName: QueryTest6
 * @Description: 蓝凌文件解密类
 * @Version: 1.0
 */
public class QueryTestCustomXinYuan extends BaseCronJob {

    public static final String BASIC_DEC_PATH = "/home/resource";
    public static final String BASIC_NEW_PATH = "/home/new";

    public static String typePath = "/ZidingyiXY/";

    public String idsQuerySql =
            "SELECT\n" +
            "\tfd_id \n" +
            "FROM\n" +
            "\tkm_review_main \n" +
            "WHERE\n" +
            "\tfd_template_id IN ( SELECT fd_id FROM km_review_template WHERE fd_name IN ('合同（协议）会签审核单【信苑】', '信苑印章使用审批表') ) \n" +
            "\tAND doc_create_time < '2024-08-01' \n" +
            "\tAND auth_area_id = '179936464b7019e946d2f9241b585781' UNION\n" +
            "SELECT\n" +
            "\tfd_id \n" +
            "FROM\n" +
            "\tkm_imissive_sign_main \n" +
            "WHERE\n" +
            "\tfd_template_id IN ( SELECT fd_id FROM km_imissive_signtemp WHERE fd_name IN ( '信苑签报' ) ) \n" +
            "\tAND doc_create_time < '2024-08-01' \n" +
            "\tAND auth_area_id = '179936464b7019e946d2f9241b585781' ";


    public void executeJob()  {
        Console.log("Mission Start");
        Console.log("typePath=>"+typePath);
        Console.log("idsQueryString=>" + idsQuerySql);
        List<String> ids = getFdModelIds();
//        Console.log("ids=>"+ids.toString());
        List<FileBefore> fileBefores = initialFromIds(ids);
//        Console.log("fileBefores=>\n"+fileBefores);
        fileBefores.forEach(fileBefore -> {
            Console.log(fileBefore.toString());
            if (translateFile(fileBefore)) {
                Console.log("success:"+fileBefore.getFdId());
            } else {
                Console.log("failure:"+fileBefore.getFdId());
            }
        });
        Console.log("Mission Finish");
    }


    public QueryTestCustomXinYuan() {
    }

    private List<String> getFdModelIds() {
        RecordSetDataSource ekpTest = new RecordSetDataSource("ekpTest");
        ekpTest.execute(idsQuerySql);
        ArrayList<String> IDS = new ArrayList<>();
        while (ekpTest.next()){
            IDS.add(Util.null2String(ekpTest.getString("fd_id")));
        }
        return IDS;
    }

    private boolean translateFile(FileBefore fileBefore) {
        File srcFile = new File(BASIC_DEC_PATH + fileBefore.getFilePath());
        if (srcFile.exists()) {
            boolean returns = false;
            // 解密后物理文件
            File decryptFile = null;
            try {
                // 原文件加密时，返回的文件路径：/home/resource/2023/11/4/18bfa97f8d70d364be880344f529b331
                // 原文件为加密时，返回的文件路径：/home/resource/2023/11/4/18bfa97f8d70d364be880344f529b331_DecryptWkj
                decryptFile = DecryptFile.decryptFile1(srcFile);

                // ===============================具体逻辑项目开发自行处理，以下仅为示例===================================

                // 拷贝解密后的文件到指定路径，例：/home/newfile/01 双碳政策与规范专项汇编报告.pdf
                // 注意：只做复制文件，不做移动文件
                int counts = 0;
                File newFile = new File(fileBefore.getFileName(counts));
                while (newFile.exists()) {
                    counts ++;
                    newFile = new File(fileBefore.getFileName(counts));
                }
                Console.log(fileBefore.getFileName(counts));
                File parentDir = newFile.getParentFile();
                // 如果父目录不存在，则创建它
                if (parentDir != null && !parentDir.exists()) {
                    parentDir.mkdirs();
                }
                FileUtils.copyFile(decryptFile, newFile);
                returns = true;
                // ===============================具体逻辑项目开发自行处理，以上仅为示例===================================

            } catch (Exception e) {
                System.out.println("===文件解密失败===filePath: " + srcFile.getAbsolutePath());
                Console.log("===文件解密失败===filePath: " + srcFile.getAbsolutePath());
                e.printStackTrace();
                Console.log(e.getClass().getName() + ": " + e.getMessage());
                StringBuilder sbException = new StringBuilder();
                for (StackTraceElement ele : e.getStackTrace()) {
                    sbException.append(MessageFormat.format("\tat {0}.{1}({2}:{3})\n",
                            ele.getClassName(), ele.getMethodName(), ele.getFileName(), ele.getLineNumber()));;
                }
                Console.log(sbException.toString());
            } finally {
                if (decryptFile != null && decryptFile.getName().endsWith("_DecryptWkj")) {
                    new FileDeleteUtil().deleteFile(decryptFile);
                }
            }
            return returns;
        }
        return false;
    }

    private List<FileBefore> initialFromIds(List<String> fd_model_ids) {
        String sqlQuery = "select m.fd_model_id,m.fd_file_name,concat(m.fd_id,'_', f.fd_id) as fd_id,f.fd_file_path from SYS_ATT_MAIN m left join sys_att_file f on m.fd_file_id = f.fd_id where m.fd_model_id in";
        RecordSetDataSource ekpTest = new RecordSetDataSource("ekpTest");
        String execute = sqlQuery + FileBefore.arrayToString(fd_model_ids);
        Console.log(execute);
        ekpTest.execute(execute);
        ArrayList<FileBefore> fileBefores = new ArrayList<>();
        while (ekpTest.next()) {
            fileBefores.add(new FileBefore(
                    Util.null2String(ekpTest.getString("fd_model_id")),
                    Util.null2String(ekpTest.getString("fd_id")),
                    Util.null2String(ekpTest.getString("fd_file_name")),
                    Util.null2String(ekpTest.getString("fd_file_path"))
            ));
        }
        return fileBefores;
    }

    @Override
    public void execute() {
        executeJob();
    }

    static class FileBefore {
        String fd_model_id;
        String fd_id;
        String fd_name;
        String fd_file_path;



        static String arrayToString(List<String> a) {
            if (a == null)
                return "'null'";

            int iMax = a.size() - 1;
            if (iMax == -1)
                return "()";

            StringBuilder b = new StringBuilder();
            b.append('(');
            for (int i = 0; ; i++) {
                b.append("'").append(String.valueOf(a.get(i))).append("'");
                if (i == iMax)
                    return b.append(')').toString();
                b.append(", ");
            }
        }

        public FileBefore(String fd_model_id, String fd_id, String fd_name, String fd_file_path) {
            this.fd_model_id = fd_model_id;
            this.fd_id = fd_id;
            this.fd_name = fd_name;
            this.fd_file_path = fd_file_path;
        }

        public String getFilePath() {
            return fd_file_path;
        }

        public String getFileName() {
            String[] split = fd_name.split("\\.");
            String name = "";
            for (int i = 0; i < split.length -1; i++) {
                name += split[i];
                if (i!=split.length -2)
                    name += ".";
            }
            return BASIC_NEW_PATH + typePath + name + "_" + fd_model_id + "." + split[split.length-1];
        }

        public String getFileName(int index) {
            if (index == 0)
                return getFileName();
            String[] split = fd_name.split("\\.");
            String name = "";
            for (int i = 0; i < split.length -1; i++) {
                name += split[i];
                if (i!=split.length -2)
                    name += ".";
            }
            return BASIC_NEW_PATH + typePath + name+"(" + index +")" + "_" + fd_model_id + "." + split[split.length-1];
        }

        public String getFdId() {
            return fd_id;
        }

        @Override
        public String toString() {
            return "\n{" +
                    "fd_model_id='" + fd_model_id + '\'' +
                    ", fd_id='" + fd_id + '\'' +
                    ", fd_name='" + fd_name + '\'' +
                    ", fd_file_path='" + BASIC_DEC_PATH + fd_file_path + '\'' +
                    ", newFIlePath='" + BASIC_NEW_PATH + typePath + getFileName() + '\'' +
                    '}';
        }
    }

}

