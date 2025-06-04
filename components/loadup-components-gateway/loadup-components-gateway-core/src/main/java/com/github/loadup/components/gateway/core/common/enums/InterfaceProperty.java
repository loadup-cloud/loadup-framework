/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.gateway.core.common.enums;

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

/**
 * 接口属性
 */
public enum InterfaceProperty implements PropertyName {
    /**
     * 鉴权类型:BASE,OAUTH_CLIENT,OAUTH_TOKEN
     */
    AUTH_TYPE("AUTH_TYPE", "interface auth type", "BASE"),
    /**
     * 是否忽略鉴权
     */
    AUTH_IGNORED("AUTH_IGNORED", "is ignored interface auth", "FALSE"),
    /**
     * 是否需要幂等校验
     */
    CHECK_UNIQUE("CHECK_UNIQUE", "is need to check unique", "FALSE"),
    /**
     * 调用内部系统超时时间ms  默认10s
     */
    INNER_TIMEOUT("INNER_TIMEOUT", "invoke inner system timeout", "10000"),
    /**
     * 压测交易是否发出,默认false
     */
    NEED_LOAD_DISPATCH("NEED_LOAD_DISPATCH", "need load test dispatch", "false"),
    /**
     * 压测mock的sleep时间ms 默认300ms
     */
    LOAD_TEST_SLEEP_TIME("LOAD_TEST_SLEEP_TIME", "load test sleep time", "300"),
    /**
     * 压测交易使用原交易的报文处理配置
     */
    LOAD_TEST_MSG_PROC_REUSE("LOAD_TEST_MSG_PROC_REUSE", "use original interface msgproc", "false"),
    /**
     * 压测用于替换的interfaceId
     */
    LOAD_TEST_INTERFACE_ID("LOAD_TEST_INTERFACE_ID", "load test interface id", ""),
    /**
     * 异转同首次查询时间 ms 默认500ms
     */
    FIRST_QUERY_TIME("FIRST_QUERY_TIME", "first query time", "500"),
    /**
     * 异转同查询间隔时间ms 默认500ms
     */
    CHECK_INTERVAL("CHECK_INTERVAL", "CHECK INTERVAL", "500"),
    /**
     * 是否支持动态url
     */
    IS_DYNAMIC_URL("IS_DYNAMIC_URL", "is dynamic url", "false"),
    /**
     * type of invoke inner system
     */
    INNER_TYPE("innerType", "inner type", "tr"),
    /**
     * generic facade
     */
    FACADE("facade", "generic facade", ""),
    /**
     * generic method
     */
    METHOD("method", "generic method", "");

    private final String name;

    private final String description;

    private final String defaultValue;

    private InterfaceProperty(String name, String description, String defaultValue) {
        this.name = name;
        this.description = description;
        this.defaultValue = defaultValue;
    }

    @Override
    public String getDefaultValue() {
        return defaultValue;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getName() {
        return name;
    }
}
