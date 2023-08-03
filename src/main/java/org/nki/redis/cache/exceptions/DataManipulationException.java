package org.nki.redis.cache.exceptions;

/**
 * Author Neeschal Kissoon created on 03/08/2023
 */
public class DataManipulationException extends RuntimeException {

    public DataManipulationException() {
        super();
    }

    public DataManipulationException(String message) {
        super(message);
    }

    public DataManipulationException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataManipulationException(Throwable cause) {
        super(cause);
    }
}
