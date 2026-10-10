/*
 * #%L
 * LoadUp Contract
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
package io.github.loadup.modules.contract.infrastructure.dataobject;

import com.mybatisflex.annotation.Table;
import io.github.loadup.commons.dataobject.BaseDO;
import io.github.loadup.commons.json.ToStringAsJson;

@Table("merchant_contract_revision")
public class ContractRevisionDO extends BaseDO {
    private String contractId;
    private Integer revision;
    private String snapshot;
    private String snapshotHash;

    public String getContractId() {
        return contractId;
    }

    public void setContractId(String value) {
        this.contractId = value;
    }

    public Integer getRevision() {
        return revision;
    }

    public void setRevision(Integer value) {
        this.revision = value;
    }

    public String getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(String value) {
        this.snapshot = value;
    }

    public String getSnapshotHash() {
        return snapshotHash;
    }

    public void setSnapshotHash(String value) {
        this.snapshotHash = value;
    }

    @Override
    public String toString() {
        return ToStringAsJson.reflectionToString(this);
    }
}
