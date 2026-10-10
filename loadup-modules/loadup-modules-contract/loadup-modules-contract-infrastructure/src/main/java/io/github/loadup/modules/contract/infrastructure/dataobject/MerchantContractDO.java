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

@Table("merchant_contract")
public class MerchantContractDO extends BaseDO {
    private String merchantId;
    private String scopeKey;
    private String planVersionId;
    private String status;
    private Long generation;
    private String requestKey;
    private String requestDigest;
    private String createdBy;

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String value) {
        this.merchantId = value;
    }

    public String getScopeKey() {
        return scopeKey;
    }

    public void setScopeKey(String value) {
        this.scopeKey = value;
    }

    public String getPlanVersionId() {
        return planVersionId;
    }

    public void setPlanVersionId(String value) {
        this.planVersionId = value;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String value) {
        this.status = value;
    }

    public Long getGeneration() {
        return generation;
    }

    public void setGeneration(Long value) {
        this.generation = value;
    }

    public String getRequestKey() {
        return requestKey;
    }

    public void setRequestKey(String value) {
        this.requestKey = value;
    }

    public String getRequestDigest() {
        return requestDigest;
    }

    public void setRequestDigest(String value) {
        this.requestDigest = value;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String value) {
        this.createdBy = value;
    }

    @Override
    public String toString() {
        return ToStringAsJson.reflectionToString(this);
    }
}
