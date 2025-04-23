package com.alipay.test.acts.mock.model;

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

/**
 * MockRecoverObject
 * 
 * @author midang
 * @version $Id: MockRecoverObject.java,v 0.1 2010-11-17 下午01:45:12 midang Exp $
 */
public class MockRecoverObject {

    private Object orgBean;
    private Object proxyBean;

    public MockRecoverObject() {
    }

    public MockRecoverObject(Object orgBean, Object proxyBean) {
        this.orgBean = orgBean;
        this.proxyBean = proxyBean;
    }

    public Object getOrgBean() {
        return orgBean;
    }

    public void setOrgBean(Object orgBean) {
        this.orgBean = orgBean;
    }

    public Object getProxyBean() {
        return proxyBean;
    }

    public void setProxyBean(Object proxyBean) {
        this.proxyBean = proxyBean;
    }

}
