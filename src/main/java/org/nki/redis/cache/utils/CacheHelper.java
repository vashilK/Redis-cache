package org.nki.redis.cache.utils;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.nki.redis.cache.annotations.CacheSave;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;

/**
 * Author Neeschal Kissoon created on 04/11/2022
 */
public class CacheHelper {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static List<Class<?>> findClasses(File directory, String packageName) throws ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<>();
        if (!directory.exists()) {
            return classes;
        }

        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                String root = packageName.substring(1);
                classes.add(Class.forName(root + '.' + file.getName().substring(0, file.getName().length() - 6)));
            }
        }

        return classes;
    }


    public static List<Class<?>> getAllClasses() throws IOException, ClassNotFoundException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Enumeration<URL> resources = classLoader.getResources("./");
        List<File> dirs = new ArrayList<File>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }

        ArrayList<Class<?>> classes = new ArrayList<>();
        for (File directory : dirs) {
            classes.addAll(findClasses(directory, ""));
        }

        return classes;
    }


    public static Method getMethod(JoinPoint joinPoint) throws NoSuchMethodException {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        return joinPoint.getTarget().getClass().getMethod(signature.getMethod().getName(), signature.getMethod().getParameterTypes());
    }

    public static Method getMethod(ProceedingJoinPoint joinPoint) throws NoSuchMethodException {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        return joinPoint.getTarget().getClass().getMethod(signature.getMethod().getName(), signature.getMethod().getParameterTypes());
    }

    public static List<Method> getMethodsAnnotatedWith(final Class<?> type, final Class<? extends Annotation> annotation) {
        final List<Method> methods = new ArrayList<Method>();
        Class<?> clazz = type;

        while (clazz != Object.class) {
            for (final Method method : clazz.getDeclaredMethods()) {
                if (method.isAnnotationPresent(annotation)) {
                    methods.add(method);
                }
            }
            clazz = clazz.getSuperclass();
        }

        return methods;
    }

    public static String getPattern(JoinPoint joinPoint, Method method) throws JsonProcessingException {
        List<String> patternBuilder = new ArrayList<>();
        patternBuilder.add(method.getAnnotation(CacheSave.class).group());
        patternBuilder.add(method.getName());
        String pattern = String.join("::", patternBuilder);

        Object[] arguments = joinPoint.getArgs();
        List<String> args = new ArrayList<>();

        for (Object arg : arguments) {
            if (arg instanceof List) {
                List<?> array = (List<?>) arg;
                String dataType = array.size() > 0 ? array.stream().findFirst().map(type -> type.getClass().getSimpleName()).orElse("") : "Object";
                String jsonData = OBJECT_MAPPER.writeValueAsString(array);
                String key = "List<" + dataType + ">=" + jsonData;
                args.add(key);
            } else if (arg instanceof Set) {
                Set<?> array = (Set<?>) arg;
                String dataType = array.size() > 0 ? array.stream().findFirst().map(type -> type.getClass().getSimpleName()).orElse("") : "Object";
                String jsonData = OBJECT_MAPPER.writeValueAsString(array);
                String key = "Set<" + dataType + ">=" + jsonData;
                args.add(key);
            } else {
                String jsonData = OBJECT_MAPPER.writeValueAsString(arg);
                String key = arg.getClass().getSimpleName() + "=" + jsonData;
                args.add(key);
            }
        }

        return (pattern + "::" + String.join("\\§", args)).replace(", ", ",");
    }
}
