package org.nki.redis.cache.annotations.impl;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.nki.redis.cache.annotations.CacheSave;
import org.nki.redis.cache.annotations.CacheSync;
import org.nki.redis.cache.model.MethodInvocation;
import org.nki.redis.cache.model.WrapperPair;
import org.nki.redis.cache.utils.CacheHelper;
import org.nki.redis.cache.utils.Transformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static org.nki.redis.cache.utils.CacheHelper.getMethod;
import static org.nki.redis.cache.utils.CacheHelper.getMethodsAnnotatedWith;

/**
 * Author Neeschal Kissoon created on 04/11/2022
 */

@Aspect
public class CacheSyncHandler implements ApplicationContextAware {

    private final RedisTemplate<String, Object> template;
    private final ObjectMapper objectMapper;
    private static ApplicationContext applicationContext;
    private final Logger logger = LoggerFactory.getLogger(CacheSyncHandler.class);

    public CacheSyncHandler(RedisTemplate<String, Object> template, ObjectMapper objectMapper) {
        this.template = template;
        this.objectMapper = objectMapper;
    }

    @AfterReturning(pointcut = "@annotation(org.nki.redis.cache.annotations.CacheSync)")
    public void synchronize(JoinPoint joinPoint) throws NoSuchMethodException, IOException, ClassNotFoundException {
        logger.warn("CacheSyncHandler invoked...");
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
            Map<String, List<WrapperPair>> params = initParams(redisKeys);
            List<MethodInvocation> methodInvocations = getMethodInvocations(methods, params);
            methodFutureInvocations(redisKeys, methodInvocations);
        }
    }

    private MethodInvocation buildMethodInvocation(Method m0, List<Object> parameters) {
        return new MethodInvocation(m0, parameters);
    }

    private Object buildPojo(String arg) {
        if (arg.contains("List")) {
            return buildObjects(arg, "List");
        } else if (arg.contains("Set")) {
            return buildObjects(arg, "Set");
        } else {
            return buildRawType(arg);
        }
    }

    private Object buildObjects(String arg, String dataStructure) {
        try {
            String[] arguments = arg.split("=");
            String name = arguments[0].replace(dataStructure + "<", "").replace(">", "");
            List<Class<?>> classes = CacheHelper.getAllClasses();
            String clazzName = classes.stream().filter(clazz -> clazz.getSimpleName().equals(name)).findFirst().map(Class::getCanonicalName).orElseThrow();
            Class<?> clazz = Class.forName(clazzName + dataStructure + "TypeReference");

            return objectMapper.readValue(arguments[1], (TypeReference<? super Object>) clazz.getMethod("getType").invoke(Class.forName(clazzName + dataStructure + "TypeReference")));

        } catch (ClassNotFoundException | IOException | InvocationTargetException | IllegalAccessException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private Object buildRawType(String arg) {
        Optional<?> optObj = Transformer.rawTypes
                .stream()
                .filter(rawType -> arg.contains(rawType.getSimpleName()))
                .findFirst()
                .map(rawType -> {
                    String[] data = arg.split("=");
                    try {
                        return objectMapper.readValue(data[1], rawType);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                });

        if (optObj.isPresent()) {
            return optObj.get();
        } else {
            String pojoName = arg.split("=")[0];
            try {
                List<Class<?>> classes = CacheHelper.getAllClasses();
                String clazzName = classes.stream().filter(clazz -> clazz.getSimpleName().equals(pojoName)).findFirst().map(Class::getCanonicalName).orElseThrow();
                Class<?> type = Class.forName(clazzName);
                return objectMapper.readValue(arg.split("=")[1], type);
            } catch (ClassNotFoundException | IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private List<MethodInvocation> getMethodInvocations(List<Method> methods, Map<String, List<WrapperPair>> methodParams) {
        return methods.stream()
                .map(m0 ->
                        methodParams
                                .entrySet()
                                .stream()
                                .filter(methodParam -> Objects.equals(methodParam.getKey(), m0.getName()))
                                .findFirst()
                                .map(methodParam -> buildMethodInvocation(m0, methodParam.getValue().stream().flatMap(item -> item.getParams().stream()).collect(Collectors.toList())))
                                .orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private Map<String, List<WrapperPair>> initParams(Set<String> keys) {
        return keys.stream()
                .map(value -> {
                    String[] args = value.split("::");
                    List<String> params = args.length > 2 ? Arrays.stream(args[2].split("\\\\§")).collect(Collectors.toList()) : Collections.emptyList();
                    List<Object> parameters = params.stream().map(this::buildPojo).collect(Collectors.toList());

                    return WrapperPair.of(args[1], parameters);
                })
                .collect(Collectors.groupingBy(WrapperPair::getMethodName));
    }

    private Object invokeMethod(MethodInvocation methodInvocation) {
        try {
            Class<?> clazz = methodInvocation.getMethod().getDeclaringClass();
            Object invocationServiceContext = applicationContext.getBean(clazz);
            Method m0 = invocationServiceContext.getClass().getDeclaredMethod(methodInvocation.getMethod().getName(), methodInvocation.getMethod().getParameterTypes());
            if (!CollectionUtils.isEmpty(methodInvocation.getInvocationParams())) {
                return m0.invoke(invocationServiceContext, methodInvocation.getInvocationParams().toArray());
            } else {
                return m0.invoke(invocationServiceContext);
            }
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private void methodFutureInvocations(Set<String> redisKeys, List<MethodInvocation> methodInvocations) {
        CompletableFuture
                .supplyAsync(() -> {
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
                    CompletableFuture
                            .supplyAsync(() -> invokeMethod(methodInvocation))
                            .whenComplete((val, detail) -> {
                                if (detail != null) {
                                    logger.error("failure: {}", detail.getMessage());
                                }
                            });
                });
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        CacheSyncHandler.applicationContext = applicationContext;
    }
}
