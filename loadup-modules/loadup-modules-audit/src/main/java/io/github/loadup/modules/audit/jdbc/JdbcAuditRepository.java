package io.github.loadup.modules.audit.jdbc;

import io.github.loadup.modules.audit.AuditEvent;
import io.github.loadup.modules.audit.AuditPage;
import io.github.loadup.modules.audit.AuditQuery;
import java.sql.Timestamp;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/** Append-only JDBC store with tenant-scoped, bounded searches. */
public class JdbcAuditRepository {
    private static final String COLUMNS =
            "id, tenant_id, actor_id, action, http_method, request_path, outcome, trace_id, occurred_at";
    private final JdbcTemplate jdbc;
    private final NamedParameterJdbcTemplate namedJdbc;

    public JdbcAuditRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.namedJdbc = new NamedParameterJdbcTemplate(jdbc);
    }

    public void insert(AuditEvent event) {
        jdbc.update(
                "INSERT INTO audit_event (id, tenant_id, actor_id, action, http_method, request_path, outcome, "
                        + "trace_id, occurred_at, created_at, updated_at, deleted) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0)",
                event.id(),
                event.tenantId(),
                event.actorId(),
                event.action(),
                event.method(),
                event.path(),
                event.outcome(),
                event.traceId(),
                Timestamp.valueOf(event.occurredAt()),
                Timestamp.valueOf(event.occurredAt()),
                Timestamp.valueOf(event.occurredAt()));
    }

    public AuditPage search(AuditQuery query) {
        StringBuilder where = new StringBuilder(" WHERE deleted = 0");
        MapSqlParameterSource params = new MapSqlParameterSource();
        if (query.tenantId() == null) {
            where.append(" AND tenant_id IS NULL");
        } else {
            where.append(" AND tenant_id = :tenantId");
            params.addValue("tenantId", query.tenantId());
        }
        if (query.actorId() != null && !query.actorId().isBlank()) {
            where.append(" AND actor_id = :actorId");
            params.addValue("actorId", query.actorId());
        }
        if (query.action() != null && !query.action().isBlank()) {
            where.append(" AND action = :action");
            params.addValue("action", query.action());
        }
        if (query.outcome() != null && !query.outcome().isBlank()) {
            where.append(" AND outcome = :outcome");
            params.addValue("outcome", query.outcome());
        }
        if (query.from() != null) {
            where.append(" AND occurred_at >= :fromTime");
            params.addValue("fromTime", Timestamp.valueOf(query.from()));
        }
        if (query.to() != null) {
            where.append(" AND occurred_at <= :toTime");
            params.addValue("toTime", Timestamp.valueOf(query.to()));
        }
        Long total = namedJdbc.queryForObject("SELECT COUNT(*) FROM audit_event" + where, params, Long.class);
        params.addValue("limit", query.size());
        params.addValue("offset", (long) (query.page() - 1) * query.size());
        List<AuditEvent> events = namedJdbc.query(
                "SELECT " + COLUMNS + " FROM audit_event" + where
                        + " ORDER BY occurred_at DESC, id DESC LIMIT :limit OFFSET :offset",
                params,
                (rs, rowNum) -> new AuditEvent(
                        rs.getString("id"),
                        rs.getString("tenant_id"),
                        rs.getString("actor_id"),
                        rs.getString("action"),
                        rs.getString("http_method"),
                        rs.getString("request_path"),
                        rs.getString("outcome"),
                        rs.getString("trace_id"),
                        rs.getTimestamp("occurred_at").toLocalDateTime()));
        return new AuditPage(events, total == null ? 0 : total, query.page(), query.size());
    }
}
