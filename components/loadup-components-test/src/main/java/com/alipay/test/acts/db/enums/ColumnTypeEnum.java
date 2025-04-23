package com.alipay.test.acts.db.enums;

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
 * 数据库列类型枚举
 * 
 * @author baishuo.lp
 * @version $Id: ColumnTypeEnum.java, v 0.1 2015年3月2日 下午6:42:03 baishuo.lp Exp $
 */
public enum ColumnTypeEnum {

    VARCHAR("VARCHAR", "字符类型"),

    INT("INT", "数字类型"),

    NUMERIC("NUMERIC", "数字类型"),

    TIMESTAMP("TIMESTAMP", "时间戳类型"),

    NULL("NULL", "空");

    private ColumnTypeEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    private String code;

    private String description;

    public static ColumnTypeEnum getByPrefix(String columnType) {
        for (ColumnTypeEnum columnTypeEnum : ColumnTypeEnum.values()) {
            if (columnType.startsWith(columnTypeEnum.getCode())) {
                return columnTypeEnum;
            }
        }
        return ColumnTypeEnum.NULL;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

}
