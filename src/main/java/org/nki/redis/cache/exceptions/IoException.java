package org.nki.redis.cache.exceptions;

/**
 * Author Neeschal Kissoon created on 03/08/2023
 */
public class IoException extends RuntimeException {
    
    public static final String ERROR_JSON_DESERIALIZING = "Error while deserializing object from cache.";

    public IoException() {
        super();
    }

    public IoException(String message) {
        super(message);
    }

    public IoException(String message, Throwable cause) {
        super(message, cause);
    }

    public IoException(Throwable cause) {
        super(cause);
    }
}
