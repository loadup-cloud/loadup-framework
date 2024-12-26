package com.github.loadup.components.gateway.core.prototype.util;

import com.github.loadup.components.gateway.common.exception.ErrorCode;
import com.github.loadup.components.gateway.core.ctrl.context.GatewayRuntimeProcessContext;
import com.github.loadup.components.gateway.facade.util.LogUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>:DigestLoggerUtil.java
 */
public class DigestLoggerUtil {
    /**
     * message digest
     */
    public static final Logger digestMessageLogger = LoggerFactory
            .getLogger("DIGEST-MESSAGE-LOGGER");

    /**
     * http digest
     */
    public static final Logger DIGEST_HTTP_LOGGER = LoggerFactory
            .getLogger("DIGEST-HTTP-LOGGER");

    /**
     * count error logger
     */
    public static final Logger COUNT_ERROR_LOGGER = LoggerFactory
            .getLogger("COUNT-ERROR-LOGGER");

    /**
     * limit digest logger
     */
    public static final Logger digestLimitLogger = LoggerFactory
            .getLogger("DIGEST-LIMIT-LOGGER");

    /**
     * 通用日志打印方法，屏蔽敏感日志
     */
    public static void printInfoLog(GatewayRuntimeProcessContext processContext, long timeCost) {

    }

    /**
     * 通用日志打印方法, 打印调用方耗时信息
     */
    public static void printSimpleDigestLog(String url, String respMsg, long timeCost) {
        LogUtil.info(DIGEST_HTTP_LOGGER, url, ", ", respMsg, ", ", timeCost);
    }

    /**
     * print digest limit log
     */
    public static void printLimitDigestLog(String entityId, String maxLimit, ErrorCode errorCode) {
        LogUtil.info(digestLimitLogger, entityId, ", ", maxLimit, ", ", errorCode);
    }

}