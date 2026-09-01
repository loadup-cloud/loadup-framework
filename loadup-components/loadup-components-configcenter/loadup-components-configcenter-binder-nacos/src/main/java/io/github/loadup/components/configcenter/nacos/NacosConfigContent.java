package io.github.loadup.components.configcenter.nacos;

/*-
 * #%L
 * LoadUp ConfigCenter Binder Nacos
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.loadup.commons.util.JsonUtil;
import java.io.StringReader;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import org.yaml.snakeyaml.Yaml;

/**
 * Parses and renders Nacos file content (properties / yaml) for the key-value facade.
 *
 * <p>Nacos stores one file per {@code dataId}; the LoadUp facade exposes individual keys. This
 * class flattens the file into a {@code Map<String, String>} (yaml nesting becomes dotted keys,
 * collections become JSON strings) and renders the map back to the original format for
 * read-modify-write operations.
 */
public final class NacosConfigContent {

    private static final String YAML = "yaml";
    private static final String YML = "yml";
    private static final ObjectMapper MAPPER = JsonUtil.getObjectMapper();

    private NacosConfigContent() {
        // Utility class
    }

    /**
     * Parse file content into a flat key-value map.
     *
     * @param content raw file content; may be {@code null} or blank
     * @param fileExtension properties / yaml / yml; anything else is treated as properties
     * @return ordered key-value map, never {@code null}
     */
    public static Map<String, String> parse(String content, String fileExtension) {
        if (content == null || content.isBlank()) {
            return new LinkedHashMap<>();
        }
        if (YAML.equalsIgnoreCase(fileExtension) || YML.equalsIgnoreCase(fileExtension)) {
            return parseYaml(content);
        }
        return parseProperties(content);
    }

    /**
     * Render a key-value map back to file content.
     *
     * @param configs key-value map
     * @param fileExtension properties / yaml / yml; anything else is rendered as properties
     * @return file content
     */
    public static String render(Map<String, String> configs, String fileExtension) {
        if (YAML.equalsIgnoreCase(fileExtension) || YML.equalsIgnoreCase(fileExtension)) {
            return renderYaml(configs);
        }
        return renderProperties(configs);
    }

    private static Map<String, String> parseProperties(String content) {
        Properties properties = new Properties();
        try {
            properties.load(new StringReader(content));
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse properties content", e);
        }
        Map<String, String> result = new LinkedHashMap<>();
        properties.forEach((key, value) -> result.put(String.valueOf(key), String.valueOf(value)));
        return result;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, String> parseYaml(String content) {
        Object root = new Yaml().load(content);
        if (!(root instanceof Map)) {
            return new LinkedHashMap<>();
        }
        Map<String, String> result = new LinkedHashMap<>();
        flatten((Map<String, Object>) root, "", result);
        return result;
    }

    private static void flatten(Map<String, Object> map, String prefix, Map<String, String> out) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Map<?, ?> nested) {
                Map<String, Object> nestedMap = new LinkedHashMap<>();
                nested.forEach((k, v) -> nestedMap.put(String.valueOf(k), v));
                flatten(nestedMap, key, out);
            } else if (value == null) {
                out.put(key, "");
            } else if (value instanceof Collection<?> || value.getClass().isArray()) {
                out.put(key, toJson(value));
            } else {
                out.put(key, String.valueOf(value));
            }
        }
    }

    private static String renderProperties(Map<String, String> configs) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> entry : configs.entrySet()) {
            builder.append(entry.getKey())
                    .append('=')
                    .append(escapePropertyValue(entry.getValue()))
                    .append('\n');
        }
        return builder.toString();
    }

    private static String renderYaml(Map<String, String> configs) {
        Map<String, Object> nested = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : configs.entrySet()) {
            unflatten(nested, entry.getKey(), entry.getValue());
        }
        return new Yaml().dump(nested);
    }

    private static void unflatten(Map<String, Object> target, String dottedKey, String value) {
        String[] parts = dottedKey.split("\\.");
        Map<String, Object> current = target;
        for (int i = 0; i < parts.length - 1; i++) {
            Object next = current.get(parts[i]);
            if (next instanceof Map<?, ?> nested) {
                Map<String, Object> existing = new LinkedHashMap<>();
                nested.forEach((k, v) -> existing.put(String.valueOf(k), v));
                current.put(parts[i], existing);
                current = existing;
            } else {
                Map<String, Object> child = new LinkedHashMap<>();
                current.put(parts[i], child);
                current = child;
            }
        }
        current.put(parts[parts.length - 1], toObject(value));
    }

    private static Object toObject(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if ((trimmed.startsWith("[") && trimmed.endsWith("]")) || (trimmed.startsWith("{") && trimmed.endsWith("}"))) {
            try {
                return MAPPER.readValue(trimmed, Object.class);
            } catch (Exception ignored) {
                // fall through and keep the raw string
            }
        }
        return value;
    }

    private static String escapePropertyValue(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\n", "\\n").replace("\r", "\\r");
    }

    private static String toJson(Object value) {
        try {
            return MAPPER.writeValueAsString(value);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to serialize collection value", e);
        }
    }
}
