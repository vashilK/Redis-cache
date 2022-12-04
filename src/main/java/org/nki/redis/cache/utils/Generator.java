package org.nki.redis.cache.utils;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.nki.redis.cache.annotations.RedisCacheSerializable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Author Neeschal Kissoon created on 28/11/2022
 */
public class Generator {

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        List<Class<?>> classes = CacheHelper.getAllClasses();

        List<Class<?>> classesWithAnnotations = classes
                .stream()
                .filter(clazz -> Arrays.stream(clazz.getAnnotations()).anyMatch(annotation -> Objects.equals(annotation.annotationType(), RedisCacheSerializable.class)))
                .distinct()
                .collect(Collectors.toList());

        classesWithAnnotations.addAll(Transformer.rawTypes);

        classesWithAnnotations.forEach(clazz -> {
            String packageName = (clazz.getPackage().getName()).contains("java") ? "org.nki.redis.cache.model" : clazz.getPackage().getName();
            String modelName = clazz.getSimpleName();
            String classLocation = clazz.getPackage().getName();

            try {
                generateMultipleObj(packageName, modelName, classLocation, "List");
                generateMultipleObj(packageName, modelName, classLocation, "Set");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static void generateDirectories(String packageRoot) {
        File theDir = new File("./target/generated-sources/annotations/main/java/" + packageRoot);
        if (!theDir.exists()) {
            boolean created = theDir.mkdirs();
            System.out.println("Created: " + created);
        }
    }

    protected static void generateMultipleObj(String packageName, String modelName, String classLocation, String dataStructure) throws IOException {
        VelocityEngine velocityEngine = initVelocityEngine();
        VelocityContext context = new VelocityContext();

        if (packageName != null) {
            context.put("packagename", packageName);
        }

        context.put("className", (modelName + dataStructure + "TypeReference"));
        context.put("dataStructure", dataStructure);
        context.put("classLocation", classLocation + "." + modelName);
        context.put("type", "TypeReference<" + dataStructure + "<" + modelName + ">>");
        context.put("gtype", "TypeReference<" + dataStructure + "<" + modelName + ">>");

        String packageRoot = Objects.nonNull(packageName) ? packageName.replace(".", "/") + "/" : "";
        generateDirectories(packageRoot);

        Writer writer = new FileWriter(new File("./target/generated-sources/annotations/main/java/" + packageRoot + modelName + dataStructure + "TypeReference.java"));
        velocityEngine.mergeTemplate("vtemplates/listTypeReference.vm", "UTF-8", context, writer);
        writer.flush();
        writer.close();
    }

    private static VelocityEngine initVelocityEngine() {
        VelocityEngine velocityEngine = new VelocityEngine();
        velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        velocityEngine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        velocityEngine.init();
        return velocityEngine;
    }
}
