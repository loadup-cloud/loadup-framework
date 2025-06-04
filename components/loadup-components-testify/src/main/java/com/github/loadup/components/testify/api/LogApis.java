/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.testify.api;

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

import com.github.loadup.components.testify.log.TestifyLogUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 日志操作入口API，若无日志模型，则只打印至console
 *
 *
 *
 */
@Slf4j
public class LogApis {

    /**
     * 添加debug日志入console和用例线程日志
     *
     * @param message
     */
    public static void debug(String message) {
        TestifyLogUtil.debug(log, message);
    }

    /**
     * 添加info日志入console和用例线程日志
     *
     * @param message
     */
    public static void info(String message) {
        TestifyLogUtil.info(log, message);
    }

    /**
     * 添加warn日志入console和用例线程日志
     *
     * @param message
     */
    public static void warn(String message) {
        TestifyLogUtil.warn(log, message);
    }

    /**
     * 添加error日志入console和用例线程日志
     *
     * @param message
     */
    public static void error(String message) {
        TestifyLogUtil.error(log, message);
    }

    /**
     * 添加error日志和堆栈信息入console和用例线程日志
     *
     * @param message
     */
    public static void error(String message, Throwable e) {
        TestifyLogUtil.error(log, message, e);
    }

    /**
     * 添加error日志入console和用例线程日志，并以junit Assert抛出异常
     *
     * @param message
     */
    public static void fail(String message) {
        TestifyLogUtil.fail(log, message);
    }

    /**
     * 添加debug日志和堆栈信息入console和用例线程日志，并以junit Assert异常抛出日志和堆栈信息
     *
     * @param message
     */
    public static void fail(String message, Throwable e) {
        TestifyLogUtil.fail(log, message, e);
    }

    /**
     * 添加对象日志以json格式入用例线程日志（不打印到console）
     *
     * @param message
     */
    public static void addTestLog(Object message) {
        TestifyLogUtil.addProcessLog(message);
    }
}
