/*
 * #%L
 * Loadup Gotone Test
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
package io.github.loadup.components.gotone.test.store;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.mybatisflex.core.query.QueryWrapper;
import io.github.loadup.commons.log.LogUtil;
import io.github.loadup.components.gotone.config.ChannelConfigProvider.ChannelConfig;
import io.github.loadup.components.gotone.model.NotificationRequest;
import io.github.loadup.components.gotone.record.RecordHandler;
import io.github.loadup.components.gotone.store.config.JdbcChannelConfigProvider;
import io.github.loadup.components.gotone.store.config.JdbcRecordHandler;
import io.github.loadup.components.gotone.store.config.JdbcServiceConfigProvider;
import io.github.loadup.components.gotone.store.dataobject.NotificationRecordRow;
import io.github.loadup.components.gotone.store.dataobject.ServiceChannelRow;
import io.github.loadup.components.gotone.store.mapper.NotificationRecordRowMapper;
import io.github.loadup.components.gotone.store.mapper.NotificationServiceRowMapper;
import io.github.loadup.components.gotone.store.mapper.ServiceChannelRowMapper;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class JdbcStoreBoundaryTest {
    private final java.time.Clock clock =
            java.time.Clock.fixed(java.time.Instant.parse("2026-01-01T00:00:00Z"), java.time.ZoneOffset.UTC);

    private static ServiceChannelRow channel(String config, String fallback) {
        var now = java.time.LocalDateTime.of(2026, 1, 1, 0, 0);
        return new ServiceChannelRow(
                "id", "ALERT", "EMAIL", "template", config, "smtp", fallback, true, 0, now, now, false);
    }

    @Test
    void missingServiceAndChannelRowsReturnEmptyResults() {
        var services = mock(NotificationServiceRowMapper.class);
        var channels = mock(ServiceChannelRowMapper.class);
        when(services.selectOneByQuery(any(QueryWrapper.class))).thenReturn(null);
        when(channels.selectListByQuery(any(QueryWrapper.class))).thenReturn(List.of());
        assertThat(new JdbcServiceConfigProvider(services).findByServiceCode("missing"))
                .isEmpty();
        assertThat(new JdbcChannelConfigProvider(channels).findEnabledByServiceCode("missing"))
                .isEmpty();
        verify(services).selectOneByQuery(any(QueryWrapper.class));
        verify(channels).selectListByQuery(any(QueryWrapper.class));
    }

    @Test
    void nullBlankAndValidJsonSettingsAreMappedThroughThePublicProvider() {
        var mapper = mock(ServiceChannelRowMapper.class);
        var provider = new JdbcChannelConfigProvider(mapper);
        for (String raw : new String[] {null, " ", "null"}) {
            var row = channel(raw, raw);
            when(mapper.selectListByQuery(any(QueryWrapper.class))).thenReturn(List.of(row));
            var actual = provider.findEnabledByServiceCode("ALERT").getFirst();
            assertThat(actual.channelConfig()).isEmpty();
            assertThat(actual.fallbackProviders()).isEmpty();
        }
        when(mapper.selectListByQuery(any(QueryWrapper.class)))
                .thenReturn(List.of(channel("{\"limit\":42}", "[\"secondary\"]")));
        var actual = provider.findEnabledByServiceCode("ALERT").getFirst();
        assertThat(actual.channelConfig()).containsEntry("limit", 42);
        assertThat(actual.fallbackProviders()).containsExactly("secondary");
    }

    @Test
    void malformedJsonUsesEmptySettingsAndNeverLogsPayloadOrExceptionChain() {
        var mapper = mock(ServiceChannelRowMapper.class);
        var row = channel("{\"secret\":\"synthetic-private-marker\"", "[\"synthetic-private-marker\"");
        when(mapper.selectListByQuery(any(QueryWrapper.class))).thenReturn(List.of(row));
        Logger logger = (Logger) LogUtil.getLogger(JdbcChannelConfigProvider.class);
        var events = new ListAppender<ILoggingEvent>();
        events.start();
        logger.addAppender(events);
        try {
            var result = new JdbcChannelConfigProvider(mapper)
                    .findEnabledByServiceCode("ALERT")
                    .getFirst();
            assertThat(result.channelConfig()).isEmpty();
            assertThat(result.fallbackProviders()).isEmpty();
            assertThat(events.list).hasSize(2).allSatisfy(event -> {
                assertThat(event.getFormattedMessage()).doesNotContain("synthetic-private-marker");
                assertThat(event.getThrowableProxy()).isNull();
            });
        } finally {
            logger.detachAppender(events);
            events.stop();
        }
    }

    @Test
    void receiverRecordsKeepIndependentSuccessAndFailureStates() {
        var mapper = mock(NotificationRecordRowMapper.class);
        var request = NotificationRequest.of("ALERT", List.of("ok", "failed", "absent"), Map.of());
        var config = new ChannelConfig("EMAIL", "smtp", List.of(), null, Map.of());
        var result = new RecordHandler.SendAttemptResult(
                "smtp",
                true,
                1,
                2,
                "rendered",
                Map.of("ok", true, "failed", false),
                Map.of("failed", "synthetic failure"),
                List.of());
        new JdbcRecordHandler(mapper, clock).onResult(request, config, result);
        var rows = ArgumentCaptor.forClass(NotificationRecordRow.class);
        verify(mapper, times(3)).insert(rows.capture());
        assertThat(rows.getAllValues())
                .extracting(NotificationRecordRow::getStatus)
                .containsExactly("SUCCESS", "FAILED", "FAILED");
        assertThat(rows.getAllValues())
                .extracting(NotificationRecordRow::getErrorMessage)
                .containsExactly(null, "synthetic failure", null);
        assertThat(rows.getAllValues()).allSatisfy(row -> {
            assertThat(row.getContent()).isEqualTo("rendered");
            assertThat(row.getId()).isNotNull();
            assertThat(row.getSendTime()).isEqualTo(java.time.LocalDateTime.of(2026, 1, 1, 0, 0));
            assertThat(row.getCreatedAt()).isEqualTo(row.getSendTime());
            assertThat(row.getUpdatedAt()).isEqualTo(row.getSendTime());
        });
    }

    @Test
    void webhookRecordsUseGroupDeliveryResultForMentionedReceivers() {
        var mapper = mock(NotificationRecordRowMapper.class);
        var handler = new JdbcRecordHandler(mapper, clock);
        var request = NotificationRequest.of("ALERT", List.of("mentioned-user"), Map.of());
        var config = new ChannelConfig("WEBHOOK", "dingtalk", List.of(), null, Map.of());
        for (boolean success : new boolean[] {true, false}) {
            handler.onResult(
                    request,
                    config,
                    new RecordHandler.SendAttemptResult(
                            "dingtalk",
                            success,
                            success ? 1 : 0,
                            success ? 0 : 1,
                            "body",
                            Map.of("dingtalk", success),
                            Map.of(),
                            List.of()));
        }
        var rows = ArgumentCaptor.forClass(NotificationRecordRow.class);
        verify(mapper, times(2)).insert(rows.capture());
        assertThat(rows.getAllValues())
                .extracting(NotificationRecordRow::getStatus)
                .containsExactly("SUCCESS", "FAILED");
    }
}
