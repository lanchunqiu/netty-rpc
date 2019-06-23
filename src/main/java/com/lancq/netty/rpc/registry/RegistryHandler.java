package com.lancq.netty.rpc.registry;

import com.lancq.netty.rpc.protocol.InvokerProtocol;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lancq
 */
@Slf4j
public class RegistryHandler extends ChannelInboundHandlerAdapter {

    /**
     * 保存所有相关的服务类名
     */
    private List<String> classNames = new ArrayList();

    /**
     * 用于保存所有可用的服务
     */
    private ConcurrentHashMap<String, Object> registryMap = new ConcurrentHashMap();


    public RegistryHandler() {
        //完成递归扫描
        scannerClass("com.lancq.netty.rpc.provider");
        doRegister();
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

    /**
     * 扫描指定包下的所有类
     * @param packageName
     */
    private void scannerClass(String packageName) {
        System.out.println("开始扫描包：" + packageName);
        URL url = this.getClass().getClassLoader().getResource(packageName.replaceAll("\\.", "/"));
        File classPath = new File(url.getFile());
        for (File file : classPath.listFiles()) {
            if (file.isDirectory()) {
                scannerClass(packageName + "." +file.getName());
            } else {
                System.out.println("扫描到：" + file.getName());
                classNames.add(packageName + "." + file.getName().replace(".class", ""));
            }
        }
    }

    /**
     * 完成注册
     */
    private void doRegister() {
        System.out.println("开始注册");
        if (classNames.isEmpty()) {return ;}
        for (String className : classNames) {
            try {
                Class<?> clazz = Class.forName(className);
                Class<?> i = clazz.getInterfaces()[0];
                registryMap.put(i.getName(), clazz.newInstance());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
