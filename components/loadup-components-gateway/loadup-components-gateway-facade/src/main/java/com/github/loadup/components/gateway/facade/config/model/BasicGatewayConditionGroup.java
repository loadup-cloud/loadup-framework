/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.gateway.facade.config.model;

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

import java.io.Serializable;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 网关通用产品基础配置条件组
 */
public class BasicGatewayConditionGroup implements Serializable {

    private static final long serialVersionUID = 4099359934469430937L;

    /**
     * keys use to be shielded
     */
    private String shieldKeys = "mobile,bankcard,id";

    /**
     * default result struct in gatewaylite response if result not exist
     */
    private Boolean needFillDefaultACResult = Boolean.TRUE;

    /**
     *
     */
    public String getShieldKeys() {
        return shieldKeys;
    }

    /**
     *
     */
    public void setShieldKeys(String shieldKeys) {
        this.shieldKeys = shieldKeys;
    }

    /**
     *
     */
    public Boolean getNeedFillDefaultACResult() {
        return needFillDefaultACResult;
    }

    /**
     *
     */
    public void setNeedFillDefaultACResult(Boolean needFillDefaultACResult) {
        this.needFillDefaultACResult = needFillDefaultACResult;
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
