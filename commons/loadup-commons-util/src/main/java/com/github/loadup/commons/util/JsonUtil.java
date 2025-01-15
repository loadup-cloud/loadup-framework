package com.github.loadup.commons.util;

/*-
 * #%L
 * loadup-commons-util
 * %%
 * Copyright (C) 2022 - 2024 loadup_cloud
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
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

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class JsonUtil {

	private static final ObjectMapper objectMapper = createObjectMapper();

	private static ObjectMapper createObjectMapper() {
		ObjectMapper mapper = new ObjectMapper();
		//忽略 在json字符串中存在，但是在java对象中不存在对应属性的情况。防止错误
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		//忽略空Bean转json的错误
		mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
		//取消默认转换timestamps形式
		mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		//对象的所有字段全部列入
		mapper.setSerializationInclusion(Include.ALWAYS);
		//所有的日期格式都统一为以下的样式，即yyyy-MM-dd HH:mm:ss
		mapper.setDateFormat(new SimpleDateFormat(CommonConstants.DEFAULT_DATE_FORMAT));

		// 注册 JavaTimeModule 处理 java.time 包的类
		JavaTimeModule javaTimeModule = new JavaTimeModule();
		javaTimeModule.addSerializer(LocalDate.class,
				new LocalDateSerializer(DateTimeFormatter.ofPattern(CommonConstants.DEFAULT_DATE_FORMAT)));
		javaTimeModule.addDeserializer(LocalDate.class,
				new LocalDateDeserializer(DateTimeFormatter.ofPattern(CommonConstants.DEFAULT_DATE_FORMAT)));
		javaTimeModule.addSerializer(LocalDateTime.class,
				new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(CommonConstants.DEFAULT_DATE_TIME_FORMAT)));
		javaTimeModule.addDeserializer(LocalDateTime.class,
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
			return obj instanceof String ? (String) obj : objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
		} catch (IOException e) {
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
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static <T> T parseObject(String str, TypeReference<T> typeReference) {

		if (StringUtils.isEmpty(str) || typeReference == null) {
			return null;
		}

		try {
			return (T) (typeReference.getType().equals(String.class) ? str : objectMapper.readValue(str, typeReference));
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

	}

	public static <T> T parseObject(String str, Class<?> collectionClass, Class<?>... elementClasses) {

		JavaType javaType = objectMapper.getTypeFactory().constructParametricType(collectionClass, elementClasses);

		try {
			return objectMapper.readValue(str, javaType);
		} catch (IOException e) {
			e.printStackTrace();
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
		} catch (IOException e) {
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
		} catch (IOException e) {
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
		} catch (IOException e) {
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
		} catch (IOException e) {
			e.printStackTrace();
			return new HashMap<>();
		}
	}
}
