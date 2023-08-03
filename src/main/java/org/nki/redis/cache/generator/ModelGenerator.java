package org.nki.redis.cache.generator;


import org.nki.redis.cache.annotations.RedisCacheSerializable;
import org.nki.redis.cache.exceptions.IoException;
import org.nki.redis.cache.utils.CacheHelper;
import org.nki.redis.cache.utils.Transformer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Author Neeschal Kissoon created on 28/11/2022
 */
public class ModelGenerator {

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        List<Class<?>> classes = CacheHelper.getAllClasses();

        List<Class<?>> classesWithAnnotations = classes
                .stream()
                .filter(clazz -> Arrays.stream(clazz.getAnnotations()).anyMatch(
                        annotation -> Objects.equals(annotation.annotationType(),
                                RedisCacheSerializable.class)))
                .distinct()
                .collect(Collectors.toList());

        classesWithAnnotations.addAll(Transformer.rawTypes);

        classesWithAnnotations.forEach(clazz -> {
            String packageName = (clazz.getPackage().getName()).contains(
                    "java") ? "org.nki.redis.cache.model" : clazz.getPackage().getName();
            String modelName = clazz.getSimpleName();
            String classLocation = clazz.getPackage().getName();

            try {
                generateTypeReferenceObj(packageName, modelName, classLocation, "List");
                generateTypeReferenceObj(packageName, modelName, classLocation, "Set");

            } catch (IOException e) {
                throw new IoException(e.getMessage());
            }
        });
    }

    protected static void generateTypeReferenceObj(String packageName, String modelName, String classLocation, String dataStructure)
            throws IOException {
        GeneratorEngine generatorEngine = GeneratorEngine.init();
        GeneratorContext context = new GeneratorContext();

        if (packageName != null) {
            context.put("packagename", packageName);
        }

        context.put("className", (modelName + dataStructure + "TypeReference"));
        context.put("dataStructure", dataStructure);
        context.put("classLocation", classLocation + "." + modelName);
        context.put("type", "TypeReference<" + dataStructure + "<" + modelName + ">>");
        context.put("gtype", "TypeReference<" + dataStructure + "<" + modelName + ">>");

        InputStream inputStream =
                ModelGenerator.class.getResourceAsStream("/templates/listTypeReference.txt");
        String data = readFromInputStream(inputStream);
        generatorEngine.create(data, context, (modelName + dataStructure), "java");
    }

    private static String readFromInputStream(InputStream inputStream) throws IOException {
        StringBuilder resultStringBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                resultStringBuilder.append(line).append("\n");
            }
        }

        return resultStringBuilder.toString();
    }
}
