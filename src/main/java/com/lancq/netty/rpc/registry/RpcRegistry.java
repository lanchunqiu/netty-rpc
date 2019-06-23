package com.lancq.netty.rpc.registry;

import com.lancq.netty.rpc.protocol.InvokerProtocol;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

/**
 * @author lancq
 */
public class RpcRegistry {
    private int port;

    public RpcRegistry(int port) {
        this.port = port;
    }

    public void start(){
        //主线程池，selector
        EventLoopGroup bossGroup = new NioEventLoopGroup();

        //子线程池，具体对象客户端的处理逻辑
        EventLoopGroup workGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap server = new ServerBootstrap();
            server.group(bossGroup, workGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            //在Netty中，把所有的业务逻辑处理全部归总到了一个队里中
                            //这个队列中包含了各种各样的处理逻辑，对这些处理逻辑在Netty中有一个封装
                            //封装成了一个对象，无锁化串行队列pipeline
                            ChannelPipeline pipeline = ch.pipeline();

                            //对于自定义的内容进行编解码
                            pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
                            //自定义编码
                            pipeline.addLast(new LengthFieldPrepender(4));
                            //实参处理
                            pipeline.addLast("encoder", new ObjectEncoder());
                            pipeline.addLast("decoder", new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(null)));

                            //前面的编解码，就是对数据的解析
                            //最后一步，执行属于自己的逻辑
                            //1.注册，给每一个对象起一个名字
                            //2.服务位置做一个登记
                            pipeline.addLast(new RegistryHandler());
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            //正式启动服务，相当于一个死循环开始轮训
            ChannelFuture sync = server.bind(this.port).sync();
            System.out.println("Rpc Registry start listen at " + port);
        } catch (Exception e) {
            e.printStackTrace();
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        new RpcRegistry(8080).start();
    }
}
