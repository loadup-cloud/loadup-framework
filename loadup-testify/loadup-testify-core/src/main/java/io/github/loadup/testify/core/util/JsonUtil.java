package io.github.loadup.testify.core.util;

/*-
 * #%L
 * Testify Core
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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.List;
import java.util.Map;

public class JsonUtil {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.registerModule(new JavaTimeModule());
        // 允许未知的属性，防止反序列化失败
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

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
