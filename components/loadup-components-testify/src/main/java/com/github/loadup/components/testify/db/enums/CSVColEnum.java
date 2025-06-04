/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.testify.db.enums;

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

/**
 * csv数据库文件列值枚举
 *
 *
 *
 */
public enum CSVColEnum {
    COLUMN("columnName", "数据库CSV文件列名"),

    COMMENT("comment", "数据库CSV文件注释"),

    TYPE("dataType", "数据库CSV文件列类型"),

    PRIMARY("primaryKey", "是否为主键"),

    NULLABLE("nullable", "是否可空"),

    CLASS("class", "对象CSV文件类名"),

    PROPERTY("property", "对象CSV文件属性名称"),

    FLAG("flag", "生成/比较标识"),

    RULE("rule", "原子数据规则"),

    VALUE("value", "字段具体值");

    private String code;

    private String description;

    private CSVColEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static CSVColEnum getByCode(String code) {
        for (CSVColEnum col : CSVColEnum.values()) {
            if (col.getCode().equalsIgnoreCase(code)) {
                return col;
            }
        }
        return null;
    }

    /**
     *
     *
     * @return property value of code
     */
    public String getCode() {
        return code;
    }

    /**
     *
     *
     * @return property value of description
     */
    public String getDescription() {
        return description;
    }
}
