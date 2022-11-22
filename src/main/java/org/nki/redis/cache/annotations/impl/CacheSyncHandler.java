package org.nki.redis.cache.annotations.impl;


import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.nki.redis.cache.annotations.CacheSave;
import org.nki.redis.cache.annotations.CacheSync;
import org.nki.redis.cache.model.MethodAttribute;
import org.nki.redis.cache.model.MethodInvocation;
import org.nki.redis.cache.model.MethodWrapper;
import org.nki.redis.cache.utils.CacheHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.nki.redis.cache.utils.CacheHelper.getMethod;
import static org.nki.redis.cache.utils.CacheHelper.getMethodsAnnotatedWith;
import static org.nki.redis.cache.utils.Transformer.cast;

/**
 * Author Neeschal Kissoon created on 04/11/2022
 */

@Aspect
public class CacheSyncHandler {

    private final RedisTemplate<String, Object> template;
    private final Logger logger = LoggerFactory.getLogger(CacheSyncHandler.class);

    public CacheSyncHandler(RedisTemplate<String, Object> template) {
        this.template = template;
    }

    @AfterReturning(pointcut = "@annotation(org.nki.redis.cache.annotations.CacheSync)")
    public void synchronize(JoinPoint joinPoint) throws NoSuchMethodException, IOException, ClassNotFoundException {
        Method method = getMethod(joinPoint);
        String groupName = method.getAnnotation(CacheSync.class).group();

        Set<String> redisKeys = template.keys(groupName + "::*");
        List<Class<?>> classes = CacheHelper.getAllClasses();

        List<Method> methods = classes
                .stream()
                .flatMap(clazz -> getMethodsAnnotatedWith(clazz, CacheSave.class).stream())
                .distinct()
                .collect(Collectors.toList());

        if (!CollectionUtils.isEmpty(redisKeys)) {
            Set<MethodWrapper> methodInvocationParams = getMethodInvocationParams(redisKeys);
            List<MethodInvocation> methodInvocations = getMethodInvocations(methods, methodInvocationParams);
            CompletableFuture<Object> completableFuture = methodFutureInvocations(redisKeys, methodInvocations);
            completableFuture.join();
        }
    }

    private MethodInvocation buildMethodInvocation(Method m0, MethodWrapper methodInvocationParam) {
        List<MethodAttribute> attributes = methodInvocationParam.getAttributes();
        List<Object> parameters = new ArrayList<>();

        if (!CollectionUtils.isEmpty(attributes)) {
            for (Class<?> param : m0.getParameterTypes()) {
                try {
                    Object obj = Class.forName(param.getName()).getDeclaredConstructor().newInstance();
                    Field[] fields = obj.getClass().getDeclaredFields();

                    for (Field field : fields) {
                        attributes
                                .stream()
                                .filter(data -> Objects.equals(field.getName(), data.getAttributeName()))
                                .map(MethodAttribute::getAttributeValue)
                                .filter(data -> !Objects.equals(data, "null"))
                                .findFirst()
                                .ifPresent(val -> {
                                    try {
                                        field.setAccessible(true);
                                        field.set(obj, cast(field.getType(), val));
                                    } catch (IllegalAccessException e) {
                                        throw new RuntimeException();
                                    }

                                });
                    }

                    parameters.add(obj);
                } catch (ClassNotFoundException | InvocationTargetException |
                         InstantiationException | IllegalAccessException |
                         NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return new MethodInvocation(m0, parameters);
    }

    private List<MethodInvocation> getMethodInvocations(List<Method> methods, Set<MethodWrapper> methodInvocationParams) {
        return methods.stream()
                .map(m0 ->
                        methodInvocationParams
                                .stream()
                                .filter(methodInvocationParam -> Objects.equals(methodInvocationParam.getMethodName(), m0.getName()))
                                .findFirst()
                                .map(methodInvocationParam -> buildMethodInvocation(m0, methodInvocationParam))
                                .orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private Set<MethodWrapper> getMethodInvocationParams(Set<String> redisKeys) {
        return redisKeys.stream()
                .map(key -> key.replace("[", "").replace("]", ""))
                .map(key -> key.split("\\("))
                .map(values -> {
                    if (values.length > 1) {
                        String[] arr = values[1].replace(")", "").split(",");
                        List<MethodAttribute> attributeValues = StreamSupport
                                .stream(Arrays.stream(arr).spliterator(), false)
                                .map(param -> param.split("="))
                                .map(params -> new MethodAttribute(params[0].trim(), params[1]))
                                .collect(Collectors.toList());

                        return new MethodWrapper(values[0].split("::")[1], attributeValues);
                    }

                    return new MethodWrapper(values[0].split("::")[1], Collections.emptyList());
                })
                .collect(Collectors.toSet());
    }

    private Object invokeMethod(MethodInvocation methodInvocation) {
        try {
            Class<?> clazz = methodInvocation.getMethod().getDeclaringClass();
            Object invocationServiceContext = clazz.getDeclaredConstructor().newInstance();
            Method m0 = invocationServiceContext.getClass().getDeclaredMethod(methodInvocation.getMethod().getName(), methodInvocation.getParameterTypes());
            if (!CollectionUtils.isEmpty(methodInvocation.getInvocationParams())) {
                return m0.invoke(invocationServiceContext, methodInvocation.getInvocationValues());
            } else {
                return m0.invoke(invocationServiceContext);
            }
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException |
                 InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    private CompletableFuture<Object> methodFutureInvocations(Set<String> redisKeys, List<MethodInvocation> methodInvocations) {
        CompletableFuture<Object> completableFuture = CompletableFuture.supplyAsync(Object::new);

        completableFuture
                .thenApply(i -> {
                    if (!CollectionUtils.isEmpty(redisKeys)) {
                        return template.delete(redisKeys);
                    }

                    return 0L;
                }).whenComplete((val, detail) -> {
                    if (val == 0L) {
                        logger.error("Delete could not be performed: {}", detail.getMessage());
                    }
                });

        methodInvocations
                .forEach(methodInvocation -> {
                    completableFuture
                            .thenApply(i -> invokeMethod(methodInvocation))
                            .whenComplete((val, detail) -> {
                                if (detail != null) {
                                    logger.error("failure: {}", detail.getMessage());
                                }
                            });
                });

        return completableFuture;
    }
}
