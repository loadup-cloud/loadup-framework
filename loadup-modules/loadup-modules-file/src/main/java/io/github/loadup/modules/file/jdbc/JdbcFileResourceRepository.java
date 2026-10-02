package io.github.loadup.modules.file.jdbc;

import io.github.loadup.modules.file.FilePage;
import io.github.loadup.modules.file.FileReference;
import io.github.loadup.modules.file.FileResource;
import io.github.loadup.modules.file.FileResourceRepository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;

/** Tenant-scoped JDBC metadata and reference store. */
public class JdbcFileResourceRepository implements FileResourceRepository {
    private static final String COLUMNS = "id, tenant_id, owner_id, storage_id, filename, content_type, "
            + "content_length, provider, state, created_at, updated_at";
    private final JdbcTemplate jdbc;

    public JdbcFileResourceRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override
    public void insert(FileResource file) {
        jdbc.update("INSERT INTO file_resource (" + COLUMNS + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                file.id(), file.tenantId(), file.ownerId(), file.storageId(), file.filename(),
                file.contentType(), file.size(), file.provider(), file.state(),
                Timestamp.valueOf(file.createdAt()), Timestamp.valueOf(file.updatedAt()));
    }

    @Override
    public Optional<FileResource> find(String tenantId, String id) {
        return jdbc.query("SELECT " + COLUMNS + " FROM file_resource WHERE tenant_id = ? AND id = ?",
                JdbcFileResourceRepository::file, tenantId, id).stream().findFirst();
    }

    @Override
    public Optional<FileResource> lock(String tenantId, String id) {
        return jdbc.query("SELECT " + COLUMNS + " FROM file_resource WHERE tenant_id = ? AND id = ? FOR UPDATE",
                JdbcFileResourceRepository::file, tenantId, id).stream().findFirst();
    }

    @Override
    public FilePage<FileResource> list(String tenantId, String ownerId, int page, int size) {
        Long total = jdbc.queryForObject("SELECT COUNT(*) FROM file_resource WHERE tenant_id = ? AND owner_id = ? "
                + "AND state = 'ACTIVE'", Long.class, tenantId, ownerId);
        List<FileResource> records = jdbc.query("SELECT " + COLUMNS + " FROM file_resource WHERE tenant_id = ? "
                + "AND owner_id = ? AND state = 'ACTIVE' ORDER BY created_at DESC, id DESC LIMIT ? OFFSET ?",
                JdbcFileResourceRepository::file, tenantId, ownerId, size, (long) (page - 1) * size);
        return new FilePage<>(records, total == null ? 0 : total, page, size);
    }

    @Override
    public void markPending(String tenantId, String id) {
        jdbc.update("UPDATE file_resource SET state = 'PENDING_DELETE', updated_at = CURRENT_TIMESTAMP(6) "
                + "WHERE tenant_id = ? AND id = ? AND state = 'ACTIVE'", tenantId, id);
    }

    @Override
    public void markDeleted(String tenantId, String id) {
        jdbc.update("UPDATE file_resource SET state = 'DELETED', updated_at = CURRENT_TIMESTAMP(6) "
                + "WHERE tenant_id = ? AND id = ? AND state = 'PENDING_DELETE'", tenantId, id);
    }

    @Override
    public List<FileResource> pending(int limit) {
        return jdbc.query("SELECT " + COLUMNS + " FROM file_resource WHERE state = 'PENDING_DELETE' "
                + "ORDER BY updated_at, id LIMIT ?", JdbcFileResourceRepository::file, limit);
    }

    @Override
    public void addReference(FileReference reference) {
        jdbc.update("INSERT INTO file_resource_reference "
                + "(id, tenant_id, file_id, reference_type, reference_id, created_at) VALUES (?, ?, ?, ?, ?, ?)",
                reference.id(), reference.tenantId(), reference.fileId(), reference.referenceType(),
                reference.referenceId(), Timestamp.valueOf(reference.createdAt()));
    }

    @Override
    public void removeReference(String tenantId, String fileId, String referenceType, String referenceId) {
        jdbc.update("DELETE FROM file_resource_reference WHERE tenant_id = ? AND file_id = ? "
                + "AND reference_type = ? AND reference_id = ?", tenantId, fileId, referenceType, referenceId);
    }

    @Override
    public long countReferences(String tenantId, String fileId) {
        Long count = jdbc.queryForObject("SELECT COUNT(*) FROM file_resource_reference WHERE tenant_id = ? "
                + "AND file_id = ?", Long.class, tenantId, fileId);
        return count == null ? 0 : count;
    }

    @Override
    public List<FileReference> references(String tenantId, String fileId) {
        return jdbc.query("SELECT id, tenant_id, file_id, reference_type, reference_id, created_at "
                + "FROM file_resource_reference WHERE tenant_id = ? AND file_id = ? ORDER BY created_at, id",
                JdbcFileResourceRepository::reference, tenantId, fileId);
    }

    private static FileResource file(ResultSet rs, int row) throws SQLException {
        return new FileResource(rs.getString("id"), rs.getString("tenant_id"), rs.getString("owner_id"),
                rs.getString("storage_id"), rs.getString("filename"), rs.getString("content_type"),
                rs.getLong("content_length"), rs.getString("provider"), rs.getString("state"),
                rs.getTimestamp("created_at").toLocalDateTime(), rs.getTimestamp("updated_at").toLocalDateTime());
    }

    private static FileReference reference(ResultSet rs, int row) throws SQLException {
        return new FileReference(rs.getString("id"), rs.getString("tenant_id"), rs.getString("file_id"),
                rs.getString("reference_type"), rs.getString("reference_id"),
                rs.getTimestamp("created_at").toLocalDateTime());
    }
}
