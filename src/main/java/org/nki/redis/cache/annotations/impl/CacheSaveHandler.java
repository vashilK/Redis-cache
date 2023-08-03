package org.nki.redis.cache.annotations.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.nki.redis.cache.exceptions.Exceptions;
import org.nki.redis.cache.exceptions.IoException;
import org.nki.redis.cache.exceptions.NoSuchMethodException;
import org.nki.redis.cache.exceptions.PointCutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

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
    private final Logger logger =
            LoggerFactory.getLogger(CacheSaveHandler.class);

    @Value("${redis-cache.enable.logs:false}")
    private boolean isLoggingEnabled;

    public CacheSaveHandler(ObjectMapper objectMapper, RedisTemplate<String, Object> redisTemplate) {
        this.objectMapper = objectMapper;
        this.redisTemplate = redisTemplate;
    }

    @Around(value = "(@annotation(org.nki.redis.cache.annotations.CacheSave))")
    public Object fetchCache(ProceedingJoinPoint joinPoint) {
        Method method =
                Exceptions.handle(() -> getMethod(joinPoint),
                        NoSuchMethodException::new);
        String pattern = Exceptions.handle(() -> getPattern(joinPoint, method),
                () -> new IoException(IoException.ERROR_JSON_DESERIALIZING));

        return Optional
                .ofNullable(redisTemplate.opsForValue().get(pattern))
                .map(Object::toString)
                .map(results -> {
                    if (isLoggingEnabled) {
                        logger.info("Invoking data for method {} from cache.",
                                method.getName());
                    }

                    Class<?> returnType = method.getReturnType();
                    return Exceptions.handle(
                            () -> objectMapper.readValue(results, returnType),
                            () -> new IoException(IoException.ERROR_JSON_DESERIALIZING));
                }).orElseGet(() -> {
                    try {
                        if (isLoggingEnabled) {
                            logger.info(
                                    "Data not present in cache for method {} invoking datasource.",
                                    method.getName());
                        }

                        joinPoint.proceed();
                        return null;
                    } catch (Throwable e) {
                        throw new PointCutException(PointCutException.ERROR_RESUMING, e);
                    }
                });
    }

    @AfterReturning(pointcut = "@annotation(org.nki.redis.cache.annotations.CacheSave)", returning = "result")
    public void persisResult(JoinPoint joinPoint, Object result) {
        Method method =
                Exceptions.handle(() -> getMethod(joinPoint),
                        NoSuchMethodException::new);
        String pattern = Exceptions.handle(() -> getPattern(joinPoint, method),
                () -> new IoException(IoException.ERROR_JSON_DESERIALIZING));

        if (isLoggingEnabled) {
            logger.info("Saving result for method {} in cache.",
                    method.getName());
        }

        redisTemplate.opsForValue().set(pattern,
                Exceptions.handle(() -> objectMapper.writeValueAsString(result),
                        () -> new IoException(IoException.ERROR_JSON_DESERIALIZING)));

    }
}
