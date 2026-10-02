package io.github.loadup.modules.audit;

import java.util.List;

/** Bounded page of audit events. */
public record AuditPage(List<AuditEvent> events, long total, int page, int size) {}
