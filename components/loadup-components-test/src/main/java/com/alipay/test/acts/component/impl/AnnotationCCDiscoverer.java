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

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.alipay.test.acts.component.CCBase;
import com.alipay.test.acts.component.CCDiscoverer;
import com.alipay.test.acts.component.annotation.CCHost;

/**
 * 通过spring容器+注解方式获取组件。
 * 
 * @author dasong.jds
 * @version $Id: AnnotationCCDiscoverer.java, v 0.1 2015年5月11日 下午9:34:14 dasong.jds Exp $
 */
public class AnnotationCCDiscoverer implements CCDiscoverer, ApplicationContextAware {

    /** spring 应用上下文 */
    private ApplicationContext applicationContext;

    /** 
     * @see com.ipay.itest.common.repository.CCDiscoverer#discover()
     */
    @Override
    public Object[] discover() {

        Map<String, Object> beans = new HashMap<String, Object>();

        beans.putAll(applicationContext.getBeansWithAnnotation(CCHost.class));

        beans.putAll(applicationContext.getBeansOfType(CCBase.class));

        return beans.values().toArray();
    }

    /** 
     * @see ApplicationContextAware#setApplicationContext(ApplicationContext)
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

        this.applicationContext = applicationContext;
    }

}
