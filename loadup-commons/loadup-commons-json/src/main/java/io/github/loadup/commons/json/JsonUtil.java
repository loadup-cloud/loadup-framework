/*
 * #%L
 * LoadUp Common JSON
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
package io.github.loadup.commons.json;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.github.loadup.commons.enums.CommonResultCodeEnum;
import io.github.loadup.commons.error.CommonException;
import io.github.loadup.commons.log.LogUtil;
import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.ext.javatime.ser.LocalDateSerializer;
import tools.jackson.databind.ext.javatime.ser.LocalDateTimeSerializer;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 *
 *
 * <ul>
 * </ul>
 *
 *
 * <ul>
 * </ul>
 *
 * @author loadup_cloud
 * @since 1.0.0
 */
public class JsonUtil {

    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    public static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private static volatile ObjectMapper objectMapper = initObjectMapper();

    /**
     * Use the application mapper when Spring configures one.
     */
    public static void setObjectMapper(ObjectMapper mapper) {
        objectMapper = java.util.Objects.requireNonNull(mapper);
    }

    /**
     *
     *
     * <ul>
     *   <li>Register custom serializers for Java time types
     * </ul>
     *
     */
    private static ObjectMapper initObjectMapper() {
        return customize(JsonMapper.builder()).build();
    }

    /**
     * Apply the framework JSON conventions to a Jackson 3 mapper builder.
     */
    public static JsonMapper.Builder customize(JsonMapper.Builder builder) {
        SimpleModule javaTimeModule = new SimpleModule();
        javaTimeModule.addSerializer(
                LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT)));
        javaTimeModule.addDeserializer(LocalDate.class, new MultiLocalDateDeserializer());
        javaTimeModule.addSerializer(
                LocalDateTime.class,
                new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_FORMAT)));
        javaTimeModule.addDeserializer(LocalDateTime.class, new MultiLocalDateTimeDeserializer());

        SimpleModule module = new SimpleModule();
        module.addDeserializer(Date.class, new MultiDateDeserializer());
        return builder.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(DateTimeFeature.WRITE_DURATIONS_AS_TIMESTAMPS)
                .changeDefaultPropertyInclusion(ignored ->
                        com.fasterxml.jackson.annotation.JsonInclude.Value.construct(Include.ALWAYS, Include.ALWAYS))
                .defaultDateFormat(new SimpleDateFormat(DEFAULT_DATE_TIME_FORMAT))
                .defaultTimeZone(java.util.TimeZone.getDefault())
                .addModule(javaTimeModule)
                .addModule(module);
    }

    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public static String toJson(Object object) {
        if (object == null) {
            LogUtil.debug(JsonUtil.class, "Object is null, returning null");
            return null;
        }
        if (object instanceof String) {
            return (String) object;
        }
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JacksonException e) {
            LogUtil.error(
                    JsonUtil.class,
                    "JSON operation failed; operation={}; failureType={}",
                    "toJson",
                    e.getClass().getSimpleName());
            return null;
        }
    }

    public static <T> String toJsonStringPretty(T obj) {
        if (obj == null) {
            LogUtil.debug(JsonUtil.class, "Object is null, returning null");
            return null;
        }
        if (obj instanceof String) {
            return (String) obj;
        }
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (JacksonException e) {
            LogUtil.error(
                    JsonUtil.class,
                    "JSON operation failed; operation={}; failureType={}",
                    "toJsonStringPretty",
                    e.getClass().getSimpleName());
            return null;
        }
    }

    public static <T> T fromJson(String jsonString, Class<T> valueType) {
        if (jsonString == null || jsonString.isEmpty()) {
            LogUtil.debug(JsonUtil.class, "JSON string is null or empty, returning null");
            return null;
        }
        if (valueType == null) {
            LogUtil.warn(JsonUtil.class, "Value type is null, returning null");
            return null;
        }
        try {
            return objectMapper.readValue(jsonString, valueType);
        } catch (JacksonException e) {
            LogUtil.error(
                    JsonUtil.class,
                    "JSON operation failed; operation={}; failureType={}",
                    "fromJson",
                    e.getClass().getSimpleName());
            return null;
        }
    }

    public static <T> T fromJson(String str, TypeReference<T> typeReference) {
        if (str == null || str.isEmpty()) {
            LogUtil.debug(JsonUtil.class, "JSON string is null or empty, returning null");
            return null;
        }
        if (typeReference == null) {
            LogUtil.warn(JsonUtil.class, "TypeReference is null, returning null");
            return null;
        }

        try {
            if (typeReference.getType().equals(String.class)) {
                return (T) str;
            }
            return objectMapper.readValue(str, typeReference);
        } catch (JacksonException e) {
            LogUtil.error(
                    JsonUtil.class,
                    "JSON operation failed; operation={}; failureType={}",
                    "fromJson",
                    e.getClass().getSimpleName());
            return null;
        }
    }

    public static <T> T fromJson(String str, Class<?> collectionClass, Class<?>... elementClasses) {
        if (str == null || str.isEmpty()) {
            LogUtil.debug(JsonUtil.class, "JSON string is null or empty, returning null");
            return null;
        }
        if (collectionClass == null) {
            LogUtil.warn(JsonUtil.class, "Collection class is null, returning null");
            return null;
        }
        if (elementClasses == null || elementClasses.length == 0) {
            LogUtil.warn(JsonUtil.class, "Element classes is null or empty, returning null");
            return null;
        }

        JavaType javaType = objectMapper.getTypeFactory().constructParametricType(collectionClass, elementClasses);

        try {
            return objectMapper.readValue(str, javaType);
        } catch (JacksonException e) {
            LogUtil.error(
                    JsonUtil.class,
                    "JSON operation failed; operation={}; failureType={}",
                    "fromJson",
                    e.getClass().getSimpleName());
            return null;
        }
    }

    /**
     *
     * @param inputStream InputStream
     */
    public static <T> T fromJson(InputStream inputStream, Class<T> valueType) {
        if (inputStream == null) {
            LogUtil.warn(JsonUtil.class, "InputStream is null, returning null");
            return null;
        }
        if (valueType == null) {
            LogUtil.warn(JsonUtil.class, "Value type is null, returning null");
            return null;
        }
        try {
            return objectMapper.readValue(inputStream, valueType);
        } catch (JacksonException e) {
            LogUtil.error(
                    JsonUtil.class,
                    "JSON operation failed; operation={}; failureType={}",
                    "fromJson",
                    e.getClass().getSimpleName());
            return null;
        }
    }

    public static Map<String, Object> toMap(String jsonString) {
        return toJsonMap(jsonString);
    }

    public static <T> T mapToObject(Map<String, ?> map, Class<T> valueType) {
        if (map == null || map.isEmpty() || valueType == null) {
            LogUtil.warn(JsonUtil.class, "Map or valueType is null/empty, returning null");
            return null;
        }
        String jsonString = toJson(map);
        if (jsonString == null) {
            LogUtil.warn(JsonUtil.class, "Failed to convert map to JSON string");
            return null;
        }
        return fromJson(jsonString, valueType);
    }

    public static <T> T mapToObject(Map<String, ?> map, Class<?> collectionClass, Class<?>... elementClasses) {
        if (map == null || map.isEmpty() || collectionClass == null) {
            LogUtil.warn(JsonUtil.class, "Map or collectionClass is null/empty, returning null");
            return null;
        }
        if (elementClasses == null || elementClasses.length == 0) {
            LogUtil.warn(JsonUtil.class, "Element classes is null or empty, returning null");
            return null;
        }
        String jsonString = toJson(map);
        if (jsonString == null) {
            LogUtil.warn(JsonUtil.class, "Failed to convert map to JSON string");
            return null;
        }
        return fromJson(jsonString, collectionClass, elementClasses);
    }

    public static <T> T mapToObject(Map<String, ?> map, JavaType javaType) {
        if (map == null || map.isEmpty() || javaType == null) {
            LogUtil.warn(JsonUtil.class, "Map or javaType is null/empty, returning null");
            return null;
        }
        String jsonString = toJson(map);
        if (jsonString == null) {
            LogUtil.warn(JsonUtil.class, "Failed to convert map to JSON string");
            return null;
        }
        try {
            return objectMapper.readValue(jsonString, javaType);
        } catch (JacksonException e) {
            LogUtil.error(
                    JsonUtil.class,
                    "JSON operation failed; operation={}; failureType={}",
                    "mapToObject",
                    e.getClass().getSimpleName());
            return null;
        }
    }

    public static Map<String, Object> toJsonMap(String jsonString) {
        if (jsonString == null || jsonString.isBlank()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(jsonString, new TypeReference<>() {});
        } catch (JacksonException e) {
            LogUtil.error(
                    JsonUtil.class,
                    "JSON operation failed; operation={}; failureType={}",
                    "toJsonMap",
                    e.getClass().getSimpleName());
            return new HashMap<>();
        }
    }

    public static Map<String, String> toStringMap(String jsonString) {
        if (jsonString == null || jsonString.isBlank()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(jsonString, new TypeReference<>() {});
        } catch (JacksonException e) {
            LogUtil.error(
                    JsonUtil.class,
                    "JSON operation failed; operation={}; failureType={}",
                    "toStringMap",
                    e.getClass().getSimpleName());
            return new HashMap<>();
        }
    }

    public static <T> T fromJson(File file, Class<T> valueType) {
        if (file == null) {
            throw new IllegalArgumentException("File cannot be null");
        }
        if (valueType == null) {
            throw new IllegalArgumentException("Value type cannot be null");
        }
        if (!file.exists()) {
            throw new IllegalArgumentException("File does not exist: " + file.getAbsolutePath());
        }
        if (!file.isFile()) {
            throw new IllegalArgumentException("Path is not a file: " + file.getAbsolutePath());
        }
        try {
            return objectMapper.readValue(file, valueType);
        } catch (JacksonException e) {
            LogUtil.error(
                    JsonUtil.class,
                    "JSON operation failed; operation={}; failureType={}",
                    "fromJson",
                    e.getClass().getSimpleName());
            throw new CommonException(CommonResultCodeEnum.SYS_ERROR, e);
        }
    }

    public static void toFile(File file, Object object) {
        if (file == null) {
            throw new IllegalArgumentException("File cannot be null");
        }
        if (object == null) {
            throw new IllegalArgumentException("Object cannot be null");
        }

        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists() && !parentDir.mkdirs()) {
            throw new CommonException(CommonResultCodeEnum.SYS_ERROR);
        }
        try {
            objectMapper.writeValue(file, object);
        } catch (JacksonException e) {
            LogUtil.error(
                    JsonUtil.class,
                    "JSON operation failed; operation={}; failureType={}",
                    "toFile",
                    e.getClass().getSimpleName());
            throw new CommonException(CommonResultCodeEnum.SYS_ERROR, e);
        }
    }

    public static JsonNode getSubNode(String jsonString, String path) {
        if (jsonString == null || jsonString.isEmpty()) {
            LogUtil.debug(JsonUtil.class, "JSON string is null or empty, returning null");
            return null;
        }
        if (path == null || path.isEmpty()) {
            LogUtil.warn(JsonUtil.class, "Path is null or empty, returning null");
            return null;
        }
        try {
            JsonNode rootNode = objectMapper.readTree(jsonString);
            return rootNode.at(path);
        } catch (JacksonException e) {
            LogUtil.error(
                    JsonUtil.class,
                    "JSON operation failed; operation={}; failureType={}",
                    "getSubNode",
                    e.getClass().getSimpleName());
            return null;
        }
    }

    public static JsonNode toJsonNodeTree(String jsonString) {
        if (jsonString == null || jsonString.isEmpty()) {
            LogUtil.debug(JsonUtil.class, "JSON string is null or empty, returning null");
            return null;
        }
        try {
            return objectMapper.readTree(jsonString);
        } catch (JacksonException e) {
            LogUtil.error(
                    JsonUtil.class,
                    "JSON operation failed; operation={}; failureType={}",
                    "toJsonNodeTree",
                    e.getClass().getSimpleName());
            return null;
        }
    }

    public static ObjectNode createEmptyJsonObject() {
        return objectMapper.createObjectNode();
    }

    public static ArrayNode createEmptyJsonArray() {
        return objectMapper.createArrayNode();
    }

    public static void addElementToJsonArray(ArrayNode arrayNode, Object element) {
        if (arrayNode == null) {
            throw new IllegalArgumentException("ArrayNode cannot be null");
        }
        if (element == null) {
            LogUtil.debug(JsonUtil.class, "Element is null, skipping add operation");
            return;
        }
        try {
            JsonNode jsonNode = objectMapper.valueToTree(element);
            arrayNode.add(jsonNode);
        } catch (IllegalArgumentException e) {
            LogUtil.error(
                    JsonUtil.class,
                    "JSON operation failed; operation={}; failureType={}",
                    "addElementToJsonArray",
                    e.getClass().getSimpleName());
        }
    }

    public static <T> List<T> fromJson(ArrayNode arrayNode, Class<T> valueType) {
        List<T> objectList = new ArrayList<>();
        if (arrayNode == null) {
            LogUtil.debug(JsonUtil.class, "ArrayNode is null, returning empty list");
            return objectList;
        }
        if (valueType == null) {
            LogUtil.warn(JsonUtil.class, "Value type is null, returning empty list");
            return objectList;
        }

        for (JsonNode jsonNode : arrayNode) {
            T object = fromJson(jsonNode.toString(), valueType);
            if (object != null) {
                objectList.add(object);
            }
        }
        return objectList;
    }

    public static <T> T convert(Object value, Class<T> clazz) {
        return objectMapper.convertValue(value, clazz);
    }

    public static <T> T convert(Object value, TypeReference<T> typeRef) {
        return objectMapper.convertValue(value, typeRef);
    }

    public static byte[] toBytes(Object value) {
        try {
            return objectMapper.writeValueAsBytes(value);
        } catch (JacksonException e) {
            return new byte[0];
        }
    }

    public static <T> T fromBytes(byte[] bytes, TypeReference<T> typeRef) {
        try {
            return objectMapper.readValue(bytes, typeRef);
        } catch (JacksonException e) {
            return null;
        }
    }

    public static boolean isValidJson(byte[] bytes) {
        try {

            objectMapper.readTree(bytes);
            return true;
        } catch (JacksonException e) {
            return false;
        }
    }

    public static boolean isValidJson(String jsonString) {
        try {
            objectMapper.readTree(jsonString);
            return true;
        } catch (JacksonException e) {
            return false;
        }
    }

    public static List<Map<String, Object>> toListMap(JsonNode node) {
        return objectMapper.convertValue(node, new TypeReference<>() {});
    }

    public static JsonNode valueToTree(Object obj) {
        return objectMapper.valueToTree(obj);
    }

    public static <T> T convertValue(JsonNode exp, TypeReference<T> typeReference) {
        return objectMapper.convertValue(exp, typeReference);
    }

    public static <T> T convertValue(String exp, Class<T> parameterType) {
        return objectMapper.convertValue(exp, parameterType);
    }

    public static <T> T convertValue(Map<String, Object> variables, TypeReference<T> typeReference) {
        return objectMapper.convertValue(variables, typeReference);
    }

    public static <T> T convertValue(JsonNode jsonNode, Class<T> parameterType) {
        return objectMapper.convertValue(jsonNode, parameterType);
    }

    public static Object convertValue(Object resolvedValue, Class<?> returnType) {
        return objectMapper.convertValue(resolvedValue, returnType);
    }

    public static boolean equals(Object expected, Object actual) {
        return toJson(expected).equals(toJson(actual));
    }

    public static JsonNode readTree(String text) {
        return objectMapper.readTree(text);
    }
}
