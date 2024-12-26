package com.github.loadup.components.gateway.core.common.facade;

/**
 * 可刷新接口，组件需要实现
 */
public interface Refreshable {

    /**
     * 初始化缓存
     */
    void init(Object... obj);

    /**
     * 初始化OK否
     */
    boolean isInitOk();

    /**
     * 刷新缓存
     */
    void refresh(Object... obj);

    /**
     * 根据id刷新部分缓存
     */
    void refreshById(String id, Object... obj);

}
