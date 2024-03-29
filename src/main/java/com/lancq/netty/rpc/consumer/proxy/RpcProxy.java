package com.lancq.netty.rpc.consumer.proxy;

import com.lancq.netty.rpc.protocol.InvokerProtocol;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 *
 * @author lancq
 */
public class RpcProxy {

    /**
     * 生成代理对象
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T create(Class<?> clazz) {
        //clazz传进来本身就是Interface
        MethodProxy proxy = new MethodProxy(clazz);
        Class<?>[] interfaces = clazz.isInterface() ? new Class[]{clazz} : clazz.getInterfaces();
        T result = (T) Proxy.newProxyInstance(clazz.getClassLoader(), interfaces, proxy);
        return result;
    }


    private static class MethodProxy implements InvocationHandler {
        private Class<?> clazz;

        public MethodProxy(Class<?> clazz) {
            this.clazz = clazz;
        }
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (Object.class.equals(method.getDeclaringClass())) {
                //实体类
                return method.invoke(this, args);
            } else {
                //接口
                return rpcInvoke(proxy, method, args);
            }
        }

        /**
         * rpc调用
         *
         * @param proxy
         * @param method
         * @param args
         * @return
         */
        private Object rpcInvoke(Object proxy, Method method, Object[] args) {
            //传输协议封装
            InvokerProtocol msg = new InvokerProtocol();
            msg.setClassName(this.clazz.getName());
            msg.setMethodName(method.getName());
            msg.setValues(args);
            msg.setParameters(method.getParameterTypes());

            final RpcProxyHandler consumerHandler = new RpcProxyHandler();
            EventLoopGroup group = new NioEventLoopGroup();
            try {
                Bootstrap b = new Bootstrap();
                b.group(group);
                b.channel(NioSocketChannel.class);
                b.option(ChannelOption.TCP_NODELAY, true);
                b.handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        //自定义协议解码器
                        pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0,4));
                        //自定义协议编码器
                        pipeline.addLast("frameEncoder", new LengthFieldPrepender(4));
                        //对象参数类型编码器
                        pipeline.addLast("encoder", new ObjectEncoder());
                        //对象参数类型解码器
                        pipeline.addLast("decoder", new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(null)));
                        pipeline.addLast("handler", consumerHandler);
                    }
                });
                ChannelFuture f = b.connect("localhost", 8080).sync();
                System.out.println("连接到服务 ：localhost:" + 8080 );
                f.channel().writeAndFlush(msg).sync();
                f.channel().closeFuture().sync();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                group.shutdownGracefully();
            }
            return consumerHandler.getResponse();
        }
    }
}
