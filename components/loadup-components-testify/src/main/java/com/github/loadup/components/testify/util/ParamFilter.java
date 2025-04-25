
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

import com.github.loadup.components.testify.exception.TestifyException;
import com.github.vincentrussell.json.datagenerator.JsonDataGeneratorException;
import com.github.vincentrussell.json.datagenerator.impl.JsonDataGeneratorImpl;
import com.google.common.io.Closer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 参数过滤器
 *
 * @author yuli.qyl
 * 
 */
public class ParamFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ParamFilter.class);

    private static final Pattern pattern = Pattern.compile("\\{\\{(.*?)\\}\\}");
    private static Matcher matcher;

    /**
     * @param jsonText
     * @return
     */
    public static String filterJson(String jsonText) {

        // 参数校验
        if (!StringUtils.isBlank(jsonText)) {
            return jsonText;
        }

        // 生成随机数据
        String result;
        try {
            result = generateJsonData(jsonText);
        } catch (Exception e) {
            throw new TestifyException("尝试生成JSON数据异常", e);
        }

        return result;
    }

    /**
     * 替换字符串中的占位符为指定类型
     *
     * @param jsonText JSON文本
     * @return 返回
     * @throws IOException
     * @see <a href= "https://github.com/vincentrussell/json-data-generator">json-data-generator</>
     */
    private static String generateJsonData(String jsonText) throws IOException {

        // 统一资源关闭器
        Closer closer = Closer.create();
        // 字节输出流
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        closer.register(outputStream);
        // Json数据生成器
        JsonDataGeneratorImpl parser = new JsonDataGeneratorImpl();

        try {
            parser.generateTestDataJson(jsonText, outputStream);
        } catch (JsonDataGeneratorException e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }

        return outputStream.toString("UTF-8");
    }

    /**
     * 替换json字符串中占位符, json字符串中使用{{key}}
     *
     * @param jsonText
     * @param param
     * @return
     */
    public static String replaceWithMap(String jsonText, Map<String, Object> param) {
        if (StringUtils.isEmpty(jsonText) || CollectionUtils.isEmpty(param)) {
            return jsonText;
        }
        //这里可以先替换随机参数
        String resultString = filterJson(jsonText);
        matcher = pattern.matcher(jsonText);
        while (matcher.find()) {
            try {
                String key = matcher.group();
                String keyclone = key.substring(2, key.length() - 2).trim();
                Object value = param.get(keyclone);
                if (value != null && param.keySet().contains(keyclone)) {
                    resultString = resultString.replace(key, value.toString());
                }
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
            }
        }
        return resultString;
    }

}
