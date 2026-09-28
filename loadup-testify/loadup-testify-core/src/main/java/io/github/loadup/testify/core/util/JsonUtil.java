package io.github.loadup.testify.core.util;

import java.util.List;
import java.util.Map;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;

public class JsonUtil {
    private static final ObjectMapper MAPPER = JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();

    /**
     * 将对象转为 JSON 字符串
     */
    public static String toJson(Object obj) {
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("Json serialization failed", e);
        }
    }

    /**
     * 将 JsonNode 转换为特定的 List<Map>，用于 DbAssertEngine
     */
    public static List<Map<String, Object>> toListMap(JsonNode node) {
        return MAPPER.convertValue(node, new TypeReference<>() {});
    }

    public static JsonNode valueToTree(Object obj) {
        return MAPPER.valueToTree(obj);
    }

    /**
     * Parse a JSON string into a {@link JsonNode}.
     *
     * @param json JSON source text
     * @return parsed tree
     * @throws RuntimeException if the text is not valid JSON
     */
    public static JsonNode readTree(String json) {
        try {
            return MAPPER.readTree(json);
        } catch (Exception e) {
            throw new RuntimeException("JSON parse failed", e);
        }
    }

    public static <T> T convertValue(JsonNode exp, TypeReference<T> typeReference) {
        return MAPPER.convertValue(exp, typeReference);
    }

    public static <T> T convertValue(String exp, Class<T> parameterType) {
        return MAPPER.convertValue(exp, parameterType);
    }

    public static <T> T convertValue(Map<String, Object> variables, TypeReference<T> typeReference) {
        return MAPPER.convertValue(variables, typeReference);
    }

    public static <T> T convertValue(JsonNode jsonNode, Class<T> parameterType) {
        return MAPPER.convertValue(jsonNode, parameterType);
    }

    public static Object convertValue(Object resolvedValue, Class<?> returnType) {
        return MAPPER.convertValue(resolvedValue, returnType);
    }

    public static boolean equals(Object expected, Object actual) {
        return JsonUtil.toJson(expected).equals(JsonUtil.toJson(actual));
    }
}
