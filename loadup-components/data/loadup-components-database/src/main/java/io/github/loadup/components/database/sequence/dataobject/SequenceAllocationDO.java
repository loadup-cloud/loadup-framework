/*
 * #%L
 * Loadup Components Database
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
package io.github.loadup.components.database.sequence.dataobject;

import com.mybatisflex.annotation.Table;
import io.github.loadup.commons.dataobject.BaseDO;

@Table("loadup_sequence_allocation")
public class SequenceAllocationDO extends BaseDO {
    private String scopeKey;
    private Long maxAllocated;
    private Long maxValue;

    public String getScopeKey() {
        return scopeKey;
    }

    public void setScopeKey(String v) {
        scopeKey = v;
    }

    public Long getMaxAllocated() {
        return maxAllocated;
    }

    public void setMaxAllocated(Long v) {
        maxAllocated = v;
    }

    public Long getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(Long v) {
        maxValue = v;
    }
}
