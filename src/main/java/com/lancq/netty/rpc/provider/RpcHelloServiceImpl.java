package com.lancq.netty.rpc.provider;

import com.lancq.netty.rpc.api.RpcHelloService;

/**
 * @author lancq
 */
public class RpcHelloServiceImpl implements RpcHelloService {
    @Override
    public String hello(String name) {
        return "Hello " + name;
    }
}
