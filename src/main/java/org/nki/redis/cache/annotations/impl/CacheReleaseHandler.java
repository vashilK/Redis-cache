package org.nki.redis.cache.annotations.impl;


import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.nki.redis.cache.annotations.CacheRelease;
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

    public CacheReleaseHandler(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }


    @Before(value = "@annotation(org.nki.redis.cache.annotations.CacheRelease)")
    public void releaseCache(JoinPoint joinPoint) throws NoSuchMethodException {
        Method method = getMethod(joinPoint);
        CacheRelease annotation = method.getAnnotation(CacheRelease.class);
        String groupName = annotation.group();

        if (Objects.nonNull(groupName)) {
            Set<String> redisKeys = redisTemplate.keys(groupName + "::*");

            if (!CollectionUtils.isEmpty(redisKeys)) {
                redisTemplate.delete(redisKeys);
            }
        }
    }
}
