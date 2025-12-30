package com.github.loadup.commons.util;

/*-
 * #%L
 * loadup-commons-util
 * %%
 * Copyright (C) 2022 - 2024 loadup_cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.github.loadup.commons.constant.CommonConstants;
import com.github.loadup.commons.result.SingleResponse;
import com.github.loadup.commons.util.json.MultiDateDeserializer;
import org.apache.commons.collections4.MapUtils;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class JsonUtil {

    private static final ObjectMapper objectMapper = initObjectMapper();

    public static ObjectMapper initObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // 忽略 在json字符串中存在，但是在java对象中不存在对应属性的情况。防止错误
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        // 忽略空Bean转json的错误
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        // 取消默认转换timestamps形式
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // 对象的所有字段全部列入
        mapper.setSerializationInclusion(Include.ALWAYS);
        // 所有的日期格式都统一为以下的样式，即yyyy-MM-dd HH:mm:ss
        mapper.setDateFormat(new SimpleDateFormat(CommonConstants.DEFAULT_DATE_FORMAT));
        // mapper.activateDefaultTypingAsProperty(mapper.getPolymorphicTypeValidator(),
        // ObjectMapper.DefaultTyping.NON_FINAL, "@type");

        // 注册 JavaTimeModule 处理 java.time 包的类
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(
            LocalDate.class,
            new LocalDateSerializer(DateTimeFormatter.ofPattern(CommonConstants.DEFAULT_DATE_FORMAT)));
        javaTimeModule.addDeserializer(
            LocalDate.class,
            new LocalDateDeserializer(DateTimeFormatter.ofPattern(CommonConstants.DEFAULT_DATE_FORMAT)));
        javaTimeModule.addSerializer(
            LocalDateTime.class,
            new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(CommonConstants.DEFAULT_DATE_TIME_FORMAT)));
        javaTimeModule.addDeserializer(
            LocalDateTime.class,
            new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(CommonConstants.DEFAULT_DATE_TIME_FORMAT)));
        mapper.registerModule(javaTimeModule);

        SimpleModule module = new SimpleModule();
        module.addDeserializer(Date.class, new MultiDateDeserializer());
        mapper.registerModule(module);
        return mapper;
    }

    /**
     * 将对象转换为JSON字符串
     */
    public static String toJSONString(Object object) {
        if (object == null) {
            return null;
        }
        try {
            return object instanceof String ? (String) object : objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static <T> String toJSONStringPretty(T obj) {

        if (obj == null) {
            return null;
        }
        try {
            return obj instanceof String
                ? (String) obj
                : objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 将JSON字符串转换为指定类型的对象
     */
    public static <T> T parseObject(String jsonString, Class<T> valueType) {
        if (StringUtils.isEmpty(jsonString) || valueType == null) {
            return null;
        }
        try {
            return objectMapper.readValue(jsonString, valueType);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static <T> T parseObject(String str, TypeReference<T> typeReference) {

        if (StringUtils.isEmpty(str) || typeReference == null) {
            return null;
        }

        try {
            return (T)
                (typeReference.getType().equals(String.class) ? str : objectMapper.readValue(str, typeReference));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static <T> T parseObject(String str, Class<?> collectionClass, Class<?>... elementClasses) {

        JavaType javaType = objectMapper.getTypeFactory().constructParametricType(collectionClass, elementClasses);

        try {
            return objectMapper.readValue(str, javaType);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Json string to map
     *
     * @param jsonString
     * @return
     */
    public static Map toMap(String jsonString) {
        return parseObject(jsonString, Map.class);
    }

    /**
     * parse map to object
     *
     * @param map
     * @param valueType
     * @param <T>
     * @return
     */
    public static <T> T parseObject(Map map, Class<T> valueType) {
        if (MapUtils.isEmpty(map) || valueType == null) {
            return null;
        }
        String jsonString = toJSONString(map);
        return parseObject(jsonString, valueType);
    }

    public static <T> T parseObject(Map map, Class<?> collectionClass, Class<?>... elementClasses) {
        if (MapUtils.isEmpty(map) || collectionClass == null) {
            return null;
        }
        String jsonString = toJSONString(map);
        return parseObject(jsonString, collectionClass, elementClasses);
    }

    public static Object parseObject(Map<String, String> value, JavaType javaType) {
        String jsonString = toJSONString(value);
        try {
            return objectMapper.readValue(jsonString, javaType);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    /**
     * 从JSON字符串中获取指定路径下的子节点
     */
    public static JsonNode getSubNode(String jsonString, String path) {
        try {
            JsonNode rootNode = objectMapper.readTree(jsonString);
            return rootNode.at(path);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 创建一个空的JSON对象节点
     */
    public static ObjectNode createEmptyJsonObject() {
        return objectMapper.createObjectNode();
    }

    /**
     * 创建一个空的JSON数组节点
     */
    public static ArrayNode createEmptyJsonArray() {
        return objectMapper.createArrayNode();
    }

    /**
     * 向JSON数组节点中添加元素
     */
    public static void addElementToJsonArray(ArrayNode arrayNode, Object element) {
        try {
            JsonNode jsonNode = objectMapper.valueToTree(element);
            arrayNode.add(jsonNode);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 将JSON数组节点转换为指定类型的对象列表
     */
    public static <T> List<T> parseObject(ArrayNode arrayNode, Class<T> valueType) {
        List<T> objectList = new ArrayList<>();
        if (arrayNode != null) {
            for (JsonNode jsonNode : arrayNode) {
                T object = parseObject(jsonNode.toString(), valueType);
                if (object != null) {
                    objectList.add(object);
                }
            }
        }
        return objectList;
    }

    public static JsonNode toJsonNodeTree(String jsonString) {
        try {
            return objectMapper.readTree(jsonString);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Map<String, Object> toJsonMap(String jsonString) {
        if (StringUtils.isBlank(jsonString)) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(jsonString, new TypeReference<>() {});
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    public static Map<String, String> jsonToMap(String jsonString) {
        if (StringUtils.isBlank(jsonString)) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(jsonString, new TypeReference<>() {});
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    public static void main(String[] args) {
        Map<String, String> map = new HashMap<>();
        map.put("aaa", "aaa");
        map.put("bbb", "bbb");
        map.put("bcc", "ccc");
        String jsonString = "{\n"
            + "  \"data\" : {\n"
            + "    \"account\" : \"123\",\n"
            + "    \"birthday\" : \"$today\"\n"
            + "  },\n"
            + "  \"result\" : {\n"
            + "    \"code\" : \"SUCCESS\",\n"
            + "    \"message\" : \"Success.\",\n"
            + "    \"status\" : \"S\"\n"
            + "  }\n"
            + "}";
        System.out.println(JsonUtil.parseObject(jsonString, SingleResponse.class));
    }
}
