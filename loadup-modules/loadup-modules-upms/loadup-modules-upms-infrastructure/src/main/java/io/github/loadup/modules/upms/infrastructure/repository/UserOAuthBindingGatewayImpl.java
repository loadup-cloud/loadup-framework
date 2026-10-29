/*
 * #%L
 * Loadup Modules UPMS Infrastructure Layer
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
package io.github.loadup.modules.upms.infrastructure.repository;

import io.github.loadup.commons.util.IdUtils;
import io.github.loadup.components.database.tenant.DatabaseTenant;
import io.github.loadup.modules.upms.domain.gateway.UserOAuthBindingGateway;
import io.github.loadup.modules.upms.domain.model.UserOAuthBinding;
import io.github.loadup.modules.upms.infrastructure.converter.UserOAuthBindingConverter;
import io.github.loadup.modules.upms.infrastructure.dataobject.UserOAuthBindingDO;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class UserOAuthBindingGatewayImpl implements UserOAuthBindingGateway {
    private final NamedParameterJdbcTemplate jdbc;
    private final Clock clock;
    private final DatabaseTenant tenant;
    private final UserOAuthBindingConverter converter;

    public UserOAuthBindingGatewayImpl(
            NamedParameterJdbcTemplate jdbc,
            UserOAuthBindingConverter converter,
            @Qualifier("databaseClock") Clock clock,
            DatabaseTenant tenant) {
        this.jdbc = jdbc;
        this.clock = clock;
        this.tenant = tenant;
        this.converter = converter;
    }

    @Override
    @Transactional
    public UserOAuthBinding save(UserOAuthBinding binding) {
        var row = converter.toDO(binding);
        var users = jdbc.queryForList(
                "SELECT id FROM upms_user WHERE id = :userId AND tenant_id IS NOT DISTINCT FROM :tenant AND deleted = FALSE AND status = 1 FOR UPDATE",
                params().addValue("userId", row.getUserId()),
                String.class);
        if (users.isEmpty()) throw new IllegalArgumentException("Active user for OAuth binding not found");
        if (row.getId() == null) row.setId(IdUtils.uuid());
        var now = LocalDateTime.now(clock);
        if (row.getCreatedAt() == null) row.setCreatedAt(now);
        row.setUpdatedAt(now);
        row.setTenantId(tenant.current());
        int affected = jdbc.update("""
                INSERT INTO upms_user_oauth_binding (id, tenant_id, user_id, provider, open_id, union_id,
                    nickname, avatar, access_token, refresh_token, expires_at, bound_at, created_at, updated_at, deleted)
                VALUES (:id, :tenantId, :userId, :provider, :openId, :unionId, :nickname, :avatar,
                    :accessToken, :refreshToken, :expiresAt, :boundAt, :createdAt, :updatedAt, FALSE)
                ON CONFLICT (id) DO UPDATE SET user_id = EXCLUDED.user_id, provider = EXCLUDED.provider,
                    open_id = EXCLUDED.open_id, union_id = EXCLUDED.union_id, nickname = EXCLUDED.nickname,
                    avatar = EXCLUDED.avatar, access_token = EXCLUDED.access_token, refresh_token = EXCLUDED.refresh_token,
                    expires_at = EXCLUDED.expires_at, bound_at = EXCLUDED.bound_at, updated_at = EXCLUDED.updated_at
                WHERE upms_user_oauth_binding.tenant_id IS NOT DISTINCT FROM EXCLUDED.tenant_id
                    AND upms_user_oauth_binding.deleted = FALSE
                """, new BeanPropertySqlParameterSource(row));
        if (affected != 1) throw new IllegalStateException("OAuth binding was not saved");
        return converter.toEntity(row);
    }

    @Override
    public Optional<UserOAuthBinding> findByProviderAndOpenId(String provider, String openId) {
        return find(
                        "provider = :provider AND open_id = :openId",
                        params().addValue("provider", provider).addValue("openId", openId))
                .stream()
                .findFirst();
    }

    @Override
    public List<UserOAuthBinding> findByUserId(String userId) {
        return find("user_id = :userId", params().addValue("userId", userId));
    }

    @Override
    public Optional<UserOAuthBinding> findByUserIdAndProvider(String userId, String provider) {
        return find(
                        "user_id = :userId AND provider = :provider",
                        params().addValue("userId", userId).addValue("provider", provider))
                .stream()
                .findFirst();
    }

    @Override
    public void delete(String id) {
        deleteWhere("id = :id", params().addValue("id", id));
    }

    @Override
    public void deleteByUserIdAndProvider(String userId, String provider) {
        deleteWhere(
                "user_id = :userId AND provider = :provider",
                params().addValue("userId", userId).addValue("provider", provider));
    }

    private List<UserOAuthBinding> find(String predicate, MapSqlParameterSource params) {
        return jdbc
                .query(
                        "SELECT id, tenant_id, user_id, provider, open_id, union_id, nickname, avatar, access_token, refresh_token, expires_at, bound_at, created_at, updated_at, CASE WHEN deleted THEN 1 ELSE 0 END AS deleted FROM upms_user_oauth_binding WHERE deleted = FALSE AND tenant_id IS NOT DISTINCT FROM :tenant AND "
                                + predicate,
                        params,
                        BeanPropertyRowMapper.newInstance(UserOAuthBindingDO.class))
                .stream()
                .map(converter::toEntity)
                .toList();
    }

    private void deleteWhere(String predicate, MapSqlParameterSource params) {
        jdbc.update(
                "UPDATE upms_user_oauth_binding SET deleted = TRUE, updated_at = :now WHERE deleted = FALSE AND tenant_id IS NOT DISTINCT FROM :tenant AND "
                        + predicate,
                params.addValue("now", LocalDateTime.now(clock)));
    }

    private MapSqlParameterSource params() {
        return new MapSqlParameterSource("tenant", tenant.current());
    }
}
