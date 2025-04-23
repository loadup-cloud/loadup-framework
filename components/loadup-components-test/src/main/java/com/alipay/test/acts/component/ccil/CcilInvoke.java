/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.alipay.test.acts.component.ccil;

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

import java.util.List;

import com.alipay.test.acts.component.enums.RepositoryType;

/**
 * 组件调用表达式。
 * 
 * @author dasong.jds
 * @version $Id: CcilInvoke.java, v 0.1 2015年5月25日 下午3:16:09 dasong.jds Exp $
 */
public class CcilInvoke implements CcilExpr {

    /** 组件类型 */
    private RepositoryType        repositoryType;

    /** 组件ID */
    private String                componentId;

    /** 组件调用参数 */
    private List<CcilInvokeParam> params;

    /**
     * 获取组件类型。
     * 
     * @return
     */
    public RepositoryType getRepositoryType() {
        return repositoryType;
    }

    /**
     * 获取组件ID。
     * 
     * @return
     */
    public String getComponentId() {
        return componentId;
    }

    /**
     * 获取参数信息。
     * 
     * @return
     */
    public List<CcilInvokeParam> getParams() {
        return params;
    }

    /**
     * Setter method for property <tt>repositoryType</tt>.
     * 
     * @param repositoryType value to be assigned to property repositoryType
     */
    public void setRepositoryType(RepositoryType repositoryType) {
        this.repositoryType = repositoryType;
    }

    /**
     * Setter method for property <tt>componentId</tt>.
     * 
     * @param componentId value to be assigned to property componentId
     */
    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }

    /**
     * Setter method for property <tt>params</tt>.
     * 
     * @param params value to be assigned to property params
     */
    public void setParams(List<CcilInvokeParam> params) {
        this.params = params;
    }

    /** 
     * @see Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("CcilInvoke[repositoryType=");
        builder.append(repositoryType);
        builder.append(",componentId=");
        builder.append(componentId);
        builder.append(",params=");
        builder.append(params);
        builder.append("]");
        return builder.toString();
    }

}
