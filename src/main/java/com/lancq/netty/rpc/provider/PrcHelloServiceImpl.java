package com.lancq.netty.rpc.provider;

import com.lancq.netty.rpc.api.RpcHelloService;

/**
 * @author lancq
 */
public class PrcHelloServiceImpl implements RpcHelloService {
    @Override
    public String hello(String name) {
        return "Hello " + name;
    }
}
