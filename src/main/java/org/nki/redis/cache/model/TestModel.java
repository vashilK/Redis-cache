package org.nki.redis.cache.model;

import org.nki.redis.cache.annotations.RedisCacheSerializable;

/**
 * Author Neeschal Kissoon created on 04/12/2022
 */

@RedisCacheSerializable
public class TestModel {
    private String name;
    private Integer age;

    public TestModel(String name, Integer age) {
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
