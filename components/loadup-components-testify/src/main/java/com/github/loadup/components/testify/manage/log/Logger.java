/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.testify.manage.log;

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

import com.github.loadup.components.testify.support.TestCase;
import com.github.loadup.components.testify.support.TestCaseHolder;
import org.apache.commons.lang3.StringUtils;

/**
 * 日志便捷接口。
 *
 *
 *
 */
public abstract class Logger {

    /**
     * 摘要日志的内容分隔符
     */
    public static final String SEP = ",";

    /**
     * 修饰符
     */
    public static final char RIGHT_TAG = ']';

    /**
     * 修饰符
     */
    public static final char LEFT_TAG = '[';

    /**
     * 生成输出到日志的字符串
     * <p>输出格式:[sofaId][messageName]objs...
     *
     * @param messageName 报警日志的标题
     * @param objs        任意个要输出到日志的参数
     * @return 日志字符串
     */
    public static String getLogString(String messageName, Object... objs) {
        StringBuilder log = new StringBuilder();
        log.append(LEFT_TAG);
        log.append(fetchCaseId()).append(SEP);
        // 预留扩展位
        log.append(SEP).append(SEP).append(SEP).append(RIGHT_TAG);

        log.append(LEFT_TAG).append(messageName).append(RIGHT_TAG);

        if (objs != null) {
            for (Object o : objs) {
                log.append(o);
            }
        }

        return log.toString();
    }

    /**
     * 生成输出到日志的字符串
     *
     * @param objs 任意个要输出到日志的参数
     * @return 日志字符串
     */
    public static String getLogString(Object... objs) {
        StringBuilder log = new StringBuilder();
        log.append(LEFT_TAG);
        log.append(fetchCaseId()).append(SEP);
        // 预留扩展位
        log.append(SEP).append(SEP).append(SEP).append(RIGHT_TAG);

        if (objs != null) {
            for (Object o : objs) {
                log.append(o);
            }
        }
        return log.toString();
    }

    /**
     * 获取sofa上下文的调用id。
     *
     * @return 调用id
     */
    public static String fetchCaseId() {

        String caseId = null;
        TestCase desc = TestCaseHolder.get();
        if (desc != null) {
            caseId = desc.getCaseId();
        }
        return StringUtils.defaultIfBlank(caseId, "-");
    }

    /**
     * 打印info日志。
     *
     * @param objs 任意个要输出到日志的参数
     */
    public abstract void info(Object... objs);

    /**
     * 打印info日志。
     *
     * @param e    异常信息
     * @param objs 任意个要输出到日志的参数
     */
    public abstract void info(Throwable e, Object... objs);

    /**
     * 打印warn日志。
     *
     * @param objs 任意个要输出到日志的参数
     */
    public abstract void warn(Object... objs);

    /**
     * 打印warn日志。
     *
     * @param e    异常信息
     * @param objs 任意个要输出到日志的参数
     */
    public abstract void warn(Throwable e, Object... objs);

    /**
     * 打印error日志。
     *
     * @param e    异常信息
     * @param objs 任意个要输出到日志的参数
     */
    public abstract void error(Throwable e, Object... objs);

    /**
     * 打印error日志。
     *
     * @param objs 任意个要输出到日志的参数
     */
    public abstract void error(Object... objs);

    /**
     * 打印error日志。
     *
     * @param messageName 报警日志的标题
     * @param objs        任意个要输出到日志的参数
     */
    public abstract void error(String messageName, Object... objs);

    /**
     * 打印debug日志。
     *
     * @param objs 任意个要输出到日志的参数
     */
    public abstract void debug(Object... objs);

    /**
     * 打印debug日志。
     *
     * @param logger 日志对象
     * @param e      异常信息
     * @param objs   任意个要输出到日志的参数
     */
    public abstract void debug(Throwable e, Object... objs);
}
