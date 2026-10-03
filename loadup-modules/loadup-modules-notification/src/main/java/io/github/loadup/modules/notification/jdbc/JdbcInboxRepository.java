package io.github.loadup.modules.notification.jdbc;

import io.github.loadup.modules.notification.InboxMessage;
import io.github.loadup.modules.notification.InboxPage;
import io.github.loadup.modules.notification.InboxRepository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;

/** MySQL inbox store; every recipient operation is tenant and user scoped. */
public class JdbcInboxRepository implements InboxRepository {
    private static final String COLUMNS = "id, tenant_id, recipient_id, sender_id, category, title, body, "
            + "action_url, read_at, created_at";
    private final JdbcTemplate jdbc;

    public JdbcInboxRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override
    public int insert(InboxMessage message, String requestKey) {
        return jdbc.update("INSERT INTO notification_inbox (" + COLUMNS + ", request_key) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE id = id",
                message.id(), message.tenantId(), message.recipientId(), message.senderId(),
                message.category(), message.title(), message.body(), message.actionUrl(), null,
                Timestamp.valueOf(message.createdAt()), requestKey);
    }

    @Override
    public InboxPage list(String tenantId, String recipientId, boolean unreadOnly, int page, int size) {
        String filter = " FROM notification_inbox WHERE tenant_id = ? AND recipient_id = ? AND archived_at IS NULL"
                + (unreadOnly ? " AND read_at IS NULL" : "");
        Long total = jdbc.queryForObject("SELECT COUNT(*)" + filter, Long.class, tenantId, recipientId);
        List<InboxMessage> records = jdbc.query("SELECT " + COLUMNS + filter
                        + " ORDER BY created_at DESC, id DESC LIMIT ? OFFSET ?",
                JdbcInboxRepository::message, tenantId, recipientId, size, (long) (page - 1) * size);
        return new InboxPage(records, total == null ? 0 : total, page, size);
    }

    @Override
    public long unreadCount(String tenantId, String recipientId) {
        Long count = jdbc.queryForObject("SELECT COUNT(*) FROM notification_inbox WHERE tenant_id = ? "
                + "AND recipient_id = ? AND archived_at IS NULL AND read_at IS NULL",
                Long.class, tenantId, recipientId);
        return count == null ? 0 : count;
    }

    @Override
    public int markRead(String tenantId, String recipientId, String id) {
        int changed = jdbc.update("UPDATE notification_inbox SET read_at = CURRENT_TIMESTAMP(6) "
                + "WHERE tenant_id = ? AND recipient_id = ? AND id = ? AND archived_at IS NULL AND read_at IS NULL",
                tenantId, recipientId, id);
        if (changed > 0) return changed;
        Long count = jdbc.queryForObject("SELECT COUNT(*) FROM notification_inbox WHERE tenant_id = ? "
                + "AND recipient_id = ? AND id = ? AND archived_at IS NULL", Long.class, tenantId, recipientId, id);
        return count == null ? 0 : count.intValue();
    }

    @Override
    public int markAllRead(String tenantId, String recipientId) {
        return jdbc.update("UPDATE notification_inbox SET read_at = CURRENT_TIMESTAMP(6) "
                + "WHERE tenant_id = ? AND recipient_id = ? AND archived_at IS NULL AND read_at IS NULL",
                tenantId, recipientId);
    }

    @Override
    public int archive(String tenantId, String recipientId, String id) {
        int changed = jdbc.update("UPDATE notification_inbox SET archived_at = CURRENT_TIMESTAMP(6) "
                + "WHERE tenant_id = ? AND recipient_id = ? AND id = ? AND archived_at IS NULL",
                tenantId, recipientId, id);
        if (changed > 0) return changed;
        Long count = jdbc.queryForObject("SELECT COUNT(*) FROM notification_inbox WHERE tenant_id = ? "
                + "AND recipient_id = ? AND id = ?", Long.class, tenantId, recipientId, id);
        return count == null ? 0 : count.intValue();
    }

    private static InboxMessage message(ResultSet rs, int row) throws SQLException {
        Timestamp read = rs.getTimestamp("read_at");
        return new InboxMessage(rs.getString("id"), rs.getString("tenant_id"), rs.getString("recipient_id"),
                rs.getString("sender_id"), rs.getString("category"), rs.getString("title"),
                rs.getString("body"), rs.getString("action_url"),
                read == null ? null : read.toLocalDateTime(),
                rs.getTimestamp("created_at").toLocalDateTime());
    }
}
