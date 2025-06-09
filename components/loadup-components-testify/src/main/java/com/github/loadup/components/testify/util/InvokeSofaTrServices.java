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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 调用远程发布的TR服务工具类
 *
 * @author yangyu.yy
 *
 */
public class InvokeSofaTrServices {

    /**
     * Logger
     */
    protected static Logger logger = LoggerFactory.getLogger(InvokeSofaTrServices.class);

    /**
     *
     */
    @SuppressWarnings("rawtypes")
    public static Object invoke(String servicesUrl, Class service, String methodname, Object... parameters) {

        String targetURL = servicesUrl;

        if (StringUtils.isEmpty(targetURL) || null == service || StringUtils.isEmpty(methodname)) {
            logger.error("远程TR调用入参不合法", new IllegalArgumentException());
            return null;
        }

        return invokeTr(targetURL, service, methodname, parameters);
    }

    @SuppressWarnings("rawtypes")
    private static Object invokeTr(String serviceUrl, Class service, String method, Object... objects) {

        return null;
    }
}
