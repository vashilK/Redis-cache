package org.nki.redis.cache.model;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Author Neeschal Kissoon created on 15/11/2022
 */
public class MethodInvocation {
    public final Method method;
    public final List<Object> invocationParams;

    public MethodInvocation(Method method, List<Object> invocationParams) {
        this.method = method;
        this.invocationParams = invocationParams;
    }

    public Method getMethod() {
        return method;
    }

    public List<Object> getInvocationParams() {
        return invocationParams;
    }
}
