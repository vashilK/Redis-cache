package org.nki.redis.cache.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Any class model annotated with this, will allow
 * deserialization of method params for revocation when
 * Cache Synchronization methods are invoked.
 * <p>
 * Author Neeschal Kissoon created on 28/11/2022
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RedisCacheSerializable {
}
