package org.nki.redis.cache.utils;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Author Neeschal Kissoon created on 04/11/2022
 */
public class Transformer {

    public static Object cast(Class clazz, Object value) {
        return Optional
                .ofNullable(clazz)
                .map(data -> {
                    String val = String.valueOf(value);

                    if (data == String.class) {
                        return val;
                    } else if (data == Double.class) {
                        return Double.valueOf(val);
                    } else if (data == Long.class) {
                        return Long.valueOf(val);
                    } else if (data == Integer.class) {
                        return Integer.valueOf(val);
                    } else if (data == Boolean.class) {
                        return Boolean.getBoolean(val);
                    } else if (data == BigDecimal.class) {
                        return BigDecimal.valueOf(Long.parseLong(val));
                    }

                    return val;
                })
                .orElse(null);
    }
}
