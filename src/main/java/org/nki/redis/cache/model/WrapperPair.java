package org.nki.redis.cache.model;

import java.util.List;

/**
 * Author Neeschal Kissoon created on 04/12/2022
 */


public class WrapperPair {
    private String methodName;
    private List<Object> params;

    public static WrapperPair of() {
        return new WrapperPair();
    }

    public static WrapperPair of(String methodName, List<Object> params) {
        return new WrapperPair(methodName, params);
    }

    private WrapperPair() {
    }

    private WrapperPair(String methodName, List<Object> params) {
        this.methodName = methodName;
        this.params = params;
    }

    public String getMethodName() {
        return methodName;
    }

    public List<Object> getParams() {
        return params;
    }
}
