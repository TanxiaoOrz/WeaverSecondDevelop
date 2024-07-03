package hotswap;

import weaver.conn.RecordSet;
import weaver.general.GCONST;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author liutaihong
 * @version 1.0.0
 * @ClassName Console.java
 * @Description 自己写的日志打印，UTF-8格式
 * @createTime 2020-04-29 14:31:00
 */
public class Console {

    public  static void log(String logStr) {
        StackTraceElement[] stacks = new Throwable().getStackTrace();
        //int stacksLen = stacks.length;
        String className = stacks[1].getClassName();
        String method = stacks[1].getMethodName();
        int number = stacks[1].getLineNumber();
        String actionFileName = stacks[1].getFileName();
        write(logStr,"log",className,method,number,actionFileName);
    }

    public  static void info(String logStr) {
        StackTraceElement[] stacks = new Throwable().getStackTrace();
        //int stacksLen = stacks.length;
        String className = stacks[1].getClassName();
        String method = stacks[1].getMethodName();
        int number = stacks[1].getLineNumber();
        String actionFileName = stacks[1].getFileName();
        write(logStr,"info",className,method,number,actionFileName);
    }

    /**
     * 根据action按照日期分类打印日志
     * <p>
     * 在配置文件中可以增加NoPrintLog.properties 的className键的值（多个用，隔开）来控制对应class的日志是否完整输出
     * eg：  className=com.custompage.NodeBefore
     * 那么  com.custompage.NodeBefore 里面的具体日志不会完整的被打印出来。
     * <p>
     * 适用场景：
     * 场景1:很多大量的日志在调试完成后还会被打印出来会占用很大的磁盘空间，不得不一个个去掉调试日志。搞不好就能轻松一天打印80G的ecology日志。
     * 场景2:假设去掉日志以后，出现了新的问题，再调试之前又得一个个增加打印行。调试完成后回到场景1，往返循环。
     * 场景3:原来的log日志全部打印在/log/ecology*.log文件里面，假设你的日志达到很大的时候，比如200m的时候，找日志如同大海捞针。
     * 场景4：200M的日志文件你下载下来所消耗的时间和代价是巨大的，即使直接在浏览器里面访问也是痛苦不堪的。
     * <p>
     * 关于历史action迁移：
     * 1.假定之前 extends BaseBean类来writeLog日志，只需要改为 extends  ActionUtil这个类即可。
     * 调用还是使用writeLog()方法即可;
     * 2.在调整的时候可以考虑使用该方法打印日志，花一点点时间你将解决前面应用场景里面的问题。
     * <p>
     * 关于日志路径：
     * <<<<<<< HEAD
     * 所有日志将被存放在/log/devlog/文件夹下面 按天按java类生成对应的日志。
     * eg：
     * /log/devlog/NodeBefore.java_2017-06-18.log
     * /log/devlog/NodeAfter.java_2017-06-18.log
     * SendHrmResourceInfo.java_2017-06-28.log
     * =======
     * 所有日志将被存放在/log/devlog/文件夹下面 按天按java类生成对应的日志。
     * eg：
     * /log/devlog/NodeBefore.java_2017-06-18.log
     * /log/devlog/NodeAfter.java_2017-06-18.log
     * >>>>>>> 84c78e050685929fbe1387a54a96dc120300b2a7
     * <p>
     * 如果快速定位日志：
     * eg：
     * m=execute;n=14;t=10:06:23.743===>>开始日志
     * <<<<<<< HEAD
     * m=execute;n=14;t=10:10:08.774===>>日志未打印，如需打开请从配置文件NoPrintLog中删除"com.customcode.action.ExportEg"
     * =======
     * m=execute;n=14;t=10:10:08.774===>>日志未打印，如需打开请从配置文件NoPrintLog中删除"com.customcode.action.Export"
     * >>>>>>> 84c78e050685929fbe1387a54a96dc120300b2a7
     * <p>
     * 如上日志提供每次调用的时候提供了以下信息：
     * m=方法名(method);n=行数(number);t=执行时间(time)==>>打印日志（log）
     * 例如第一行表示 执行了execute方法 在源码第14行打印，执行时间为 10:06:23.743
     *
     * @param logStr log对象
     */
    private static  void write(String logStr,String type,String className,String method,int number , String actionFileName ) {

        actionFileName = actionFileName.substring(0, actionFileName.indexOf("."));


        SimpleDateFormat CurrentDate = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat CurrentTime = new SimpleDateFormat("HH:mm:ss.SSS");

        SimpleDateFormat CurrentHour = new SimpleDateFormat("HH");
        Date date = new Date();
        String thisHour = CurrentHour.format(date);
        String thisDate = CurrentDate.format(date);
        String thisTime = CurrentTime.format(date);

        String log = type+":m=" + method + ";n=" + number + ";t=" + thisTime + "===>>";

        String logPath = GCONST.getRootPath() + "log"+File.separatorChar +"devlog" ;
        File dir = new File(logPath);
        //writeLog(logPath);
        if (!dir.exists()) {
            dir.mkdir();
            System.out.println("创建"+logPath);
        }

        logPath = GCONST.getRootPath() + "log"+File.separatorChar +"devlog" + File.separatorChar + thisDate;
        dir = new File(logPath);
        //writeLog(logPath);
        if (!dir.exists()) {
            dir.mkdir();
            System.out.println("创建"+logPath);
        }


        logPath = GCONST.getRootPath() +"log"+File.separatorChar +"devlog" + File.separatorChar + thisDate + File.separator + actionFileName;

        dir = new File(logPath);
        //writeLog(logPath);
        if (!dir.exists()) {
            dir.mkdir();
            System.out.println("创建"+logPath);
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
            // Log(fileName, log+ "日志未打印，如需打开请从配置文件NoPrintLog中删除\""+className+"\"");

        } else {
            writeLog(fileName, log + logStr);
        }


    }

    /**
     * @param fileName 写入的文件
     * @param log      写入的内容
     */
    private static void writeLog(File fileName, String log) {
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName, true), StandardCharsets.UTF_8));

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
