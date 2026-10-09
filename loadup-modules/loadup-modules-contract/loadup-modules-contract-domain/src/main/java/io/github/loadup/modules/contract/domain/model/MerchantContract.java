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
package io.github.loadup.modules.contract.domain.model;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

/** Read-only aggregate assembled from a tenant-scoped, authoritative repository query. */
public record MerchantContract(
        String tenantId,
        String merchantId,
        String scopeKey,
        String contractId,
        ContractStatus status,
        long generation,
        List<MerchantContractRevision> revisions) {
    public MerchantContract {
        tenantId = ContractChecks.code(tenantId);
        merchantId = ContractChecks.code(merchantId);
        scopeKey = ContractChecks.code(scopeKey);
        contractId = ContractChecks.code(contractId);
        Objects.requireNonNull(status, "status");
        if (generation < 1) throw new IllegalArgumentException("Generation must be positive");
        revisions = revisions.stream()
                .sorted(Comparator.comparing(MerchantContractRevision::effectiveFrom))
                .toList();
        if (revisions.isEmpty() || revisions.size() > 256) throw new IllegalArgumentException("Invalid revision count");
        var versions = new HashSet<Integer>();
        MerchantContractRevision previous = null;
        for (var revision : revisions) {
            if (!revision.tenantId().equals(tenantId)
                    || !revision.merchantId().equals(merchantId)
                    || !revision.scopeKey().equals(scopeKey)
                    || !revision.contractId().equals(contractId)) {
                throw new IllegalArgumentException("Contract identity mismatch");
            }
            if (!versions.add(revision.revision())) throw new IllegalArgumentException("Duplicate revision");
            if (previous != null
                    && (previous.effectiveTo() == null
                            || revision.effectiveFrom().isBefore(previous.effectiveTo()))) {
                throw new IllegalArgumentException("Overlapping revisions");
            }
            previous = revision;
        }
    }
}
