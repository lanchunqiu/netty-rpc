package com.lancq.netty.rpc.registry;

import com.lancq.netty.rpc.protocol.InvokerProtocol;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lancq
 */
public class RegistryHandler extends ChannelInboundHandlerAdapter {

    private List<String> classNames = new ArrayList();

    private Map<String, Object> registryMap = new HashMap();

    //1.根据一个包名将所有符合添加的class都扫描出来


    public RegistryHandler() {
        //完成递归扫描
        scannerClass("com.lancq.netty.rpc.provider");
        doRegister();
    }

    /**
     * 完成注册
     */
    private void doRegister() {
        if (classNames.isEmpty()) {return ;}

        for (String className : classNames) {
            try {
                Class<?> clazz = Class.forName(className);
                Class<?> i = clazz.getInterfaces()[0];
                String serviceName = i.getName();
                registryMap.put(className, serviceName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 扫描指定包下的所有类
     * @param packageName
     */
    private void scannerClass(String packageName) {
        URL url = this.getClass().getClassLoader().getResource(packageName.replace("\\.", "/"));
        File classPath = new File(url.getFile());
        for (File file : classPath.listFiles()) {
            if (file.isDirectory()) {
                scannerClass(packageName + "." +file.getName());
            } else {
                classNames.add(packageName + "." + file.getName().replace(".class", ""));
            }
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Object result = new Object();
        InvokerProtocol request = (InvokerProtocol) msg;

        //当客户端建立连接时，需要懂自定义协议获取信息，拿到具体的服务和实参
        if (registryMap.containsKey(request.getClassName())) {
            Object service = registryMap.get(request.getClassName());
            Method method = service.getClass().getMethod(request.getMethodName(),request.getParameters());
            result = method.invoke(service, request.getValues());
        }
        ctx.write(result);
        ctx.flush();
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
