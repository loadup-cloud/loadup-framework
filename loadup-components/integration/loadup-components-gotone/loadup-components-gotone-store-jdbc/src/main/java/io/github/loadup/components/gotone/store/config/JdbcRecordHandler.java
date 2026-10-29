/*
 * #%L
 * Loadup Gotone Store JDBC
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
package io.github.loadup.components.gotone.store.config;

import io.github.loadup.components.database.tenant.DatabaseTenant;
import io.github.loadup.components.gotone.config.ChannelConfigProvider.ChannelConfig;
import io.github.loadup.components.gotone.model.NotificationRequest;
import io.github.loadup.components.gotone.record.RecordHandler;
import io.github.loadup.components.gotone.store.dataobject.NotificationRecordDO;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * JDBC-backed {@link RecordHandler} that persists one notification record per receiver.
 */
public class JdbcRecordHandler implements RecordHandler {

    private static final String STATUS_SUCCESS = "SUCCESS";
    private static final String STATUS_FAILED = "FAILED";

    private final NamedParameterJdbcTemplate jdbc;
    private final DatabaseTenant tenant;
    private final Clock clock;

    public JdbcRecordHandler(NamedParameterJdbcTemplate jdbc, DatabaseTenant tenant, Clock clock) {
        this.jdbc = jdbc;
        this.tenant = tenant;
        this.clock = clock;
    }

    @Override
    public void onResult(NotificationRequest request, ChannelConfig config, SendAttemptResult result) {
        LocalDateTime now = LocalDateTime.now(clock);
        for (String receiver : request.receivers()) {
            NotificationRecordDO record = new NotificationRecordDO();
            record.setId(UUID.randomUUID().toString());
            record.setServiceCode(request.serviceCode());
            record.setRequestId(request.requestId());
            record.setChannel(config.channel());
            record.setProvider(result.actualProvider());
            record.setReceiver(receiver);
            record.setContent(result.content());
            boolean delivered = "WEBHOOK".equals(config.channel())
                    ? result.success()
                    : Boolean.TRUE.equals(result.receiverStatus().get(receiver));
            record.setStatus(delivered ? STATUS_SUCCESS : STATUS_FAILED);
            record.setErrorMessage(result.receiverErrors().get(receiver));
            record.setSendTime(now);
            record.setTenantId(tenant.current());
            record.setCreatedAt(now);
            record.setUpdatedAt(now);
            jdbc.update("""
                    INSERT INTO gotone_notification_record (id, tenant_id, service_code, trace_id, request_id, channel, provider,
                        receiver, content, status, error_message, send_time, created_at, updated_at, deleted)
                    VALUES (:id, :tenantId, :serviceCode, :traceId, :requestId, :channel, :provider, :receiver, :content,
                        :status, :errorMessage, :sendTime, :createdAt, :updatedAt, FALSE)
                    """, new BeanPropertySqlParameterSource(record));
        }
    }
}
