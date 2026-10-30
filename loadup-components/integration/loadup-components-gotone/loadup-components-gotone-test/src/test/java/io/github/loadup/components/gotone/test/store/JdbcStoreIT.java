/* Copyright 2026 LoadUp Cloud. Licensed under the Apache License, Version 2.0. */
package io.github.loadup.components.gotone.test.store;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.loadup.commons.util.TenantUtil;
import io.github.loadup.components.gotone.config.ChannelConfigProvider;
import io.github.loadup.components.gotone.config.ServiceConfigProvider;
import io.github.loadup.components.gotone.model.NotificationRequest;
import io.github.loadup.components.gotone.record.RecordHandler;
import io.github.loadup.components.gotone.test.TestGotoneApplication;
import io.github.loadup.components.testcontainers.annotation.ContainerType;
import io.github.loadup.components.testcontainers.annotation.EnableTestContainers;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = TestGotoneApplication.class)
@EnableTestContainers(value = ContainerType.POSTGRESQL, reuse = false)
@ActiveProfiles("test")
@TestPropertySource(
        properties = {"loadup.database.multi-tenant.enabled=true", "loadup.database.multi-tenant.required=true"})
class JdbcStoreIT {
    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    ServiceConfigProvider services;

    @Autowired
    ChannelConfigProvider channels;

    @Autowired
    RecordHandler records;

    @Test
    void readsTenantScopedConfigurationAndPersistsDeliveryRecord() {
        String code = "IT_" + UUID.randomUUID().toString().replace("-", "");
        String tenantA = "tenant-a";
        String tenantB = "tenant-b";
        jdbc.update(
                "INSERT INTO gotone_notification_service (id, tenant_id, service_code, service_name, enabled, created_at, updated_at) "
                        + "VALUES (?, ?, ?, ?, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                UUID.randomUUID().toString(),
                tenantA,
                code,
                "A");
        jdbc.update(
                "INSERT INTO gotone_notification_service (id, tenant_id, service_code, service_name, enabled, created_at, updated_at) "
                        + "VALUES (?, ?, ?, ?, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                UUID.randomUUID().toString(),
                tenantB,
                code,
                "B");
        jdbc.update(
                "INSERT INTO gotone_service_channel (id, tenant_id, service_code, channel, provider, enabled, priority, created_at, updated_at) "
                        + "VALUES (?, ?, ?, ?, ?, TRUE, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                UUID.randomUUID().toString(),
                tenantA,
                code,
                "EMAIL",
                "smtp",
                10);
        jdbc.update(
                "INSERT INTO gotone_service_channel (id, tenant_id, service_code, channel, provider, enabled, priority, created_at, updated_at) "
                        + "VALUES (?, ?, ?, ?, ?, TRUE, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                UUID.randomUUID().toString(),
                tenantA,
                code,
                "SMS",
                "sms",
                1);
        TenantUtil.runWithTenant(tenantA, () -> {
            assertThat(services.findByServiceCode(code).orElseThrow().serviceName())
                    .isEqualTo("A");
            var found = channels.findEnabledByServiceCode(code);
            assertThat(found)
                    .extracting(ChannelConfigProvider.ChannelConfig::channel)
                    .containsExactly("SMS", "EMAIL");
            var request = NotificationRequest.of(code, List.of("recipient"), Map.of());
            records.onResult(
                    request,
                    found.getLast(),
                    new RecordHandler.SendAttemptResult(
                            "smtp", true, 1, 0, "body", Map.of("recipient", true), Map.of(), List.of()));
        });
        TenantUtil.runWithTenant(tenantB, () -> {
            assertThat(services.findByServiceCode(code).orElseThrow().serviceName())
                    .isEqualTo("B");
            assertThat(channels.findEnabledByServiceCode(code)).isEmpty();
        });
        assertThat(jdbc.queryForObject(
                        "SELECT count(*) FROM gotone_notification_record WHERE tenant_id = ? AND service_code = ? AND status = 'SUCCESS'",
                        Long.class,
                        tenantA,
                        code))
                .isEqualTo(1L);
    }
}
