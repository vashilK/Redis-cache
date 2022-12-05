package org.nki.redis.cache.annotations.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Optional;

import static org.nki.redis.cache.utils.CacheHelper.getMethod;
import static org.nki.redis.cache.utils.CacheHelper.getPattern;

/**
 * Author Neeschal Kissoon created on 04/11/2022
 */

@Aspect
@Component
public class CacheSaveHandler {

    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    public CacheSaveHandler(ObjectMapper objectMapper, RedisTemplate<String, Object> redisTemplate) {
        this.objectMapper = objectMapper;
        this.redisTemplate = redisTemplate;
    }

    @Around(value = "@annotation(org.nki.redis.cache.annotations.CacheSave)")
    public Object fetchCache(ProceedingJoinPoint joinPoint) throws NoSuchMethodException, JsonProcessingException {
        Method method = getMethod(joinPoint);
        String pattern = getPattern(joinPoint, method);

        return Optional
                .ofNullable(redisTemplate.opsForValue().get(pattern))
                .map(Object::toString)
                .map(results -> {
                    try {
                        Class<?> returnType = method.getReturnType();
                        return objectMapper.readValue(results, returnType);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }).orElseGet(() -> {
                    try {
                        joinPoint.proceed();
                        return null;
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    @AfterReturning(pointcut = "@annotation(org.nki.redis.cache.annotations.CacheSave)", returning = "result")
    public void persisResult(JoinPoint joinPoint, Object result) throws NoSuchMethodException, JsonProcessingException {
        Method method = getMethod(joinPoint);
        String pattern = getPattern(joinPoint, method);
        redisTemplate.opsForValue().set(pattern, objectMapper.writeValueAsString(result));
    }
}
