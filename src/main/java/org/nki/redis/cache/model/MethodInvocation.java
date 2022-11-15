package org.nki.redis.cache.model;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.nki.redis.cache.utils.Transformer.cast;

/**
 * Author Neeschal Kissoon created on 15/11/2022
 */
public class MethodInvocation {
    public final Method method;
    public final List<Object> invocationParams;

    public MethodInvocation(Method method, List<Object> invocationParams) {
        this.method = method;
        this.invocationParams = invocationParams;
    }

    public Method getMethod() {
        return method;
    }

    public List<Object> getInvocationParams() {
        return invocationParams;
    }

    public Class<?>[] getParameterTypes() {
        Class<?>[] arr = new Class<?>[invocationParams.size()];

        for (int i = 0; i < arr.length; i++) {
            arr[i] = invocationParams.get(i).getClass();
        }

        return arr;
    }

    public Class<?>[] getObjectParameterTypes() {
        List<Class<?>> x0 = invocationParams
                .stream()
                .flatMap(x -> Arrays.stream(x.getClass().getDeclaredFields()).distinct())
                .map(Field::getType)
                .collect(Collectors.toList());

        Class<?>[] arr = new Class<?>[x0.size()];

        for (int i = 0; i < x0.size(); i++) {
            arr[i] = x0.get(i);
        }

        return arr;
    }

    public Object[] getInvocationValues() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Object[] invocationValues = new Object[invocationParams.size()];

        for (int i = 0; i < invocationValues.length; i++) {
            Object val = invocationParams.get(i);
            List<ImmutablePair> attributes = Arrays
                    .stream(val.getClass().getDeclaredFields())
                    .map(field -> {
                        field.setAccessible(true);
                        try {
                            Object value = field.get(val);
                            return new ImmutablePair(field.getName(), value);
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    }).collect(Collectors.toList());

            invocationValues[i] = invocationParams.get(i).getClass().getDeclaredConstructor(getObjectParameterTypes()).newInstance(getParameterValues(attributes));
        }

        return invocationValues;
    }

    private Object[] getParameterValues(List<ImmutablePair> attributeValues) {
        Object[] invocationValues = new Object[attributeValues.size()];

        for (int i = 0; i < attributeValues.size(); i++) {
            invocationValues[i] = cast(Optional.ofNullable(attributeValues.get(i).getValue()).map(Object::getClass).orElse(null), attributeValues.get(i).getValue());
        }

        return invocationValues;
    }
}
