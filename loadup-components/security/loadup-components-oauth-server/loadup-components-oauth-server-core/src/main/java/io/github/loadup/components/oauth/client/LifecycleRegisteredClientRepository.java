/*
 * Copyright (C) 2025-2026 LoadUp Cloud
 *
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
 */
package io.github.loadup.components.oauth.client;

import io.github.loadup.components.oauth.OAuthClientSettings;
import java.time.Clock;
import java.util.Objects;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.transaction.support.TransactionOperations;

/** Protocol-facing view. Disabled and expired clients cannot authenticate or redeem grants. */
public final class LifecycleRegisteredClientRepository implements RegisteredClientRepository {
    private final RegisteredClientRepository storage;
    static final String REVISION = "loadup.oauth.revision";
    private final Clock clock;
    private final JdbcOperations jdbc;
    private final TransactionOperations transactions;

    public LifecycleRegisteredClientRepository(
            RegisteredClientRepository storage, JdbcOperations jdbc, TransactionOperations transactions, Clock clock) {
        this.storage = Objects.requireNonNull(storage);
        this.clock = Objects.requireNonNull(clock);
        this.jdbc = Objects.requireNonNull(jdbc);
        this.transactions = Objects.requireNonNull(transactions);
    }

    RegisteredClientRepository storage() {
        return storage;
    }

    @Override
    public void save(RegisteredClient client) {
        Objects.requireNonNull(client);
        transactions.executeWithoutResult(status -> {
            // Spring may save an upgraded password hash during authentication. A management
            // change must not be overwritten by that older authentication snapshot.
            jdbc.queryForList(
                    "select id from oauth2_registered_client where id = ? for update", String.class, client.getId());
            var current = storage.findById(client.getId());
            if (current != null && revision(current) != revision(client)) {
                throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_CLIENT);
            }
            storage.save(client);
        });
    }

    static long revision(RegisteredClient client) {
        Object revision = client.getClientSettings().getSetting(REVISION);
        if (revision == null) {
            return 0;
        }
        if (!(revision instanceof Number value) || value.longValue() < 0) {
            throw new IllegalArgumentException("Invalid OAuth client revision");
        }
        return value.longValue();
    }

    @Override
    public RegisteredClient findById(String id) {
        return active(storage.findById(id));
    }

    @Override
    public RegisteredClient findByClientId(String clientId) {
        return active(storage.findByClientId(clientId));
    }

    private RegisteredClient active(RegisteredClient client) {
        return isActive(client, clock) ? client : null;
    }

    static boolean isActive(RegisteredClient client, Clock clock) {
        if (client == null) {
            return false;
        }
        Object enabled = client.getClientSettings().getSetting(OAuthClientSettings.ENABLED);
        return (enabled == null || Boolean.TRUE.equals(enabled))
                && (client.getClientSecretExpiresAt() == null
                        || clock.instant().isBefore(client.getClientSecretExpiresAt()));
    }
}
