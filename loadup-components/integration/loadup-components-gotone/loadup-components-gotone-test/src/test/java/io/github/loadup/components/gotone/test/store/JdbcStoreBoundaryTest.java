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
import static org.mockito.Mockito.*;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import io.github.loadup.commons.log.LogUtil;
import io.github.loadup.components.gotone.config.ChannelConfigProvider.ChannelConfig;
import io.github.loadup.components.gotone.model.NotificationRequest;
import io.github.loadup.components.gotone.record.RecordHandler;
import io.github.loadup.components.gotone.store.config.JdbcChannelConfigProvider;
import io.github.loadup.components.gotone.store.config.JdbcRecordHandler;
import io.github.loadup.components.gotone.store.config.JdbcServiceConfigProvider;
import io.github.loadup.components.gotone.store.dataobject.NotificationRecordRow;
import io.github.loadup.components.gotone.store.dataobject.ServiceChannelRow;
import io.github.loadup.components.gotone.store.repository.NotificationServiceRepository;
import io.github.loadup.components.gotone.store.repository.ServiceChannelRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Sort;

class JdbcStoreBoundaryTest {
    private final io.github.loadup.components.database.tenant.DatabaseTenant tenant =
            new io.github.loadup.components.database.tenant.DatabaseTenant(
                    new io.github.loadup.components.database.config.DatabaseProperties());
    private final java.time.Clock clock =
            java.time.Clock.fixed(java.time.Instant.parse("2026-01-01T00:00:00Z"), java.time.ZoneOffset.UTC);

    @Test
    void missingServiceAndChannelRowsReturnEmptyResults() {
        var services = mock(NotificationServiceRepository.class);
        var channels = mock(ServiceChannelRepository.class);
        when(services.findByServiceCode("missing")).thenReturn(Optional.empty());
        when(channels.findEnabledByServiceCode(eq("missing"), any(Sort.class))).thenReturn(List.of());
        assertThat(new JdbcServiceConfigProvider(services, tenant).findByServiceCode("missing"))
                .isEmpty();
        assertThat(new JdbcChannelConfigProvider(channels, tenant).findEnabledByServiceCode("missing"))
                .isEmpty();
        verify(services).findByServiceCode("missing");
        verify(channels).findEnabledByServiceCode(eq("missing"), any(Sort.class));
    }

    @Test
    void nullBlankAndValidJsonSettingsAreMappedThroughThePublicProvider() {
        var mapper = mock(ServiceChannelRepository.class);
        var provider = new JdbcChannelConfigProvider(mapper, tenant);
        for (String raw : new String[] {null, " ", "null"}) {
            var row = channel(raw, raw);
            when(mapper.findEnabledByServiceCode(eq("ALERT"), any(Sort.class))).thenReturn(List.of(row));
            var actual = provider.findEnabledByServiceCode("ALERT").getFirst();
            assertThat(actual.channelConfig()).isEmpty();
            assertThat(actual.fallbackProviders()).isEmpty();
        }
        when(mapper.findEnabledByServiceCode(eq("ALERT"), any(Sort.class)))
                .thenReturn(List.of(channel("{\"limit\":42}", "[\"secondary\"]")));
        var actual = provider.findEnabledByServiceCode("ALERT").getFirst();
        assertThat(actual.channelConfig()).containsEntry("limit", 42);
        assertThat(actual.fallbackProviders()).containsExactly("secondary");
    }

    @Test
    void malformedJsonUsesEmptySettingsAndNeverLogsPayloadOrExceptionChain() {
        var mapper = mock(ServiceChannelRepository.class);
        var row = channel("{\"secret\":\"synthetic-private-marker\"", "[\"synthetic-private-marker\"");
        when(mapper.findEnabledByServiceCode(eq("ALERT"), any(Sort.class))).thenReturn(List.of(row));
        Logger logger = (Logger) LogUtil.getLogger(JdbcChannelConfigProvider.class);
        var events = new ListAppender<ILoggingEvent>();
        events.start();
        logger.addAppender(events);
        try {
            var result = new JdbcChannelConfigProvider(mapper, tenant)
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
        var mapper = mock(org.springframework.data.jdbc.core.JdbcAggregateTemplate.class);
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
        new JdbcRecordHandler(mapper, tenant, clock).onResult(request, config, result);
        var rows = ArgumentCaptor.forClass(NotificationRecordRow.class);
        verify(mapper, times(3)).insert(rows.capture());
        assertThat(rows.getAllValues())
                .extracting(NotificationRecordRow::status)
                .containsExactly("SUCCESS", "FAILED", "FAILED");
        assertThat(rows.getAllValues())
                .extracting(NotificationRecordRow::errorMessage)
                .containsExactly(null, "synthetic failure", null);
        assertThat(rows.getAllValues()).allSatisfy(row -> {
            assertThat(row.content()).isEqualTo("rendered");
            assertThat(row.tenantId()).isEqualTo("__default__");
            assertThat(row.id()).isNotNull();
            assertThat(row.sendTime()).isEqualTo(java.time.LocalDateTime.of(2026, 1, 1, 0, 0));
            assertThat(row.createdAt()).isEqualTo(row.sendTime());
            assertThat(row.updatedAt()).isEqualTo(row.sendTime());
        });
    }

    @Test
    void webhookRecordsUseGroupDeliveryResultForMentionedReceivers() {
        var mapper = mock(org.springframework.data.jdbc.core.JdbcAggregateTemplate.class);
        var handler = new JdbcRecordHandler(mapper, tenant, clock);
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
                .extracting(NotificationRecordRow::status)
                .containsExactly("SUCCESS", "FAILED");
    }

    private static ServiceChannelRow channel(String config, String fallback) {
        var now = java.time.LocalDateTime.of(2026, 1, 1, 0, 0);
        return new ServiceChannelRow(
                "id", "__default__", "ALERT", "EMAIL", "template", config, "smtp", fallback, true, 0, now, now, false);
    }
}
