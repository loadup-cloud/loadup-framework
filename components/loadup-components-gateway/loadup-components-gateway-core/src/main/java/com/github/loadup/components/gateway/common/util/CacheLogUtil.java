package com.github.loadup.components.gateway.common.util;

import com.github.loadup.components.gateway.facade.util.LogUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 *
 */
public class CacheLogUtil {

    /**
     * logger
     */
    private static final Logger logger = LoggerFactory.getLogger("DIGEST-CACHE-LOGGER");

    /**
     * Print log
     */
    public static <K, V> void printLog(String cacheName, Map<K, V> cache) {

        try {
            LogUtil.info(logger, cacheName + ":\n" + cache);
        } catch (Exception e) {
            LogUtil.error(logger, e, "");
        }
    }

    /**
     * Print log
     */
    public static void printLog(String cacheName, List<?> cache) {

        try {
            LogUtil.info(logger, cacheName + ":\n" + cache);
        } catch (Exception e) {
            LogUtil.error(logger, e, "");
        }
    }
}