/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.testify.data;

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

import com.github.loadup.components.testify.data.enums.MetaInitType;

/**
 * 原子初始项。
 * <p> 与DB字典和业务模型对应。
 *
 *
 *
 */
public class MetaInitItem {

    /**
     * 系统名
     */
    private final String system;

    /**
     * 所属对象，支持路径描述
     */
    private final String host;

    /**
     * 字段
     */
    private final String field;

    /**
     * 元数据类型
     */
    private MetaInitType initType;

    /**
     * 元数据字段类型
     */
    private MetaFieldType fieldType;

    /**
     * @param system
     * @param host
     * @param field
     */
    public MetaInitItem(String system, String host, String field) {
        super();
        this.system = system;
        this.field = field;

        int index = host.lastIndexOf(".");
        if (index > 0) {
            host = host.substring(index + 1);
        }
        this.host = host;
    }

    /**
     *
     *
     * @return property value of host
     */
    public String getHost() {
        return host;
    }

    /**
     *
     *
     * @return property value of field
     */
    public String getField() {
        return field;
    }

    /**
     *
     *
     * @return property value of system
     */
    public String getSystem() {
        return system;
    }

    /**
     *
     *
     * @return property value of initType
     */
    public MetaInitType getInitType() {
        return initType;
    }

    /**
     *
     *
     * @param initType value to be assigned to property initType
     */
    public void setInitType(MetaInitType initType) {
        this.initType = initType;
    }

    /**
     *
     *
     * @return property value of fieldType
     */
    public MetaFieldType getFieldType() {
        return fieldType;
    }

    /**
     *
     *
     * @param fieldType value to be assigned to property fieldType
     */
    public void setFieldType(MetaFieldType fieldType) {
        this.fieldType = fieldType;
    }

    /**
     * @see Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((field == null) ? 0 : field.hashCode());
        result = prime * result + ((host == null) ? 0 : host.hashCode());
        result = prime * result + ((system == null) ? 0 : system.hashCode());
        return result;
    }

    /**
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        MetaInitItem other = (MetaInitItem) obj;
        if (field == null) {
            if (other.field != null) return false;
        } else if (!field.equals(other.field)) return false;
        if (host == null) {
            if (other.host != null) return false;
        } else if (!host.equals(other.host)) return false;
        if (system == null) {
            if (other.system != null) return false;
        } else if (!system.equals(other.system)) return false;
        return true;
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("MetaInitItem[system=");
        builder.append(system);
        builder.append(",host=");
        builder.append(host);
        builder.append(",field=");
        builder.append(field);
        builder.append(",initType=");
        builder.append(initType);
        builder.append(",fieldType=");
        builder.append(fieldType);
        builder.append("]");
        return builder.toString();
    }
}
