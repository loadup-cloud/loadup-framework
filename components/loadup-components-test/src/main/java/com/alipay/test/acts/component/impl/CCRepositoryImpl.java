/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.alipay.test.acts.component.impl;

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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.alipay.test.acts.component.CCComponent;
import com.alipay.test.acts.component.CCDiscoverer;
import com.alipay.test.acts.component.CCRepository;

/**
 * 仓库实现。
 * 
 * @author dasong.jds
 * @version $Id: CCRepositoryImpl.java, v 0.1 2015年4月23日 下午10:02:40 dasong.jds Exp $
 */
public class CCRepositoryImpl<A extends Annotation> implements CCRepository {

    /** 组件集合 */
    private volatile Map<String, CCComponent> components = null;

    /** 仓储回调 */
    private RepositoryCallback<A>             repositoryCallback;

    /** 组件发现器 */
    private CCDiscoverer                      ccDiscoverer;

    /** 
     * @see com.ipay.itest.common.repository.CCRepository#getComponent(String)
     */
    @Override
    public CCComponent getComponent(String id) {

        Map<String, CCComponent> components = this.components;

        if (components == null) {
            synchronized (this) {
                components = this.components;
                if (components == null) {
                    components = findComponents();
                    this.components = components;
                }
            }
        }
        return components.get(id);
    }

    /**
     * 解析组件。
     * 
     * @return      组件集合
     */
    private Map<String, CCComponent> findComponents() {

        Map<String, CCComponent> components = new HashMap<String, CCComponent>();

        if (ccDiscoverer != null) {

            Object[] beans = ccDiscoverer.discover();

            for (Object bean : beans) {
                Class<?> type = bean.getClass();

                Method[] methods = type.getMethods();

                for (Method method : methods) {
                    A desc = method.getAnnotation(repositoryCallback.getCCType());
                    if (desc != null) {
                        CCComponent comp = repositoryCallback.createComponent(desc, bean, method);
                        if (comp != null) {
                            components.put(comp.getId(), comp);
                        }
                    }
                }
            }
        }

        return components;
    }

    /**
     * Setter method for property <tt>repositoryCallback</tt>.
     * 
     * @param repositoryCallback value to be assigned to property repositoryCallback
     */
    public void setRepositoryCallback(RepositoryCallback<A> repositoryCallback) {
        this.repositoryCallback = repositoryCallback;
    }

    /**
     * Setter method for property <tt>ccDiscoverer</tt>.
     * 
     * @param ccDiscoverer value to be assigned to property ccDiscoverer
     */
    public void setCcDiscoverer(CCDiscoverer ccDiscoverer) {
        this.ccDiscoverer = ccDiscoverer;
    }

}
