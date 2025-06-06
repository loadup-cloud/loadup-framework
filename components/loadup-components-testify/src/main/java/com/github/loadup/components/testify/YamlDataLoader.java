package com.github.loadup.components.testify;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class YamlDataLoader {

    /**
     * 加载 YAML 文件并返回结构化数据
     *
     * @param filePath 文件路径（classpath 下）
     * @return 包含 query、data、ignore、match 的 Map
     */
    public Map<String, Object> loadWithDetails(String filePath) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(filePath)) {
            if (is == null) {
                throw new IllegalArgumentException("File not found in classpath: " + filePath);
            }

            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

            // 将整个 YAML 文件解析为 Map
            Map<String, Object> root = mapper.readValue(is, new TypeReference<>() {});

            // 校验必须字段
            validateRequiredFields(root);

            return root;

        } catch (IOException e) {
            throw new RuntimeException("Failed to read YAML file: " + filePath, e);
        }
    }

    /**
     * 验证必须字段是否存在
     */
    private void validateRequiredFields(Map<String, Object> root) {
        if (!root.containsKey("query")) {
            throw new IllegalArgumentException("Missing required field: 'query'");
        }
        if (!root.containsKey("data")) {
            throw new IllegalArgumentException("Missing required field: 'data'");
        }
    }

    /**
     * 替换 SQL 中的占位符 ${xxx}
     */
    public static String replacePlaceholders(String query, Map<String, Object> context) {
        for (Map.Entry<String, Object> entry : context.entrySet()) {
            if (entry.getValue() instanceof String || entry.getValue() instanceof Number) {
                query = query.replaceAll(
                        "\\$\\{" + entry.getKey() + "\\}",
                        entry.getValue().toString()
                );
            }
        }
        return query;
    }
}
