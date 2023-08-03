package org.nki.redis.cache.utils;


import org.nki.redis.cache.exceptions.DataManipulationException;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * Author Neeschal Kissoon created on 04/11/2022
 */
public class Transformer {

    public static final List<Class<?>> rawTypes =
            Arrays.asList(Integer.class, Double.class, Float.class,
                    Character.class, Long.class, BigDecimal.class,
                    Boolean.class, Byte.class, Short.class,
                    String.class);

    public static <T> Object cast(Class<T> rawType, Object val) {
        if (rawType == Integer.class) {
            return Integer.valueOf((String) val);
        }
        if (rawType == String.class) {
            return String.valueOf(val);
        }
        if (rawType == Byte.class) {
            return Byte.valueOf((String) val);
        }
        if (rawType == Short.class) {
            return Short.valueOf((String) val);
        }
        if (rawType == Long.class) {
            return Long.valueOf((String) val);
        }
        if (rawType == Double.class) {
            return Double.valueOf((String) val);
        }
        if (rawType == Float.class) {
            return Float.valueOf((String) val);
        }
        if (rawType == Boolean.class) {
            return Boolean.valueOf((String) val);
        }
        if (rawType == Character.class) {
            return (Character) val;
        }
        if (rawType == BigDecimal.class) {
            return BigDecimal.valueOf(Long.parseLong((String) val));
        }

        throw new DataManipulationException("Error: data could not be converted to wrapper class.");
    }
}
