package hotswap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import weaver.conn.RecordSet;
import weaver.formmode.customjavacode.AbstractModeExpandJavaCodeNew;
import weaver.general.GCONST;
import weaver.general.Util;
import weaver.soa.workflow.request.MainTableInfo;
import weaver.soa.workflow.request.Property;
import weaver.soa.workflow.request.RequestInfo;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @Author: 张骏山
 * @Date: 2024/4/15 13:34
 * @PackageName: hotswap
 * @ClassName: HotSwap
 * @Description: TODO
 * @Version: 1.0
 **/
public class HotSwap extends AbstractModeExpandJavaCodeNew {

    public static final String root = GCONST.getRootPath();

    private Map<String, String> errorMap(String errmsg) {
        HashMap<String, String> result = new HashMap<>();
        result.put("errmsg", errmsg);
        result.put("flag", "false");
        return result;
    }


    public void doSwap(String classPath, boolean isVersion, int id, boolean test) throws NoSuchMethodException, ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException {
        if (isVersion)
            printVersion(classPath);
        String filePath = root + "classbean" + File.separatorChar + classPath.replace('.', File.separatorChar) + ".class";
        Console.log("filePath = " + filePath);
        Console.log("classPath = " + classPath);
        Console.log("isVersion = " + isVersion);

        AgentProxy.getInstance().hotSwap(classPath,filePath);

        if (isVersion) {
            String version = printVersion(classPath);
            String now =  LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            String sql = "update uf_hotswapList set currentVersion = '" + version + "', lastUpdateTime = '" + now +"' where id = " + id;
            Console.log(sql);
            new RecordSet().execute(sql);
        } else {
            String now =  LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            String sql = "update uf_hotswapList set lastUpdateTime = '" + now +"' where id = " + id;
            Console.log(sql);
            new RecordSet().execute(sql);
        }
    }


//    public void doSwap(String classPath, boolean isVersion) throws MalformedURLException {
//        String filePath =root + classPath.replace('.',File.separatorChar);
//        Console.log("hotswap start class: " + classPath +" file: "+ filePath);
//        URLClassLoader classLoader = new URLClassLoader(new URL[]{new URL(filePath)});
//
//    }


    private String locateCurrentPID() {
        return ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
    }

    private String printVersion(String classPath) throws NoSuchMethodException, ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Class<?> clazz = Class.forName(classPath);
        VersionControl versionControl = (VersionControl) clazz.getConstructor().newInstance();
        String version = versionControl.getVersion();
        Console.log(classPath + "current version : " + version);
        return version;
    }

    @Override
    public Map<String, String> doModeExpand(Map<String, Object> map) {
        RequestInfo requestInfo = ((RequestInfo) map.get("RequestInfo"));
        MainTableInfo mainTableInfo = requestInfo.getMainTableInfo();
        int id = Util.getIntValue(requestInfo.getRequestid());

        Optional<Property> classPathP = Arrays.stream(mainTableInfo.getProperty()).filter((property) -> property.getName().equals("classPath")).findFirst();
        if (!classPathP.isPresent())
            return errorMap("缺少类路径");
        String classPath = Util.null2String(classPathP.get().getValue());

        Optional<Property> isVersionP = Arrays.stream(mainTableInfo.getProperty()).filter((property) -> property.getName().equals("isVersion")).findFirst();
        if (!isVersionP.isPresent())
            return errorMap("是否版本控制");
        boolean isVersion = Util.getIntValue(isVersionP.get().getValue()) == 1;

        try {
            doSwap(classPath, isVersion, id, false);
        } catch (Exception e) {
            return errorMap(e);
        }

        return null;
    }

    private Map<String, String> errorMap(Exception e) {
        try {
            Console.log(new ObjectMapper().writeValueAsString(e));
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex);
        }
        return errorMap("执行错误,请联系管理员");
    }

}