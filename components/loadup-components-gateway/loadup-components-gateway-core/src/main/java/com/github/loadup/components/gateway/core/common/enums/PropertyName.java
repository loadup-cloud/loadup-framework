package com.github.loadup.components.gateway.core.common.enums;

/**
 * 通用配置属性key约定
 */
public interface PropertyName {

    /**
     * 属性名称
     */
    String getName();

    /**
     * 属性描述
     */
    String getDescription();

    /**
     * 获取默认值
     */
    String getDefaultValue();

}
