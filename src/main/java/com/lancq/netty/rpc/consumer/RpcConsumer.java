package com.lancq.netty.rpc.consumer;

import com.lancq.netty.rpc.api.RpcHelloService;
import com.lancq.netty.rpc.api.RpcService;
import com.lancq.netty.rpc.consumer.proxy.RpcProxy;

/**
 * @author lancq
 */
public class RpcConsumer {
    public static void main(String[] args) {
        RpcHelloService rpcHello = RpcProxy.create(RpcHelloService.class);
        System.out.println(rpcHello.hello("ABCD"));

        RpcService service = RpcProxy.create(RpcService.class);
        System.out.println("8 + 2" + service.add(8,2));
        System.out.println("8 - 2" + service.sub(8,2));
        System.out.println("8 * 2" + service.mult(8,2));
        System.out.println("8 / 2" + service.div(8,2));


    }
}
