package com.github.loadup.components.gateway.message.base.model;

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
     *
     */
    public MessageFormat getFormat() {
        return format;
    }

    /**
     *
     */
    public String getDescription() {
        return description;
    }
}
