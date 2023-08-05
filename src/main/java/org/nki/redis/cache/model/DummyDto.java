package org.nki.redis.cache.model;

import org.nki.redis.cache.annotations.RedisCacheSerializable;

/**
 * Author Neeschal Kissoon created on 06/08/2023
 * <p>
 * This class was created for Test purposes only do not use
 * during coding.
 */
@RedisCacheSerializable
public class DummyDto {
    private String name;
    private Integer age;

    public DummyDto() {
    }

    public DummyDto(String name, Integer age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }
}
