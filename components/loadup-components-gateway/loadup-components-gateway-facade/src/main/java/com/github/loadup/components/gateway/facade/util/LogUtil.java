package com.github.loadup.components.gateway.facade.util;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;

import java.util.Date;

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

    private LogUtil() {
    }

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