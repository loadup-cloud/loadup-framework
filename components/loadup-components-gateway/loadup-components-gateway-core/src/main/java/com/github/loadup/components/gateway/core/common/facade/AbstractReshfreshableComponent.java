package com.github.loadup.components.gateway.core.common.facade;

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
