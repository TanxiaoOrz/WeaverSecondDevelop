package hotswap;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import weaver.general.GCONST;

import java.io.*;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

/**
 * @Author: 张骏山
 * @Date: 2024/7/3 13:44
 * @PackageName: hotswap
 * @ClassName: AgentProxy
 * @Description: 虚拟机单例代理类
 * @Version: 1.0
 **/
public class AgentProxy {

    private Instrumentation instrumentation = null;

    public synchronized void hotSwap(String classPath, String filePath) {
        try {
            byte[] bytes = getBytes(filePath);
            Class<?> toSwap = getClazz(classPath, instrumentation);
            ClassDefinition classDefinition = new ClassDefinition(toSwap, bytes);
            instrumentation.redefineClasses(classDefinition);
            Console.log("hotswap success");
        } catch (Exception e) {
            Console.log(e.getMessage());
        }
    }

    private byte[] getBytes(String filePath){

        byte[] buffer = null;
        try {
            File file = new File(filePath);
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream(1000);
            byte[] b = new byte[1000];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            fis.close();
            bos.close();
            buffer = bos.toByteArray();
        } catch (IOException e) {
            StringWriter error = new StringWriter();
            e.printStackTrace(new PrintWriter(error));
        }
        return buffer;
    }

    public Class<?> getClazz(String className, Instrumentation instrumentation) {
        return Arrays.stream(
                        instrumentation.getAllLoadedClasses()
                ).filter(aClass -> aClass.getName().equals(className))
                .findFirst().orElse(null);
    }

    private static AgentProxy agentProxy = null;
    public static final String root = GCONST.getRootPath();

    private AgentProxy() throws Exception {
        initial();
    }

    private void initial() {
        if (agentProxy == null) {
            agentProxy = this;
        }

        try {
            String currentPID = HotSwapUtils.locateCurrentPID();
            VirtualMachine virtualMachine = VirtualMachine.attach(currentPID);
            String virtualPath = root + "classbean" + File.separatorChar + "hotswap" + File.separatorChar + "Agent.jar";
            Console.log(virtualPath);
            virtualMachine.loadAgent(virtualPath);
        }catch (Exception e) {
            Console.log(e.getMessage());
            e.printStackTrace();
        }
    }

    public static synchronized AgentProxy getInstance() {
        if (agentProxy == null) {
            try {
                new AgentProxy();
            } catch (Exception e) {
                Console.log(e.getMessage());
                e.printStackTrace();
            }
        }
        return agentProxy;
    }

    public static void setInstrumentation(Instrumentation instrumentation) {
        agentProxy.instrumentation = instrumentation;
    }

}
