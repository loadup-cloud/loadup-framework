package com.github.loadup.components.gateway.certification.spi;

/**
 * 安全组件缓存刷新接口， 安全组件开放接口，接入组件的应用实现刷新逻辑
 */
public interface CertCacheRefresher {

    /**
     * 获取接入安全组件的应用名
     */
    public String getAppName();

    /**
     * 根据certCode刷新对应的安全组件的缓存
     */
    public void refreshCache(String certCode);
}