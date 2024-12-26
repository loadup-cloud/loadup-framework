package com.github.loadup.components.gateway.core.prototype.util;

import com.github.loadup.components.gateway.facade.util.LogUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 捕捉到异常的时候，我们通常会使用<code>LogUtil.error(logger, "xxxx",e)</code>方式打印日常堆栈日志<br>
 * 但是这种方式会造成错误日志打印两遍，精益求精，日志也追求极致，SUPERGW的错误日志全部使用本工具类输出
 */
public final class ExceptionUtil {

    /**
     * logger
     */
    private static final Logger logger = LoggerFactory
            .getLogger("COMMON-ERROR-LOGGER");

    /**
     * 捕捉错误日志并输出到日志文件：common-error.log
     */
    public static void caught(Throwable e, Object... message) {
        LogUtil.error(logger, e, message);
    }

}
