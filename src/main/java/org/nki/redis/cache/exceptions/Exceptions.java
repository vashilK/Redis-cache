package org.nki.redis.cache.exceptions;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

/**
 * Author Neeschal Kissoon created on 03/08/2023
 */
public class Exceptions {

    public static <T, E extends RuntimeException> Object handle(Callable<T> callable, Supplier<E> exception) {
        try {
            return callable.call();
        } catch (Exception e) {
            throw exception.get();
        }
    }

    public static <T,E extends RuntimeException> T handleException(Callable<T> callable, Supplier<E> exception) {
        try {
            return callable.call();
        } catch (Throwable ex) {
            throw exception.get();
        }
    }

}
