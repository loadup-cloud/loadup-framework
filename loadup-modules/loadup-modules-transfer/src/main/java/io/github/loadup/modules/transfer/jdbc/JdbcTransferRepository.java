package io.github.loadup.modules.transfer.jdbc;

import io.github.loadup.modules.transfer.TransferKind;
import io.github.loadup.modules.transfer.TransferPage;
import io.github.loadup.modules.transfer.TransferRepository;
import io.github.loadup.modules.transfer.TransferStatus;
import io.github.loadup.modules.transfer.TransferTask;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

/** MySQL metadata store; task options are persisted separately from JobRunr payloads. */
public class JdbcTransferRepository implements TransferRepository {
    private static final String COLUMNS = "id, tenant_id, owner_id, kind, handler_key, source_file_id, "
            + "result_file_id, status, processed_count, total_count, error_message, created_at, started_at, finished_at";
    private final JdbcTemplate jdbc;

    public JdbcTransferRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override
    @Transactional
    public void insert(TransferTask task, Map<String, String> options) {
        jdbc.update("INSERT INTO transfer_task (" + COLUMNS + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                task.id(), task.tenantId(), task.ownerId(), task.kind().name(), task.handlerKey(),
                task.sourceFileId(), task.resultFileId(), task.status().name(), task.processedCount(),
                task.totalCount(), task.errorMessage(), Timestamp.valueOf(task.createdAt()), null, null);
        options.forEach((key, value) -> jdbc.update("INSERT INTO transfer_task_option "
                + "(task_id, option_name, option_value) VALUES (?, ?, ?)", task.id(), key, value));
    }

    @Override
    public Optional<TransferTask> find(String id) {
        return jdbc.query("SELECT " + COLUMNS + " FROM transfer_task WHERE id = ?",
                JdbcTransferRepository::task, id).stream().findFirst();
    }

    @Override
    public TransferPage<TransferTask> list(String tenantId, String ownerId, int page, int size) {
        Long total = jdbc.queryForObject("SELECT COUNT(*) FROM transfer_task WHERE tenant_id = ? AND owner_id = ?",
                Long.class, tenantId, ownerId);
        List<TransferTask> records = jdbc.query("SELECT " + COLUMNS
                + " FROM transfer_task WHERE tenant_id = ? AND owner_id = ?"
                + " ORDER BY created_at DESC, id DESC LIMIT ? OFFSET ?",
                JdbcTransferRepository::task, tenantId, ownerId, size, (long) (page - 1) * size);
        return new TransferPage<>(records, total == null ? 0 : total, page, size);
    }

    @Override
    public Map<String, String> options(String id) {
        Map<String, String> result = new LinkedHashMap<>();
        jdbc.query("SELECT option_name, option_value FROM transfer_task_option WHERE task_id = ? ORDER BY option_name",
                rs -> { result.put(rs.getString(1), rs.getString(2)); }, id);
        return Map.copyOf(result);
    }

    @Override
    public boolean start(String id) {
        return jdbc.update("UPDATE transfer_task SET status = 'RUNNING', "
                + "started_at = CURRENT_TIMESTAMP(6), error_message = NULL "
                + "WHERE id = ? AND status IN ('QUEUED', 'RUNNING')", id) > 0;
    }

    @Override
    public void progress(String id, long processed, long total) {
        jdbc.update("UPDATE transfer_task SET processed_count = GREATEST(processed_count, ?), "
                + "total_count = GREATEST(total_count, ?) WHERE id = ? AND status = 'RUNNING'",
                processed, total, id);
    }

    @Override
    public void succeed(String id, String resultFileId) {
        int changed = jdbc.update("UPDATE transfer_task SET status = 'SUCCEEDED', result_file_id = ?, "
                + "finished_at = CURRENT_TIMESTAMP(6), error_message = NULL WHERE id = ? AND status = 'RUNNING'",
                resultFileId, id);
        if (changed != 1) throw new IllegalStateException("transfer task is no longer running");
    }

    @Override
    public void fail(String id, String message) {
        jdbc.update("UPDATE transfer_task SET status = 'FAILED', error_message = ?, "
                + "finished_at = CURRENT_TIMESTAMP(6) WHERE id = ? AND status IN ('QUEUED', 'RUNNING')",
                message, id);
    }

    @Override
    public boolean requeue(String id) {
        return jdbc.update("UPDATE transfer_task SET status = 'QUEUED', processed_count = 0, total_count = 0, "
                + "result_file_id = NULL, error_message = NULL, started_at = NULL, finished_at = NULL "
                + "WHERE id = ? AND status = 'FAILED'", id) == 1;
    }

    private static TransferTask task(ResultSet rs, int row) throws SQLException {
        Timestamp started = rs.getTimestamp("started_at");
        Timestamp finished = rs.getTimestamp("finished_at");
        return new TransferTask(rs.getString("id"), rs.getString("tenant_id"), rs.getString("owner_id"),
                TransferKind.valueOf(rs.getString("kind")), rs.getString("handler_key"),
                rs.getString("source_file_id"), rs.getString("result_file_id"),
                TransferStatus.valueOf(rs.getString("status")), rs.getLong("processed_count"),
                rs.getLong("total_count"), rs.getString("error_message"),
                rs.getTimestamp("created_at").toLocalDateTime(),
                started == null ? null : started.toLocalDateTime(),
                finished == null ? null : finished.toLocalDateTime());
    }
}
