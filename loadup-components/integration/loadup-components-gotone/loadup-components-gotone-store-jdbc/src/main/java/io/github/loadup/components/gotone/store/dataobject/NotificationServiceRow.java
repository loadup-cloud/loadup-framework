/* Copyright 2026 LoadUp Cloud. Licensed under the Apache License, Version 2.0. */
package io.github.loadup.components.gotone.store.dataobject;

import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("gotone_notification_service")
public record NotificationServiceRow(
        @Id @Column("id") String id,
        @Column("tenant_id") String tenantId,
        @Column("service_code") String serviceCode,
        @Column("service_name") String serviceName,
        @Column("description") String description,
        @Column("enabled") boolean enabled,
        @Column("priority") int priority,
        @Column("created_at") LocalDateTime createdAt,
        @Column("updated_at") LocalDateTime updatedAt,
        @Column("deleted") boolean deleted) {}
