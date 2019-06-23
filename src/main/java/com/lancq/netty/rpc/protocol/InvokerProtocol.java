package com.lancq.netty.rpc.protocol;

import lombok.Data;

import java.io.Serializable;

/**
 * @author lancq
 */
@Data
public class InvokerProtocol implements Serializable {

    /**
     * 类名
     */
    private String className;

    /**
     * 方法名
     */
    private String methodName;

    /**
     * 形参列表
     */
    private Class<?>[] parameters;

    /**
     * 实参列表
     */
    private Object[] values;
}
