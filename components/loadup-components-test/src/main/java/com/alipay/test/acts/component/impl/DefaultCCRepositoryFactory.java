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

import java.util.Map;

import com.alipay.test.acts.component.CCRepository;
import com.alipay.test.acts.component.CCRepositoryFactory;
import com.alipay.test.acts.component.enums.RepositoryType;

/**
 * 工厂默认实现。
 * 
 * @author dasong.jds
 * @version $Id: DefaultCCRepositoryFactory.java, v 0.1 2015年5月3日 下午11:18:51 dasong.jds Exp $
 */
public class DefaultCCRepositoryFactory implements CCRepositoryFactory {

    /** 组件集 */
    private Map<String, CCRepository> repositories;

    /** 
     * @see com.CCRepositoryFactory.itest.common.repository.RepositoryFactory#getRepository(com.ipay.itest.common.enums.RepositoryType)
     */
    @Override
    public CCRepository getRepository(RepositoryType repositoryType) {

        return repositories.get(repositoryType.name());
    }

    /**
     * Setter method for property <tt>repositories</tt>.
     * 
     * @param repositories value to be assigned to property repositories
     */
    public void setRepositories(Map<String, CCRepository> repositories) {
        this.repositories = repositories;
    }

}
