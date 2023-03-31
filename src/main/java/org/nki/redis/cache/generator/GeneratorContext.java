package org.nki.redis.cache.generator;

import java.util.HashMap;

/**
 * Author Neeschal Kissoon created on 17/12/2022
 */
public class GeneratorContext {

    private HashMap<String, String> parameters = new HashMap<>();

    /**
     * Value of the placeholder and the parameter that is required to
     * replace it.
     *
     * @param placeholder
     * @param parameter
     */
    public void put(String placeholder, String parameter) {
        if (placeholder.isEmpty() || parameter.isEmpty()) {
            throw new RuntimeException("Required parameter missing");
        }

        parameters.put("${" + placeholder + "}", parameter);
    }

    public HashMap<String, String> getParameters() {
        return this.parameters;
    }
}
