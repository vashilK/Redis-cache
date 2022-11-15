package org.nki.redis.cache.model;

/**
 * Author Neeschal Kissoon created on 15/11/2022
 */
public class ImmutablePair {
    private final String attributeName;
    private final Object value;

    public ImmutablePair(String attributeName, Object value) {
        this.attributeName = attributeName;
        this.value = value;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public Object getValue() {
        return value;
    }
}
