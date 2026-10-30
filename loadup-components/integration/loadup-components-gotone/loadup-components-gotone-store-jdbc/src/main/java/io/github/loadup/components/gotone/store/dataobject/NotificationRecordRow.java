/* Copyright 2026 LoadUp Cloud. Licensed under the Apache License, Version 2.0. */
package io.github.loadup.components.gotone.store.dataobject;

import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("gotone_notification_record")
public record NotificationRecordRow(
        @Id @Column("id") String id,
        @Column("tenant_id") String tenantId,
        @Column("service_code") String serviceCode,
        @Column("trace_id") String traceId,
        @Column("request_id") String requestId,
        @Column("channel") String channel,
        @Column("provider") String provider,
        @Column("receiver") String receiver,
        @Column("content") String content,
        @Column("status") String status,
        @Column("error_message") String errorMessage,
        @Column("send_time") LocalDateTime sendTime,
        @Column("created_at") LocalDateTime createdAt,
        @Column("updated_at") LocalDateTime updatedAt,
        @Column("deleted") boolean deleted) {}
