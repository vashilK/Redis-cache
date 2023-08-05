package org.nki.redis.cache.generator;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Author Neeschal Kissoon created on 05/08/2023
 */
public class VersionUpdater {
    public static void main(String[] args) {
        try {
            MavenXpp3Reader reader = new MavenXpp3Reader();
            Model model = reader.read(new FileReader("pom.xml"));
            String version = model.getVersion();
            System.out.println("Maven project version: " + version);
            String filePath = "README.md";

            try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
                List<String> result = br
                        .lines()
                        .map(item -> {
                            if (item.contains("implementation(\"io.github.vashilk:redis-cache:")) {
                                return "implementation(\"io.github.vashilk:redis-cache:" + version +
                                        "\")";
                            }

                            if (item.contains("<!-- io.github.vashilk.version -->")) {
                                return "<version>" + version +
                                        "</version> <!-- io.github.vashilk.version -->";
                            }

                            return item;
                        }).collect(Collectors.toList());

                String updatedContent = String.join("\n", result);

                try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
                    bw.write(updatedContent);
                }
            } catch (IOException e) {
                System.err.println("Error reading the file: " + e.getMessage());
            }
        } catch (XmlPullParserException | IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
