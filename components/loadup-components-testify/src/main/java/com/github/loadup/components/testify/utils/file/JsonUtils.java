
package com.github.loadup.components.testify.utils.file;

/*-
 * #%L
 * loadup-components-test
 * %%
 * Copyright (C) 2022 - 2025 loadup_cloud
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

import org.apache.commons.lang3.StringUtils;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * JSON工具类
 *
 * 
 *
 */
public class JsonUtils {

    /**
     * 解析json字符串
     *
     * @param str
     * @param classType
     * @return
     */
    public static <T> T parse(String str, Class<T> classType) {
        if (StringUtils.isBlank(str)) {
            return null;
        }
        return JSONObject.parseObject(str, classType);
    }

    /**
     * 转换json格式字符串到Map<String, Object>
     *
     * @param jsonStr json格式的字符串
     * @return 参数为空返回new HashMap<String, Object>()
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> toMap(String jsonStr) {
        if (StringUtils.isBlank(jsonStr)) {
            return null;
        }
        return JSONObject.parseObject(jsonStr, HashMap.class);
    }

    /**
     * 转换json格式字符串到List<Map<String, Object>>
     *
     * @param jsonStr json格式的字符串
     * @return 参数为空返回null
     */
    public static List<Map<String, Object>> toListMap(String jsonStr) {
        if (StringUtils.isBlank(jsonStr)) {
            return null;
        }
        return JSONObject.parseObject(jsonStr, new TypeReference<List<Map<String, Object>>>() {
        });
    }

    /**
     * 转换json格式字符串到List<Map<String, Object>>
     *
     * @param jsonStr json格式的字符串
     * @return 参数为空返回null
     */
    public static List<Map<String, String>> toListMapStr(String jsonStr) {
        if (StringUtils.isBlank(jsonStr)) {
            return null;
        }
        return JSONObject.parseObject(jsonStr, new TypeReference<List<Map<String, String>>>() {
        });
    }

    /**
     * 转换json格式字符串到Map<String, String>
     *
     * @param jsonStr json格式的字符串
     * @return 参数为空返回new HashMap<String, String>()
     */
    @SuppressWarnings("unchecked")
    public static Map<String, String> toMapStr(String jsonStr) {
        if (StringUtils.isBlank(jsonStr)) {
            return null;
        }
        return JSONObject.parseObject(jsonStr, HashMap.class);
    }

    /**
     * 解析成json数组
     *
     * @param json
     * @return
     */
    public static JSONArray toJsonArray(String json) {
        if (json == null) {
            return null;
        }
        return JSONArray.parseArray(json);
    }

    /**
     * 将对象变成字符串
     *
     * @param object
     * @return
     */
    public static String toJson(Object object) {
        if (object == null) {
            return null;
        }
        if (object instanceof String) {
            return (String) object;
        }

        return JSONObject.toJSONString(object);
//        return JSONObject.toJSONString(object, new SerializeConfig(true),
//                SerializerFeature.IgnoreErrorGetter, SerializerFeature.WriteNonStringKeyAsString,
//                SerializerFeature.DisableCircularReferenceDetect);
    }

    /**
     * 将对象变成字符串
     *
     * @param object
     * @return
     */
    public static String toJsonNotNull(Object object) {
        if (object == null) {
            return null;
        }

        return JSONObject.toJSONString(object);
//        return JSONObject.toJSONString(object, SerializerFeature.IgnoreErrorGetter,
//                SerializerFeature.WriteNonStringKeyAsString, SerializerFeature.WriteMapNullValue,
//                SerializerFeature.WriteNullStringAsEmpty, SerializerFeature.WriteNullNumberAsZero,
//                SerializerFeature.WriteNullListAsEmpty, SerializerFeature.WriteNullBooleanAsFalse);
    }

    /**
     * 对象转换
     *
     * @return
     */
    public static Object parseObject(String json,
                                     TypeReference<Map<String, String[]>> typeReference) {
        if (StringUtils.isBlank(json)) {
            return null;
        }
        if (json.startsWith("[")) {
            return JSONArray.parseArray(json);
        }
        return JSONObject.parseObject(json, typeReference);
    }

    /**
     * 对象转换
     *
     * @return
     */
    public static Object parse(String json, TypeReference typeReference) {
        if (StringUtils.isBlank(json)) {
            return null;
        }
        return JSONObject.parseObject(json, typeReference);
    }

    /**
     * 对象转换
     *
     * @return
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Object parseObject(String json) {
        if (StringUtils.isBlank(json)) {
            return null;
        }
        return JSONObject.parseObject(json);

//        return JSON.parseObject(json, Feature.IgnoreNotMatch, Feature.UseObjectArray);
    }

    /**
     * 对象转换
     *
     * @return
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Object parseObject(String json, Class cls) {
        if (StringUtils.isBlank(json)) {
            return null;
        }
        return JSONObject.parseObject(json);

//        return JSON.parseObject(json, cls, Feature.IgnoreNotMatch, Feature.UseObjectArray);
    }

    /**
     * 将json字符串转换成jsonlist
     *
     * @param <T>
     * @return
     */
    public static <T> List<T> toList(String str, Class<T> cls) {
        if (StringUtils.isBlank(str)) {
            return new ArrayList<T>();
        }
        return JSONArray.parseArray(str, cls);
    }

    /**
     * 将json字符串转换成jsonlist
     *
     * @return
     */
    public static List<String> toListString(String str) {
        if (StringUtils.isBlank(str)) {
            return new ArrayList<String>();
        }
        return JSONArray.parseArray(str, String.class);
    }

    /**
     * json转换set
     *
     * @param json
     * @param cls
     * @return
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Set<?> parseJsonSet(String json, Class cls) {
        List list = toList(json, cls);
        return new HashSet(list);
    }

    /**
     * 判断是否是json格式
     *
     * @param string
     * @return
     */
    public static boolean isJsonObject(String string) {
        try {
            JSONObject.parseObject(string);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 判断是否是json格式
     *
     * @param string
     * @return
     */
    public static boolean isJsonArray(String string) {
        try {
//            JSONObject.parseArray(string);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
