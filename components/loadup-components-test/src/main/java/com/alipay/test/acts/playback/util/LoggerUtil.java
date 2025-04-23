/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2019 All Rights Reserved.
 */
package com.alipay.test.acts.playback.util;

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

import org.slf4j.Logger;

import java.text.MessageFormat;

/**
 *
 * @author qingqin
 * @version $Id: LoggerUtil.java, v 0.1 2019年07月30日 下午10:17 qingqin Exp $
 */
public class LoggerUtil {

    public static void info(Logger logger, String msg) {
        if (logger.isInfoEnabled()) {
            logger.info(msg);
        }
    }

    public static void warn(Logger logger, String msg) {
        if (logger != null && logger.isWarnEnabled()) {
            logger.warn(msg);
        }
    }

    public static void error(Logger logger, String msg) {
        if (logger != null) {
            logger.error(msg);
        }
    }

    public static void error(Logger logger, String msg, Throwable t) {
        if (logger != null) {
            logger.error(msg, t);
        }
    }

    public static void error(Logger logger, Throwable t, Object... obj) {
        if (logger != null) {
            logger.error(getLogString(obj), t);
        }
    }

    /**
     * 生成输出到日志的字符串
     *
     * @param obj 任意个要输出到日志的参数
     * @return
     */
    private static String getLogString(Object... obj) {
        StringBuilder log = new StringBuilder();

        for (Object o : obj) {
            log.append(o);
        }

        return log.toString();
    }

    /**
     * 格式化message
     *
     * @param template
     * @param parameters
     * @return
     */
    private static String getMessage(String template, Object... parameters) {
        if (null == parameters || parameters.length == 0) {
            return template;
        }

        return MessageFormat.format(template, parameters);
    }

    /**
     * <p>生成调试级别日志</p>
     * <p>
     * 根据带参数的日志模板和参数集合，生成debug级别日志 带参数的日志模板中以{数字}表示待替换为变量的日志点，如a={0}，表示用参数集合中index为0的参数替换{0}处
     * </p>
     *
     * @param logger     logger引用
     * @param template   带参数的日志模板
     * @param parameters 参数集合
     */
    public static void debug(Logger logger, String template, Object... parameters) {
        if (logger.isDebugEnabled()) {
            logger.debug(getMessage(template, parameters));
        }
    }

    /**
     * <p>生成通知级别日志</p>
     * <p>
     * 根据带参数的日志模板和参数集合，生成info级别日志 带参数的日志模板中以{数字}表示待替换为变量的日志点，如a={0}，表示用参数集合中index为0的参数替换{0}处
     * </p>
     *
     * @param logger     logger引用
     * @param template   带参数的日志模板
     * @param parameters 参数集合
     */
    public static void info(Logger logger, String template, Object... parameters) {
        if (logger.isInfoEnabled()) {
            logger.info(getMessage(template, parameters));
        }
    }

    /**
     * <p>生成警告级别日志</p>
     * <p>
     * 根据带参数的日志模板和参数集合，生成warn级别日志 带参数的日志模板中以{数字}表示待替换为变量的日志点，如a={0}，表示用参数集合中index为0的参数替换{0}处
     * </p>
     *
     * @param logger     logger引用
     * @param template   带参数的日志模板
     * @param parameters 参数集合
     */
    public static void warn(Logger logger, String template, Object... parameters) {
        if (logger.isWarnEnabled()) {
            logger.warn(getMessage(template, parameters));
        }
    }

    /**
     * <p>生成警告级别日志</p>
     * <p>带异常堆栈</p>
     *
     * @param e          异常堆栈
     * @param logger     logger引用
     * @param template   带参数的日志模板
     * @param parameters 参数集合
     */
    public static void warn(Throwable e, Logger logger, String template, Object... parameters) {
        if (logger.isWarnEnabled()) {
            logger.warn(getMessage(template, parameters), e);
        }
    }

    /**
     * * <p>生成错误级别日志</p>
     * <p>
     * 根据带参数的日志模板和参数集合，生成error级别日志 带参数的日志模板中以{数字}表示待替换为变量的日志点，如a={0}，表示用参数集合中index为0的参数替换{0}处
     * </p>
     *
     * @param logger     logger引用
     * @param template   带参数的日志模板
     * @param parameters 参数集合
     */
    public static void error(Logger logger, String template, Object... parameters) {
        if (logger.isErrorEnabled()) {
            logger.error(getMessage(template, parameters));
        }
    }

    /**
     * <p>生成错误级别日志</p>
     * <p>带异常堆栈</p>
     *
     * @param e          异常堆栈
     * @param logger     logger引用
     * @param template   带参数的日志模板
     * @param parameters 参数集合
     */
    public static void error(Throwable e, Logger logger, String template, Object... parameters) {
        if (logger.isErrorEnabled()) {
            logger.error(getMessage(template, parameters), e);
        }
    }


}
