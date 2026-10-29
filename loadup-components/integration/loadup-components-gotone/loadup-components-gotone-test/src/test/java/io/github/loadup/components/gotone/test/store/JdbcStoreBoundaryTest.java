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
import io.github.loadup.components.gotone.store.dataobject.ServiceChannelDO;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class JdbcStoreBoundaryTest {
    private final io.github.loadup.components.database.tenant.DatabaseTenant tenant =
            new io.github.loadup.components.database.tenant.DatabaseTenant(
                    new io.github.loadup.components.database.config.DatabaseProperties());
    private final java.time.Clock clock = java.time.Clock.systemDefaultZone();

    @Test
    void missingServiceAndChannelRowsReturnEmptyResults() {
        var services = mock(org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate.class);
        var channels = mock(org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate.class);
        when(services.query(
                        any(String.class),
                        any(org.springframework.jdbc.core.namedparam.SqlParameterSource.class),
                        org.mockito.ArgumentMatchers
                                .<org.springframework.jdbc.core.RowMapper<
                                                io.github.loadup.components.gotone.store.dataobject
                                                        .NotificationServiceDO>>
                                        any()))
                .thenReturn(List.of());
        when(channels.query(
                        any(String.class),
                        any(org.springframework.jdbc.core.namedparam.SqlParameterSource.class),
                        org.mockito.ArgumentMatchers.<org.springframework.jdbc.core.RowMapper<ServiceChannelDO>>any()))
                .thenReturn(List.of());
        assertThat(new JdbcServiceConfigProvider(services, tenant).findByServiceCode("missing"))
                .isEmpty();
        assertThat(new JdbcChannelConfigProvider(channels, tenant).findEnabledByServiceCode("missing"))
                .isEmpty();
    }

    @Test
    void nullBlankAndValidJsonSettingsAreMappedThroughThePublicProvider() {
        var mapper = mock(org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate.class);
        var provider = new JdbcChannelConfigProvider(mapper, tenant);
        for (String raw : new String[] {null, " ", "null"}) {
            var row = channel(raw, raw);
            when(mapper.query(
                            any(String.class),
                            any(org.springframework.jdbc.core.namedparam.SqlParameterSource.class),
                            org.mockito.ArgumentMatchers
                                    .<org.springframework.jdbc.core.RowMapper<ServiceChannelDO>>any()))
                    .thenReturn(List.of(row));
            var actual = provider.findEnabledByServiceCode("ALERT").getFirst();
            assertThat(actual.channelConfig()).isEmpty();
            assertThat(actual.fallbackProviders()).isEmpty();
        }
        when(mapper.query(
                        any(String.class),
                        any(org.springframework.jdbc.core.namedparam.SqlParameterSource.class),
                        org.mockito.ArgumentMatchers.<org.springframework.jdbc.core.RowMapper<ServiceChannelDO>>any()))
                .thenReturn(List.of(channel("{\"limit\":42}", "[\"secondary\"]")));
        var actual = provider.findEnabledByServiceCode("ALERT").getFirst();
        assertThat(actual.channelConfig()).containsEntry("limit", 42);
        assertThat(actual.fallbackProviders()).containsExactly("secondary");
    }

    @Test
    void malformedJsonUsesEmptySettingsAndNeverLogsPayloadOrExceptionChain() {
        var mapper = mock(org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate.class);
        var row = channel("{\"secret\":\"synthetic-private-marker\"", "[\"synthetic-private-marker\"");
        when(mapper.query(
                        any(String.class),
                        any(org.springframework.jdbc.core.namedparam.SqlParameterSource.class),
                        org.mockito.ArgumentMatchers.<org.springframework.jdbc.core.RowMapper<ServiceChannelDO>>any()))
                .thenReturn(List.of(row));
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
        var mapper = mock(org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate.class);
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
        var rows = ArgumentCaptor.forClass(org.springframework.jdbc.core.namedparam.SqlParameterSource.class);
        verify(mapper, times(3)).update(any(String.class), rows.capture());
        assertThat(rows.getAllValues())
                .extracting(row -> row.getValue("status"))
                .containsExactly("SUCCESS", "FAILED", "FAILED");
        assertThat(rows.getAllValues())
                .extracting(row -> row.getValue("errorMessage"))
                .containsExactly(null, "synthetic failure", null);
        assertThat(rows.getAllValues()).allSatisfy(row -> {
            assertThat(row.getValue("content")).isEqualTo("rendered");
            assertThat(row.getValue("sendTime")).isNotNull();
        });
    }

    @Test
    void webhookRecordsUseGroupDeliveryResultForMentionedReceivers() {
        var mapper = mock(org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate.class);
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
        var rows = ArgumentCaptor.forClass(org.springframework.jdbc.core.namedparam.SqlParameterSource.class);
        verify(mapper, times(2)).update(any(String.class), rows.capture());
        assertThat(rows.getAllValues())
                .extracting(row -> row.getValue("status"))
                .containsExactly("SUCCESS", "FAILED");
    }

    private static ServiceChannelDO channel(String config, String fallback) {
        var row = new ServiceChannelDO();
        row.setChannel("EMAIL");
        row.setProvider("smtp");
        row.setTemplateContent("template");
        row.setChannelConfig(config);
        row.setFallbackProviders(fallback);
        return row;
    }
}
