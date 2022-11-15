package org.nki.redis.cache.model;

/**
 * Author Neeschal Kissoon created on 15/11/2022
 */
public class MethodAttribute {
    public final String attributeName;
    public final Object attributeValue;

    public MethodAttribute(String attributeName, Object attributeValue) {
        this.attributeName = attributeName;
        this.attributeValue = attributeValue;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public Object getAttributeValue() {
        return attributeValue;
    }
}
