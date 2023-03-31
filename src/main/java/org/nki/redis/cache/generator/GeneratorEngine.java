package org.nki.redis.cache.generator;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Author Neeschal Kissoon created on 17/12/2022
 */
public class GeneratorEngine {

    private GeneratorEngine() {
    }

    public static GeneratorEngine init() {
        return new GeneratorEngine();
    }

    public void create(String template, GeneratorContext generatorContext, String fileName, String extension) {
        try {
            HashMap<String, String> parameters = generatorContext.getParameters();
            if (parameters.isEmpty()) {
                throw new RuntimeException("Parameters are required!");
            }

            validateParameters(parameters);
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                String target = entry.getKey();
                String replacement = entry.getValue();
                template = template.replace(target, replacement);
            }

            String packageRoot = Objects.nonNull(parameters.get("packageName")) ? parameters.get("packageName").replace(".", "/") + "/" : "";
            generateDirectories(packageRoot);
            Writer writer = new FileWriter(new File(getProjectPath() + "/target/generated-sources/main/java/" + packageRoot + fileName + "TypeReference." + extension));
            writer.append(template);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e.getCause());
        }
    }

    private static void generateDirectories(String packageRoot) {
        File theDir = new File(getProjectPath() + "/target/generated-sources/main/java/" + packageRoot);
        if (!theDir.exists()) {
            boolean created = theDir.mkdirs();
        }
    }

    private static String getProjectPath() {
        String path = Objects.requireNonNull(ModelGenerator.class.getClassLoader().getResource("")).getPath();
        String fullPath = URLDecoder.decode(path, StandardCharsets.UTF_8);
        String[] absolutePath = fullPath.split("target");
        return absolutePath[0];
    }

    private static void validateParameters(HashMap<String, String> parameters) {
        try {
            parameters.get("packageName");
        } catch (Exception e) {
            throw new RuntimeException("Error, required parameter packageName missing...");
        }
    }
}
