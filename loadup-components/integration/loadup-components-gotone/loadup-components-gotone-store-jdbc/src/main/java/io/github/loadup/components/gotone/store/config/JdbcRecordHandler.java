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
import io.github.loadup.components.gotone.store.dataobject.NotificationRecordRow;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;

/**
 * JDBC-backed {@link RecordHandler} that persists one notification record per receiver.
 */
public class JdbcRecordHandler implements RecordHandler {

    private static final String STATUS_SUCCESS = "SUCCESS";
    private static final String STATUS_FAILED = "FAILED";

    private final JdbcAggregateTemplate aggregates;
    private final DatabaseTenant tenant;
    private final Clock clock;

    public JdbcRecordHandler(JdbcAggregateTemplate aggregates, DatabaseTenant tenant, Clock clock) {
        this.aggregates = aggregates;
        this.tenant = tenant;
        this.clock = clock;
    }

    @Override
    public void onResult(NotificationRequest request, ChannelConfig config, SendAttemptResult result) {
        LocalDateTime now = LocalDateTime.now(clock);
        for (String receiver : request.receivers()) {
            boolean delivered = "WEBHOOK".equals(config.channel())
                    ? result.success()
                    : Boolean.TRUE.equals(result.receiverStatus().get(receiver));
            aggregates.insert(new NotificationRecordRow(UUID.randomUUID().toString(), tenant.current(),
                    request.serviceCode(), null, request.requestId(), config.channel(), result.actualProvider(), receiver,
                    result.content(), delivered ? STATUS_SUCCESS : STATUS_FAILED, result.receiverErrors().get(receiver),
                    now, now, now, false));
        }
    }
}
