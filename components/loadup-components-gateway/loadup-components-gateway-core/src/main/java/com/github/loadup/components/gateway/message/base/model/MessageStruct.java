package com.github.loadup.components.gateway.message.base.model;

import com.github.loadup.components.gateway.core.common.enums.MessageFormat;

/**
 * 报文结构，属于报文组件系统设计的内部概念，通讯组件是不需要关注的<br>
 *
 * <p>
 * <b>注意事项</b>
 *    <ul>
 *       <li>报文结构分类的粒度不会很细，比如XML报文、定出报文、分割符方式的报文，其报文结构都是属于文本报文
 *       <li>每一种结构一般都需要一种报文组装器进行包装，一种报文解析器进行解析
 *    </ul>
 * </p>
 */
public enum MessageStruct {

    /**
     * 文本报文
     */
    TEXT(MessageFormat.TEXT, "文本报文"),

    /**
     * 标准8583
     */
    ISO8583(MessageFormat.BYTE, "标准8583"),

    /**
     * MAP结构报文
     */
    MAP(MessageFormat.MAP, "FORM表单");

    /**
     * 报文的数据格式
     */
    private final MessageFormat format;

    /**
     * 报文描述，作为后台配置的页面展示文案
     */
    private final String description;

    /**
     *
     */
    private MessageStruct(MessageFormat format, String description) {
        this.format = format;
        this.description = description;
    }

    /**
     * Getter method for property <tt>format</tt>.
     */
    public MessageFormat getFormat() {
        return format;
    }

    /**
     * Getter method for property <tt>description</tt>.
     */
    public String getDescription() {
        return description;
    }

}
