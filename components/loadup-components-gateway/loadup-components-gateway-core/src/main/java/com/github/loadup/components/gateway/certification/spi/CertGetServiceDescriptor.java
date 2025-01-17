package com.github.loadup.components.gateway.certification.spi;

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

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 证书获取服务描述
 */
//@XObject("CertGetServiceDescriptor")
public class CertGetServiceDescriptor {

    /**
     * 组件的名字
     */
    //@XNode("@name")
    private String name;

    //@XNodeSpring("@listener")
    private CertGetService certGetService;

    /**
     * toString
     */
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);

    }

    /**
     * Getter method for property <tt>name<tt>.
     */
    public String getName() {
        return name;
    }

    /**
     * Setter method for property <tt>name<tt>.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter method for property <tt>certGetService<tt>.
     */
    public CertGetService getCertGetService() {
        return certGetService;
    }

    /**
     * Setter method for property <tt>certGetService<tt>.
     */
    public void setCertGetService(CertGetService certGetService) {
        this.certGetService = certGetService;
    }
}
