package org.nki.redis.cache.utils;


import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.nki.redis.cache.annotations.CacheSave;
import org.nki.redis.cache.annotations.CacheSync;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Author Neeschal Kissoon created on 04/11/2022
 */
public class CacheHelper {

    public static Method getMethod(JoinPoint joinPoint) throws NoSuchMethodException {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        return joinPoint.getTarget().getClass().getMethod(signature.getMethod().getName(), signature.getMethod().getParameterTypes());
    }

    public static Method getMethod(ProceedingJoinPoint joinPoint) throws NoSuchMethodException {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        return joinPoint.getTarget().getClass().getMethod(signature.getMethod().getName(), signature.getMethod().getParameterTypes());
    }

    public static String getPattern(JoinPoint joinPoint, Method method) {
        List<String> patternBuilder = new ArrayList<>();
        patternBuilder.add(method.getAnnotation(CacheSave.class).group());
        patternBuilder.add(method.getName());
        patternBuilder.add(Arrays.stream(joinPoint.getArgs()).filter(Objects::nonNull).map(Object::toString).collect(Collectors.joining(",")));
        return String.join("::", patternBuilder);
    }

    public static List<Method> getMethodsAnnotatedWith(final Class<?> type, final Class<? extends Annotation> annotation) {
        final List<Method> methods = new ArrayList<Method>();
        Class<?> clazz = type;

        while (clazz != Object.class) {
            for (final Method method : clazz.getDeclaredMethods()) {
                if (method.isAnnotationPresent(annotation)) {
                    methods.add(method);
                }
            }
            clazz = clazz.getSuperclass();
        }

        return methods;
    }
}
