package org.nki.redis.cache.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Author Neeschal Kissoon created on 03/11/2022
 */

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CacheSync {

    /**
     * Name of group which will encapsulate all the related methods
     * which point to the same database resource. i.e: 
     * Entity name would be a good fit
     * for a group name.
     */
    String group() default "";
}
