/*-
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

import io.github.loadup.components.gotone.config.ChannelConfigProvider.ChannelConfig;
import io.github.loadup.components.gotone.model.NotificationRequest;
import io.github.loadup.components.gotone.record.RecordHandler;
import io.github.loadup.components.gotone.store.dataobject.NotificationRecordDO;
import io.github.loadup.components.gotone.store.mapper.NotificationRecordDOMapper;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JDBC-backed {@link RecordHandler} that persists one notification record per receiver.
 */
public class JdbcRecordHandler implements RecordHandler {

    private static final String STATUS_SUCCESS = "SUCCESS";
    private static final String STATUS_FAILED = "FAILED";

    private final NotificationRecordDOMapper mapper;

    public JdbcRecordHandler(NotificationRecordDOMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void onResult(NotificationRequest request, ChannelConfig config, SendAttemptResult result) {
        LocalDateTime now = LocalDateTime.now();
        for (String receiver : request.receivers()) {
            NotificationRecordDO record = new NotificationRecordDO();
            record.setId(UUID.randomUUID().toString());
            record.setServiceCode(request.serviceCode());
            record.setRequestId(request.requestId());
            record.setChannel(config.channel());
            record.setProvider(result.actualProvider());
            record.setReceiver(receiver);
            record.setContent(result.content());
            record.setStatus(
                    Boolean.TRUE.equals(result.receiverStatus().get(receiver)) ? STATUS_SUCCESS : STATUS_FAILED);
            record.setErrorMessage(result.receiverErrors().get(receiver));
            record.setSendTime(now);
            mapper.insert(record);
        }
    }
}
