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

import com.github.loadup.components.testify.constant.VelocityExtendProperties;
import com.github.loadup.components.testify.exception.TestifyException;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

/**
 * velocity统一工具类。
 *
 *
 *
 */
public class VelocityUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(VelocityUtil.class);
    /**
     * 文件解析编码，默认为UTF-8
     */
    private static String charset = "UTF-8";

    /**
     * 渲染文本模版。
     *
     * @param context
     * @param template
     * @return
     */
    public static synchronized String evaluateString(Map<String, Object> context, String template) {
        Writer writer = new StringWriter();
        try {
            VelocityContext velocityContext = new VelocityContext(context);

            addExtendProperties(velocityContext);

            Velocity.evaluate(velocityContext, writer, StringUtils.EMPTY, template);
            return writer.toString();
        } catch (Exception e) {
            LOGGER.error("velocity evaluate error[template=" + template + "]", e);
            return template;
        } finally {
            IOUtils.closeQuietly(writer);
        }
    }

    /**
     * 渲染文本模版。
     *
     * @param context
     * @param templateList
     * @return
     */
    public static synchronized List<String> evaluateStringList(Map<String, Object> context, List<String> templateList) {
        List<String> result = new ArrayList<String>();
        if (CollectionUtils.isEmpty(templateList)) {
            return result;
        }
        for (String template : templateList) {
            String replace = evaluateString(context, template);
            if (replace.indexOf("$") == -1) { // 说明没有需要替换成功
                result.add(evaluateString(context, template));
            } else {
                LOGGER.error("Unable to resolve variables in " + template);
            }
        }
        return result;
    }

    /**
     * 渲染模版文件。
     *
     * @param context
     * @param templateFile
     * @return
     */
    public static String evaluateFile(Map<String, Object> context, String templateFile) {
        return evaluateFile(context, new File(templateFile));
    }

    /**
     * 渲染模版文件。
     *
     * @param context
     * @param templateFile
     * @return
     */
    public static String evaluateFile(Map<String, Object> context, File templateFile) {
        Writer writer = new StringWriter();
        BufferedReader reader = null;
        try {
            VelocityContext velocityContext = new VelocityContext(context);

            addExtendProperties(velocityContext);

            reader = new BufferedReader(new InputStreamReader(new FileInputStream(templateFile), charset));

            Velocity.evaluate(velocityContext, writer, StringUtils.EMPTY, reader);
            return writer.toString();
        } catch (Exception e) {
            throw new TestifyException("velocity evaluate error[templateFile=" + templateFile + "]", e);
        } finally {
            IOUtils.closeQuietly(writer);
            IOUtils.closeQuietly(reader);
        }
    }

    /**
     * 添加扩展参数。
     *
     * @param context
     */
    private static void addExtendProperties(VelocityContext context) {
        // 默认参数
        context.put(VelocityExtendProperties.CUR_TIME_MILLIS_STR, System.currentTimeMillis());
        context.put(VelocityExtendProperties.CUR_DATE, DateUtil.getWebTodayString());
        context.put(VelocityExtendProperties.CUR_DATE_TIME, DateUtil.simpleFormat(new Date()));
        context.put(VelocityExtendProperties.CUR_YEAR, DateUtil.getCurYear());
        context.put(VelocityExtendProperties.CUR_VER, BaseDataUtil.getCurVer());
    }
}
