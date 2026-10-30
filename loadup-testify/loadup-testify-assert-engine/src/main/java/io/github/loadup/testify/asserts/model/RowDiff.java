/*
 * #%L
 * Testify Assert Engine
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package io.github.loadup.testify.asserts.model;

import io.github.loadup.commons.json.ToStringAsJson;
import java.util.Map;

public class RowDiff {

    private int index;

    private String type;

    private String message;

    private Map<String, Object> expectedRow;

    private Map<String, FieldDiff> fieldDiffs;

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

    public static RowDiff missing(int index, String message, Map<String, Object> expectedRow) {
        return new RowDiff(index, "MISSING", message, expectedRow, null);
    }

    public static RowDiff diff(
            int index, String message, Map<String, Object> expectedRow, Map<String, FieldDiff> fieldDiffs) {
        return new RowDiff(index, "DIFF", message, expectedRow, fieldDiffs);
    }

    public int getIndex() {
        return this.index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, Object> getExpectedRow() {
        return this.expectedRow;
    }

    public void setExpectedRow(Map<String, Object> expectedRow) {
        this.expectedRow = expectedRow;
    }

    public Map<String, FieldDiff> getFieldDiffs() {
        return this.fieldDiffs;
    }

    public void setFieldDiffs(Map<String, FieldDiff> fieldDiffs) {
        this.fieldDiffs = fieldDiffs;
    }

    @Override
    public String toString() {
        return ToStringAsJson.reflectionToString(this);
    }
}
