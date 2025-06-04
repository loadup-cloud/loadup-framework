/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.gateway.facade.util;

/*-
 * #%L
 * loadup-components-gateway-facade
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

import java.util.Date;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;

/**
 *
 */
public class LogUtil {

    /**
     * 线程编号修饰符
     */
    private static final char THREAD_RIGHT_TAG = ']';

    /**
     * 线程编号修饰符
     */
    private static final char THREAD_LEFT_TAG = '[';

    private LogUtil() {}

    public static void info(Logger logger, Object... obj) {
        if (logger.isInfoEnabled()) {
            logger.info(getLogString(obj));
        }
    }

    public static void debug(Logger logger, Object... obj) {
        if (logger.isDebugEnabled()) {
            logger.debug(getLogString(obj));
        }
    }

    public static void warn(Logger logger, Object... obj) {
        if (logger.isWarnEnabled()) {
            logger.warn(getLogString(obj));
        }
    }

    public static void warn(Logger logger, String message, Throwable e) {
        if (logger.isWarnEnabled()) {
            logger.warn(message, e);
        }
    }

    public static void warn(Logger logger, Throwable t, Object... obj) {
        if (logger.isWarnEnabled()) {
            logger.warn(getLogString(obj), t);
        }
    }

    public static void error(Logger logger, Object... obj) {

        logger.error(getLogString(obj));
    }

    public static void error(Logger logger, Throwable t, Object... obj) {

        logger.error(getLogString(obj), t);
    }

    public static String getLogString(Object... obj) {

        StringBuilder log = new StringBuilder();

        log.append(DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss.S") + "  ");

        log.append(THREAD_LEFT_TAG + String.valueOf(Thread.currentThread().getId()));

        log.append(THREAD_RIGHT_TAG);

        for (Object o : obj) {
            log.append(o);
        }

        return log.toString();
    }
}
