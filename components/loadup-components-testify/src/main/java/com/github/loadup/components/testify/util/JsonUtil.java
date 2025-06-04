/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.testify.util;

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

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.github.loadup.components.testify.log.TestifyLogUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * 用于处理Json
 *
 *
 *
 */
@Slf4j
public class JsonUtil {

    /**
     * 将对象转化为json字符串，并转换为prettyFormat类型便于阅读, 并解决异常
     *
     * @param object
     * @return
     */
    public static String toPrettyString(Object object) {
        try {
            return JSON.toJSONString(object);
        } catch (Exception e) {
            TestifyLogUtil.warn(log, "对象无法转换为json");
            TestifyLogUtil.debug(log, LogUtil.getErrorMessage(e));
            return "{}";
        }
    }

    /**
     * 将对象打印到json文件
     */
    public static void writeObjectToFile(File file, Object object) {
        String jsString = JSON.toJSONString(object);
        FileUtil.writeFile(file, jsString, -1);
    }

    /**
     * 基于json生成对象，主要用于生成请求
     *
     * @param jsonRelativePath
     * @param clazz
     * @return
     */
    public static <T> T genObjectFromJsonFile(String jsonRelativePath, Class<T> clazz) {
        String jsonFullPath = FileUtil.getRelativePath(jsonRelativePath, null);
        String jsonString = FileUtil.readFile(jsonFullPath);
        T testObject = JSON.parseObject(jsonString, clazz);
        return testObject;
    }

    /**
     * 基于json字符串生成对象
     *
     * @param jsonString
     * @param clazz
     * @return
     */
    public static <T> T genObjectFromJsonString(String jsonString, Class<T> clazz) {
        return JSON.parseObject(jsonString, clazz);
    }

    public static String toJSONString(Object input) {
        if (null == input) {
            return "";
        }
        if (input instanceof String) {
            return (String) input;
        }

        return JSON.toJSONString(input);
    }

    public static <T> T stringToObject(String json, Class<T> clazz) {
        if (StringUtils.isBlank(json)) {
            return null;
        }

        return JSON.parseObject(json, clazz);
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
        try {
            return JSONObject.toJSONString(object);
        } catch (Error error) {
            // in case of unsupported methods by lower version of fastjson
            return JSONObject.toJSONString(object);
        }
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
}
