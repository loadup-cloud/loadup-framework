package com.github.loadup.components.gateway.core.prototype.util;

/*-
 * #%L
 * loadup-components-gateway-core
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
    private static final Logger logger = LoggerFactory.getLogger("COMMON-ERROR-LOGGER");

    /**
     * 捕捉错误日志并输出到日志文件：common-error.log
     */
    public static void caught(Throwable e, Object... message) {
        LogUtil.error(logger, e, message);
    }
}
