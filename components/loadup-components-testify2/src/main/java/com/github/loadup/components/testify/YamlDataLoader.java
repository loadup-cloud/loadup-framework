package com.github.loadup.components.testify;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.*;
import java.util.Map;

public class YamlDataLoader {

    public Map<String, Object> loadWithDetails(String path) throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalArgumentException("File not found: " + path);
            return new ObjectMapper(new YAMLFactory()).readValue(is, new TypeReference<>() {});
        }
    }

    public static String replacePlaceholders(String query, Map<String, Object> context) {
        for (var entry : context.entrySet()) {
            if (entry.getValue() instanceof String || entry.getValue() instanceof Number) {
                query = query.replaceAll("\\$\\{" + entry.getKey() + "\\}", entry.getValue().toString());
            }
        }
        return query;
    }
}
