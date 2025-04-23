/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2019 All Rights Reserved.
 */
package com.alipay.test.acts.playback.trservice.model;

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

/**
 * 声明一个 tr 服务所需要的基本描述
 *
 * @author qingqin
 * @version $Id: trservice.java, v 0.1 2019年07月31日 下午8:37 qingqin Exp $
 */
public class TrBeanDescriptor {

    public static final int TR = 0;
    public static final int WS = TR + 1;

    /**
     * @see com.alipay.sofa.rpc.api.binding.RpcBindingParam
     *
     * 不同子类的 RpcBindingParam 用于创建不同的类型的 rpc 服务
     *
     * 默认创建的 rpc 为 TR，当需要创建 ws 服务引用时，指定为 WS
     */
    private int bindingCode = TR;

    /**
     * tr service 引用时的 interface
     *
     * <sofa:reference interface="" unique-id=""/>
     */
    private Class interfaceType;

    /**
     * 接口的全限定名称
     */
    private String interfaceName;

    /**
     * tr service 发布时的 ref 的 bean id
     *
     * <sofa:service ref="" interface="" unique-id=""/>
     *
     * 这里太挫了，因为现在还没有找到依据 unique id 查询 service 的功能，暂时用 bean name 来使用
     */
    private String beanName;

    /**
     * tr service 发布时的 unique id 即
     *
     * <sofa:reference interface="" unique-id="" />
     */
    private String uniqueId;

    public Class getInterfaceType() {
        return interfaceType;
    }

    public void setInterfaceType(Class interfaceType) {
        this.interfaceType = interfaceType;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getBeanName() {
        return beanName;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    @Override
    public int hashCode() {

        String str = interfaceName + uniqueId;

        char[] charArr = str.toCharArray();
        int hash = 0;

        for(char c : charArr) {
            hash = hash * 131 + c;
        }
        return hash;
    }

    /**
     * 由 tr 发布的规范知道依据 interface 和 unique id 就可以唯一锁定一个 tr service
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj) {


        return  this == obj ||
                obj != null
                && obj instanceof TrBeanDescriptor
                && StringUtils.equals(this.interfaceName, ((TrBeanDescriptor) obj).interfaceName)
                && StringUtils.equals(this.uniqueId, ((TrBeanDescriptor) obj).uniqueId);

    }

    @Override
    public String toString() {
        return "TrBeanDescriptor{" +
                "interface='" + interfaceName + '\'' +
                ", beanName='" + beanName + '\'' +
                ", uniqueId='" + uniqueId + '\'' +
                '}';
    }

    public int getBindingCode() {
        return bindingCode;
    }

    public void setBindingCode(int bindingCode) {
        this.bindingCode = bindingCode;
    }
}
