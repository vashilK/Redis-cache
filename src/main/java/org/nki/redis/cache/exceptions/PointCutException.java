package org.nki.redis.cache.exceptions;

/**
 * Author Neeschal Kissoon created on 03/08/2023
 */
public class PointCutException extends RuntimeException {

    public static final String ERROR_RESUMING= "Error while aop pointcut.";

    public PointCutException() {
        super();
    }

    public PointCutException(String message) {
        super(message);
    }

    public PointCutException(String message, Throwable cause) {
        super(message, cause);
    }

    public PointCutException(Throwable cause) {
        super(cause);
    }
}
