package com.github.loadup.components.gateway.core.model;

/**
 * 敏感内容处理类型
 */
public enum SensitivityProcessType {
    //url 类型敏感字段处理
    URL,
    //MAP类型敏感字段处理
    HEADER_MAP,
    //json类型敏感字段处理
    JSON_BODY,
    //xml 类型敏感字段处理
    XML_BODY,
    //form 类型敏感字段处理
    FORM_BODY
}