package org.nki.redis.cache.model;

import java.util.List;

/**
 * Author Neeschal Kissoon created on 15/11/2022
 */
public class MethodWrapper {
    public final String methodName;
    public final List<MethodAttribute> attributes;

    public MethodWrapper(String methodName, List<MethodAttribute> attributes) {
        this.methodName = methodName;
        this.attributes = attributes;
    }

    public String getMethodName() {
        return methodName;
    }

    public List<MethodAttribute> getAttributes() {
        return attributes;
    }
}
