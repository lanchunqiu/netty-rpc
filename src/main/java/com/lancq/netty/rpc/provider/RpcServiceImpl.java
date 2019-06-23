package com.lancq.netty.rpc.provider;

import com.lancq.netty.rpc.api.RpcService;

/**
 * @author lancq
 */
public class RpcServiceImpl implements RpcService {
    @Override
    public int add(int a, int b) {
        return a + b;
    }

    @Override
    public int sub(int a, int b) {
        return a - b;
    }

    @Override
    public int mult(int a, int b) {
        return a * b;
    }

    @Override
    public int div(int a, int b) {
        return a / b;
    }
}
