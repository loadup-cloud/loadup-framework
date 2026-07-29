package io.github.loadup.testify.asserts.model;

/*-
 * #%L
 * Testify Assert Engine
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import java.util.Map;

/**
 * 数据库行差异模型
 */
public class RowDiff {
    /**
     * YAML 中定义的期望行索引（从 0 开始）
     */
    private int index;

    /**
     * 差异类型： MISSING - 数据库中找不到匹配的行 DIFF - 找到行了，但字段内容对比失败
     */
    private String type;

    /**
     * 错误概览描述
     */
    private String message;

    /**
     * 原始的期望行数据（用于定位或记录日志）
     */
    private Map<String, Object> expectedRow;

    /**
     * 具体的字段差异明细 Key: 字段名, Value: 字段差异详情
     */
    private Map<String, FieldDiff> fieldDiffs;

    /**
     * 快速构造缺失行的静态方法
     */
    public static RowDiff missing(int index, String message, Map<String, Object> expectedRow) {
        return new RowDiff(index, "MISSING", message, expectedRow, null);
    }

    /**
     * 快速构造内容差异行的静态方法
     */
    public static RowDiff diff(
            int index, String message, Map<String, Object> expectedRow, Map<String, FieldDiff> fieldDiffs) {
        return new RowDiff(index, "DIFF", message, expectedRow, fieldDiffs);
    }

    public RowDiff(
            int index,
            String type,
            String message,
            Map<String, Object> expectedRow,
            Map<String, FieldDiff> fieldDiffs) {
        this.index = index;
        this.type = type;
        this.message = message;
        this.expectedRow = expectedRow;
        this.fieldDiffs = fieldDiffs;
    }

    public RowDiff() {}

    public int getIndex() {
        return this.index;
    }

    public String getType() {
        return this.type;
    }

    public String getMessage() {
        return this.message;
    }

    public Map<String, Object> getExpectedRow() {
        return this.expectedRow;
    }

    public Map<String, FieldDiff> getFieldDiffs() {
        return this.fieldDiffs;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setExpectedRow(Map<String, Object> expectedRow) {
        this.expectedRow = expectedRow;
    }

    public void setFieldDiffs(Map<String, FieldDiff> fieldDiffs) {
        this.fieldDiffs = fieldDiffs;
    }

    @Override
    public String toString() {
        return org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString(
                this, org.apache.commons.lang3.builder.ToStringStyle.JSON_STYLE);
    }
}
