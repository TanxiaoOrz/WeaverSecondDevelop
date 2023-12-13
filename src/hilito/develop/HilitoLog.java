package hilito.develop;

import weaver.conn.RecordSet;
import weaver.general.GCONST;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Author: 张骏山
 * @Date: 2023/12/11 10:27
 * @PackageName: hilito.develop
 * @ClassName: HilitoLog
 * @Description: 自定义开发的日志输出类
 * @Version: 1.0
 **/
public class HilitoLog {

    /**
     * 写入日志文件
     * @param logStr 日志字符串
     */
    public static void log(String logStr) {
        StackTraceElement[] stacks = new Throwable().getStackTrace();
        String className = stacks[1].getClassName();
        String method = stacks[1].getMethodName();
        int number = stacks[1].getLineNumber();
        String actionFileName = stacks[1].getFileName();
        write(logStr, "log", className, method, number, actionFileName);
    }


    /**
     *
     * @param logStr
     * @param type
     * @param className
     * @param method
     * @param number
     * @param actionFileName
     */
    private static void write(String logStr, String type, String className, String method, int number, String actionFileName) {

        actionFileName = actionFileName.substring(0, actionFileName.indexOf("."));

        SimpleDateFormat CurrentDate = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat CurrentTime = new SimpleDateFormat("HH:mm:ss.SSS");

        SimpleDateFormat CurrentHour = new SimpleDateFormat("HH");
        Date date = new Date();
        String thisHour = CurrentHour.format(date);
        String thisDate = CurrentDate.format(date);
        String thisTime = CurrentTime.format(date);

        String log = type + ":m=" + method + ";n=" + number + ";t=" + thisTime + "===>>";

        String logPath = GCONST.getRootPath() + "log" + File.separatorChar + "devlog";
        File dir = new File(logPath);
        // writeLog(logPath);
        if (!dir.exists()) {
            dir.mkdir();
            System.out.println("创建" + logPath);
        }

        logPath = GCONST.getRootPath() + "log" + File.separatorChar + "devlog" + File.separatorChar + thisDate;
        dir = new File(logPath);
        // writeLog(logPath);
        if (!dir.exists()) {
            dir.mkdir();
            System.out.println("创建" + logPath);
        }

        logPath = GCONST.getRootPath() + "log" + File.separatorChar + "devlog" + File.separatorChar + thisDate
                + File.separator + actionFileName;

        dir = new File(logPath);
        // writeLog(logPath);
        if (!dir.exists()) {
            dir.mkdir();
            System.out.println("创建" + logPath);
        }

        String dirStr = logPath + File.separatorChar + thisHour + ".log";
        // writeLog("dirStr:"+dirStr);
        File fileName = new File(dirStr);
        if (!fileName.exists()) {
            try {
                fileName.createNewFile();
                writeLog(fileName, "m=方法名(method);n=行数(number);t=执行时间(time)==>>打印日志（log）");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        RecordSet recordSet = new RecordSet();

        String allNoPrintLogClass = recordSet.getPropValue("NoPrintLog", "className");
        if (allNoPrintLogClass.contains(className)) {
            // Log(fileName, log+
            // "日志未打印，如需打开请从配置文件NoPrintLog中删除\""+className+"\"");

        } else {
            writeLog(fileName, log + logStr);
        }

    }


    /**
     * 将格式化后的文件内容添加到指定的文件对象中
     * @param fileName 被写入的文件对象
     * @param log 写入的日志内容
     */
    private static void writeLog(File fileName, String log) {
        try {
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(fileName, true), StandardCharsets.UTF_8));

            writer.write(log + "\r\n");
            if (writer != null)
                writer.close();
            if (writer != null)
                writer.close();
        } catch (IOException e) {
            e.printStackTrace();

        } finally {

        }
    }

}
