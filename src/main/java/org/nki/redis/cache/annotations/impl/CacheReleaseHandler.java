package org.nki.redis.cache.annotations.impl;


import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.nki.redis.cache.annotations.CacheRelease;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Set;

import static org.nki.redis.cache.utils.CacheHelper.getMethod;


/**
 * Author Neeschal Kissoon
 */

@Aspect
@Component
public class CacheReleaseHandler {

    private final RedisTemplate<String, Object> redisTemplate;
    private final Logger logger = LoggerFactory.getLogger(CacheReleaseHandler.class);

    @Value("${redis-cache.enable.logs:false}")
    private boolean isLoggingEnabled;

    public CacheReleaseHandler(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }


    @Before("@annotation(org.nki.redis.cache.annotations.CacheRelease)")
    public void releaseCache(JoinPoint joinPoint) throws NoSuchMethodException {
        Method method = getMethod(joinPoint);
        CacheRelease annotation = method.getAnnotation(CacheRelease.class);
        String groupName = annotation.group();

        if (Objects.nonNull(groupName)) {
            if (isLoggingEnabled) {
                logger.info("Releasing cache for method {}.", method.getName());
            }

            Set<String> redisKeys = redisTemplate.keys(groupName + "::*");
            if (!CollectionUtils.isEmpty(redisKeys)) {
                redisTemplate.delete(redisKeys);
            }
        }
    }
}
