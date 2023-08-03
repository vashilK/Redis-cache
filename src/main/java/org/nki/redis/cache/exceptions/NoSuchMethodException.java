package org.nki.redis.cache.exceptions;

/**
 * Author Neeschal Kissoon created on 03/08/2023
 */
public class NoSuchMethodException extends RuntimeException {

    public NoSuchMethodException() {
        super();
    }

    public NoSuchMethodException(String message) {
        super(message);
    }

    public NoSuchMethodException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoSuchMethodException(Throwable cause) {
        super(cause);
    }
}
