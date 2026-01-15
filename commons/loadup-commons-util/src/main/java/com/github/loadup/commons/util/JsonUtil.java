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
import com.github.loadup.commons.util.json.MultiDateDeserializer;
import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JSON工具类，基于Jackson实现JSON与Java对象之间的转换
 *
 * <p>提供以下功能：
 *
 * <ul>
 *   <li>对象与JSON字符串的相互转换
 *   <li>JSON字符串与Map的相互转换
 *   <li>JSON文件的读写
 *   <li>JSON节点操作
 *   <li>支持泛型、集合类型的转换
 * </ul>
 *
 * <p>配置说明：
 *
 * <ul>
 *   <li>忽略未知属性，避免反序列化失败
 *   <li>忽略空Bean序列化错误
 *   <li>日期格式统一为 yyyy-MM-dd HH:mm:ss
 *   <li>支持Java 8时间API（LocalDate、LocalDateTime）
 *   <li>支持多种日期格式的反序列化
 * </ul>
 *
 * @author loadup_cloud
 * @since 1.0.0
 */
public class JsonUtil {

  private static final Logger log = LoggerFactory.getLogger(JsonUtil.class);

  private static final ObjectMapper objectMapper = initObjectMapper();

  /**
   * 初始化ObjectMapper实例
   *
   * <p>配置项：
   *
   * <ul>
   *   <li>忽略未知属性：FAIL_ON_UNKNOWN_PROPERTIES = false
   *   <li>忽略空Bean：FAIL_ON_EMPTY_BEANS = false
   *   <li>日期不转换为时间戳：WRITE_DATES_AS_TIMESTAMPS = false
   *   <li>包含所有字段：ALWAYS
   *   <li>日期格式：yyyy-MM-dd HH:mm:ss
   *   <li>注册JavaTimeModule处理Java 8时间类型
   *   <li>注册MultiDateDeserializer支持多种日期格式
   * </ul>
   *
   * @return 配置好的ObjectMapper实例
   */
  private static ObjectMapper initObjectMapper() {
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
        new LocalDateDeserializer(
            DateTimeFormatter.ofPattern(CommonConstants.DEFAULT_DATE_FORMAT)));
    javaTimeModule.addSerializer(
        LocalDateTime.class,
        new LocalDateTimeSerializer(
            DateTimeFormatter.ofPattern(CommonConstants.DEFAULT_DATE_TIME_FORMAT)));
    javaTimeModule.addDeserializer(
        LocalDateTime.class,
        new LocalDateTimeDeserializer(
            DateTimeFormatter.ofPattern(CommonConstants.DEFAULT_DATE_TIME_FORMAT)));
    mapper.registerModule(javaTimeModule);

    SimpleModule module = new SimpleModule();
    module.addDeserializer(Date.class, new MultiDateDeserializer());
    mapper.registerModule(module);
    return mapper;
  }

  /**
   * 获取ObjectMapper实例
   *
   * <p>用于需要自定义配置或直接使用ObjectMapper的场景
   *
   * @return ObjectMapper实例
   */
  public static ObjectMapper getObjectMapper() {
    return objectMapper;
  }

  // ==================== 序列化方法 ====================

  /**
   * 将对象转换为JSON字符串
   *
   * @param object 要转换的对象
   * @return JSON字符串，如果转换失败或对象为null则返回null
   */
  public static String toJson(Object object) {
    if (object == null) {
      log.debug("Object is null, returning null");
      return null;
    }
    if (object instanceof String) {
      return (String) object;
    }
    try {
      return objectMapper.writeValueAsString(object);
    } catch (JsonProcessingException e) {
      log.error(
          "Failed to convert object to JSON string, object type: {}",
          object.getClass().getName(),
          e);
      return null;
    }
  }

  /**
   * 将对象转换为格式化的JSON字符串（带缩进）
   *
   * @param obj 要转换的对象
   * @param <T> 对象类型
   * @return 格式化的JSON字符串，如果转换失败或对象为null则返回null
   */
  public static <T> String toJsonStringPretty(T obj) {
    if (obj == null) {
      log.debug("Object is null, returning null");
      return null;
    }
    if (obj instanceof String) {
      return (String) obj;
    }
    try {
      return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
    } catch (JsonProcessingException e) {
      log.error(
          "Failed to convert object to pretty JSON string, object type: {}",
          obj.getClass().getName(),
          e);
      return null;
    }
  }

  // ==================== 反序列化方法 ====================

  /**
   * 将JSON字符串转换为指定类型的对象
   *
   * @param jsonString JSON字符串
   * @param valueType 目标类型
   * @param <T> 目标类型泛型
   * @return 转换后的对象，如果转换失败或参数为null则返回null
   */
  public static <T> T fromJson(String jsonString, Class<T> valueType) {
    if (StringUtils.isEmpty(jsonString)) {
      log.debug("JSON string is null or empty, returning null");
      return null;
    }
    if (valueType == null) {
      log.warn("Value type is null, returning null");
      return null;
    }
    try {
      return objectMapper.readValue(jsonString, valueType);
    } catch (IOException e) {
      log.error(
          "Failed to parse JSON string to object of type: {}, json: {}",
          valueType.getName(),
          jsonString,
          e);
      return null;
    }
  }

  /**
   * 将JSON字符串转换为指定类型的对象（支持复杂泛型）
   *
   * @param str JSON字符串
   * @param typeReference 类型引用，用于保留泛型信息
   * @param <T> 目标类型泛型
   * @return 转换后的对象，如果转换失败或参数为null则返回null
   */
  public static <T> T fromJson(String str, TypeReference<T> typeReference) {
    if (StringUtils.isEmpty(str)) {
      log.debug("JSON string is null or empty, returning null");
      return null;
    }
    if (typeReference == null) {
      log.warn("TypeReference is null, returning null");
      return null;
    }

    try {
      if (typeReference.getType().equals(String.class)) {
        return (T) str;
      }
      return objectMapper.readValue(str, typeReference);
    } catch (IOException e) {
      log.error(
          "Failed to parse JSON string to object with TypeReference: {}",
          typeReference.getType(),
          e);
      return null;
    }
  }

  /**
   * 将JSON字符串转换为参数化类型的对象（如List&lt;User&gt;、Map&lt;String, User&gt;等）
   *
   * @param str JSON字符串
   * @param collectionClass 集合类型（如List.class、Map.class）
   * @param elementClasses 元素类型
   * @param <T> 目标类型泛型
   * @return 转换后的对象，如果转换失败或参数为null则返回null
   */
  public static <T> T fromJson(String str, Class<?> collectionClass, Class<?>... elementClasses) {
    if (StringUtils.isEmpty(str)) {
      log.debug("JSON string is null or empty, returning null");
      return null;
    }
    if (collectionClass == null) {
      log.warn("Collection class is null, returning null");
      return null;
    }
    if (elementClasses == null || elementClasses.length == 0) {
      log.warn("Element classes is null or empty, returning null");
      return null;
    }

    JavaType javaType =
        objectMapper.getTypeFactory().constructParametricType(collectionClass, elementClasses);

    try {
      return objectMapper.readValue(str, javaType);
    } catch (IOException e) {
      log.error(
          "Failed to parse JSON string to parametric type: {}, json: {}",
          collectionClass.getName(),
          str,
          e);
      return null;
    }
  }

  /**
   * 将 InputStream 转换为参数化类型的对象（如List&lt;User&gt;、Map&lt;String, User&gt;等）
   *
   * @param inputStream InputStream
   * @param valueType 元素类型
   * @param <T> 目标类型泛型
   * @return 转换后的对象，如果转换失败或参数为null则返回null
   */
  public static <T> T fromJson(InputStream inputStream, Class<T> valueType) {
    if (inputStream == null) {
      log.warn("InputStream is null, returning null");
      return null;
    }
    if (valueType == null) {
      log.warn("Value type is null, returning null");
      return null;
    }
    try {
      return objectMapper.readValue(inputStream, valueType);
    } catch (IOException e) {
      log.error("Failed to parse InputStream to type: {}", valueType.getName(), e);
      return null;
    }
  }

  // ==================== Map转换方法 ====================

  /**
   * 将JSON字符串转换为Map&lt;String, Object&gt;
   *
   * @param jsonString JSON字符串
   * @return Map对象，如果字符串为空或转换失败则返回空Map
   */
  public static Map<String, Object> toMap(String jsonString) {
    return toJsonMap(jsonString);
  }

  /**
   * 将Map转换为指定类型的对象
   *
   * @param map Map对象
   * @param valueType 目标类型
   * @param <T> 目标类型泛型
   * @return 转换后的对象，如果参数为null或为空则返回null
   */
  public static <T> T mapToObject(Map<String, ?> map, Class<T> valueType) {
    if (MapUtils.isEmpty(map) || valueType == null) {
      log.warn("Map or valueType is null/empty, returning null");
      return null;
    }
    String jsonString = toJson(map);
    if (jsonString == null) {
      log.warn("Failed to convert map to JSON string");
      return null;
    }
    return fromJson(jsonString, valueType);
  }

  /**
   * 将Map转换为参数化类型的对象
   *
   * @param map Map对象
   * @param collectionClass 集合类型
   * @param elementClasses 元素类型
   * @param <T> 目标类型泛型
   * @return 转换后的对象，如果参数为null或为空则返回null
   */
  public static <T> T mapToObject(
      Map<String, ?> map, Class<?> collectionClass, Class<?>... elementClasses) {
    if (MapUtils.isEmpty(map) || collectionClass == null) {
      log.warn("Map or collectionClass is null/empty, returning null");
      return null;
    }
    if (elementClasses == null || elementClasses.length == 0) {
      log.warn("Element classes is null or empty, returning null");
      return null;
    }
    String jsonString = toJson(map);
    if (jsonString == null) {
      log.warn("Failed to convert map to JSON string");
      return null;
    }
    return fromJson(jsonString, collectionClass, elementClasses);
  }

  /**
   * 将Map转换为指定JavaType的对象
   *
   * @param map Map对象
   * @param javaType Java类型
   * @param <T> 目标类型泛型
   * @return 转换后的对象，如果转换失败或参数为null则返回null
   */
  public static <T> T mapToObject(Map<String, ?> map, JavaType javaType) {
    if (MapUtils.isEmpty(map) || javaType == null) {
      log.warn("Map or javaType is null/empty, returning null");
      return null;
    }
    String jsonString = toJson(map);
    if (jsonString == null) {
      log.warn("Failed to convert map to JSON string");
      return null;
    }
    try {
      return objectMapper.readValue(jsonString, javaType);
    } catch (JsonProcessingException e) {
      log.error("Failed to parse map to object with JavaType: {}", javaType, e);
      return null;
    }
  }

  /**
   * 将JSON字符串转换为Map&lt;String, Object&gt;
   *
   * @param jsonString JSON字符串
   * @return Map对象，如果字符串为空或转换失败则返回空Map
   */
  public static Map<String, Object> toJsonMap(String jsonString) {
    if (StringUtils.isBlank(jsonString)) {
      return new HashMap<>();
    }
    try {
      return objectMapper.readValue(jsonString, new TypeReference<>() {});
    } catch (IOException e) {
      log.error("Failed to parse JSON string to Map<String, Object>", e);
      return new HashMap<>();
    }
  }

  /**
   * 将JSON字符串转换为Map&lt;String, String&gt;
   *
   * @param jsonString JSON字符串
   * @return Map对象，如果字符串为空或转换失败则返回空Map
   */
  public static Map<String, String> toStringMap(String jsonString) {
    if (StringUtils.isBlank(jsonString)) {
      return new HashMap<>();
    }
    try {
      return objectMapper.readValue(jsonString, new TypeReference<>() {});
    } catch (IOException e) {
      log.error("Failed to parse JSON string to Map<String, String>", e);
      return new HashMap<>();
    }
  }

  // ==================== 文件操作方法 ====================

  /**
   * 从JSON文件读取并转换为指定类型的对象
   *
   * @param file JSON文件
   * @param valueType 目标类型
   * @param <T> 目标类型泛型
   * @return 转换后的对象
   * @throws IllegalArgumentException 如果参数为null
   * @throws RuntimeException 如果读取文件失败
   */
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
    } catch (IOException e) {
      log.error("Failed to parse JSON file: {}", file.getAbsolutePath(), e);
      throw new RuntimeException("Failed to parse JSON file: " + file.getAbsolutePath(), e);
    }
  }

  /**
   * 将对象写入JSON文件
   *
   * @param file 目标文件
   * @param object 要写入的对象
   * @throws IllegalArgumentException 如果参数为null
   * @throws RuntimeException 如果写入文件失败
   */
  public static void toFile(File file, Object object) {
    if (file == null) {
      throw new IllegalArgumentException("File cannot be null");
    }
    if (object == null) {
      throw new IllegalArgumentException("Object cannot be null");
    }
    // 确保父目录存在
    File parentDir = file.getParentFile();
    if (parentDir != null && !parentDir.exists()) {
      if (!parentDir.mkdirs()) {
        throw new RuntimeException(
            "Failed to create parent directory: " + parentDir.getAbsolutePath());
      }
    }
    try {
      objectMapper.writeValue(file, object);
    } catch (IOException e) {
      log.error("Failed to write object to JSON file: {}", file.getAbsolutePath(), e);
      throw new RuntimeException(
          "Failed to write object to JSON file: " + file.getAbsolutePath(), e);
    }
  }

  // ==================== JSON节点操作方法 ====================

  /**
   * 从JSON字符串中获取指定路径下的子节点
   *
   * @param jsonString JSON字符串
   * @param path JSON路径（如"/data/user/name"）
   * @return JSON节点，如果获取失败或参数为null则返回null
   */
  public static JsonNode getSubNode(String jsonString, String path) {
    if (StringUtils.isEmpty(jsonString)) {
      log.debug("JSON string is null or empty, returning null");
      return null;
    }
    if (StringUtils.isEmpty(path)) {
      log.warn("Path is null or empty, returning null");
      return null;
    }
    try {
      JsonNode rootNode = objectMapper.readTree(jsonString);
      return rootNode.at(path);
    } catch (JsonProcessingException e) {
      log.error("Failed to get sub node from JSON string at path: {}", path, e);
      return null;
    }
  }

  /**
   * 将JSON字符串解析为JsonNode树
   *
   * @param jsonString JSON字符串
   * @return JsonNode对象，如果解析失败或参数为null则返回null
   */
  public static JsonNode toJsonNodeTree(String jsonString) {
    if (StringUtils.isEmpty(jsonString)) {
      log.debug("JSON string is null or empty, returning null");
      return null;
    }
    try {
      return objectMapper.readTree(jsonString);
    } catch (JsonProcessingException e) {
      log.error("Failed to parse JSON string to JsonNode tree, json: {}", jsonString, e);
      return null;
    }
  }

  /**
   * 创建一个空的JSON对象节点
   *
   * @return ObjectNode对象
   */
  public static ObjectNode createEmptyJsonObject() {
    return objectMapper.createObjectNode();
  }

  /**
   * 创建一个空的JSON数组节点
   *
   * @return ArrayNode对象
   */
  public static ArrayNode createEmptyJsonArray() {
    return objectMapper.createArrayNode();
  }

  /**
   * 向JSON数组节点中添加元素
   *
   * @param arrayNode JSON数组节点
   * @param element 要添加的元素
   * @throws IllegalArgumentException 如果arrayNode为null
   */
  public static void addElementToJsonArray(ArrayNode arrayNode, Object element) {
    if (arrayNode == null) {
      throw new IllegalArgumentException("ArrayNode cannot be null");
    }
    if (element == null) {
      log.debug("Element is null, skipping add operation");
      return;
    }
    try {
      JsonNode jsonNode = objectMapper.valueToTree(element);
      arrayNode.add(jsonNode);
    } catch (IllegalArgumentException e) {
      log.error(
          "Failed to add element to JSON array, element type: {}", element.getClass().getName(), e);
    }
  }

  /**
   * 将JSON数组节点转换为指定类型的对象列表
   *
   * @param arrayNode JSON数组节点
   * @param valueType 目标元素类型
   * @param <T> 目标类型泛型
   * @return 对象列表，如果节点为null或valueType为null则返回空列表
   */
  public static <T> List<T> fromJson(ArrayNode arrayNode, Class<T> valueType) {
    List<T> objectList = new ArrayList<>();
    if (arrayNode == null) {
      log.debug("ArrayNode is null, returning empty list");
      return objectList;
    }
    if (valueType == null) {
      log.warn("Value type is null, returning empty list");
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
    } catch (JsonProcessingException e) {
      return new byte[0];
    }
  }

  public static <T> T fromBytes(byte[] bytes, TypeReference<T> typeRef) {
    try {
      return objectMapper.readValue(bytes, typeRef);
    } catch (IOException e) {
      return null;
    }
  }

  public static boolean isValidJson(byte[] bytes) {
    try {
      // readTree 会自动处理字节流并检测编码（UTF-8, UTF-16 等）
      objectMapper.readTree(bytes);
      return true;
    } catch (IOException e) {
      return false;
    }
  }

  public static boolean isValidJson(String jsonString) {
    try {
      objectMapper.readTree(jsonString);
      return true;
    } catch (JsonProcessingException e) {
      return false;
    }
  }
}
