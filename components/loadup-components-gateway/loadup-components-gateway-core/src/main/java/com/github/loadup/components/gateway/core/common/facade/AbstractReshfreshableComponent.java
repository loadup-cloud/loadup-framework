package com.github.loadup.components.gateway.core.common.facade;

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

import com.github.loadup.components.gateway.core.common.cache.CacheName;

/**
 * 抽象可刷新组件类
 */
public abstract class AbstractReshfreshableComponent implements Refreshable {

    /**
     * 是否初始化完毕
     */
    protected boolean isInitOk = false;

    /**
     * 缓存域
     */
    protected CacheName[] domains;

    /**
     * @see Refreshable#isInitOk()
     */
    public boolean isInitOk() {
        return isInitOk;
    }

    /**
     * Setter method for property <tt>isInitOk</tt>.
     */
    public void setInitOk(boolean isInitOk) {
        this.isInitOk = isInitOk;
    }

    /**
     * @see Refreshable#getCacheDomains()
     */
    public CacheName[] getCacheDomains() {
        return domains;
    }
}
