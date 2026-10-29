package io.github.loadup.modules.merchant.client.dto;

import io.github.loadup.commons.json.ToStringAsJson;

/** Minimal authoritative facts for local transactional collaborators. */
public record MerchantPersistenceFacts(String id, boolean active, long version) {
    @Override
    public String toString() {
        return ToStringAsJson.reflectionToString(this);
    }
}
