package io.github.loadup.modules.dictionary.jdbc;

import io.github.loadup.modules.dictionary.DictionaryItem;
import io.github.loadup.modules.dictionary.DictionaryPage;
import io.github.loadup.modules.dictionary.DictionaryRepository;
import io.github.loadup.modules.dictionary.DictionaryType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;

/** MySQL-backed dictionary persistence with tenant predicates on every query. */
public class JdbcDictionaryRepository implements DictionaryRepository {
    private static final String TYPE_COLUMNS =
            "id, tenant_id, type_code, type_name, description, enabled, created_at, updated_at";
    private static final String ITEM_COLUMNS =
            "id, tenant_id, type_id, item_value, item_label, description, sort_order, enabled, created_at, updated_at";
    private final JdbcTemplate jdbc;

    public JdbcDictionaryRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<DictionaryType> findTypeById(String tenantId, String id) {
        return jdbc.query(
                        "SELECT " + TYPE_COLUMNS + " FROM dictionary_type WHERE tenant_id = ? AND id = ? AND deleted = 0",
                        JdbcDictionaryRepository::type,
                        tenantId,
                        id)
                .stream().findFirst();
    }

    @Override
    public Optional<DictionaryType> findTypeByCode(String tenantId, String code) {
        return jdbc.query(
                        "SELECT " + TYPE_COLUMNS
                                + " FROM dictionary_type WHERE tenant_id = ? AND type_code = ? AND deleted = 0",
                        JdbcDictionaryRepository::type,
                        tenantId,
                        code)
                .stream().findFirst();
    }

    @Override
    public void insertType(DictionaryType type) {
        jdbc.update(
                "INSERT INTO dictionary_type "
                        + "(id, tenant_id, type_code, type_name, description, enabled, created_at, updated_at, deleted) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 0)",
                type.id(), type.tenantId(), type.code(), type.name(), type.description(), type.enabled(),
                Timestamp.valueOf(type.createdAt()), Timestamp.valueOf(type.updatedAt()));
    }

    @Override
    public void updateType(DictionaryType type) {
        jdbc.update(
                "UPDATE dictionary_type SET type_name = ?, description = ?, enabled = ?, updated_at = ? "
                        + "WHERE tenant_id = ? AND id = ? AND deleted = 0",
                type.name(), type.description(), type.enabled(), Timestamp.valueOf(type.updatedAt()),
                type.tenantId(), type.id());
    }

    @Override
    public void deleteType(String tenantId, String id) {
        jdbc.update("DELETE FROM dictionary_type WHERE tenant_id = ? AND id = ?", tenantId, id);
    }

    @Override
    public DictionaryPage<DictionaryType> listTypes(String tenantId, int page, int size) {
        Long total = jdbc.queryForObject(
                "SELECT COUNT(*) FROM dictionary_type WHERE tenant_id = ? AND deleted = 0", Long.class, tenantId);
        List<DictionaryType> records = jdbc.query(
                "SELECT " + TYPE_COLUMNS
                        + " FROM dictionary_type WHERE tenant_id = ? AND deleted = 0"
                        + " ORDER BY type_code LIMIT ? OFFSET ?",
                JdbcDictionaryRepository::type,
                tenantId,
                size,
                (long) (page - 1) * size);
        return new DictionaryPage<>(records, total == null ? 0 : total, page, size);
    }

    @Override
    public Optional<DictionaryItem> findItemById(String tenantId, String id) {
        return jdbc.query(
                        "SELECT " + ITEM_COLUMNS + " FROM dictionary_item WHERE tenant_id = ? AND id = ? AND deleted = 0",
                        JdbcDictionaryRepository::item,
                        tenantId,
                        id)
                .stream().findFirst();
    }

    @Override
    public void insertItem(DictionaryItem item) {
        jdbc.update(
                "INSERT INTO dictionary_item "
                        + "(id, tenant_id, type_id, item_value, item_label, description, sort_order, enabled, "
                        + "created_at, updated_at, deleted) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0)",
                item.id(), item.tenantId(), item.typeId(), item.value(), item.label(), item.description(),
                item.sortOrder(), item.enabled(), Timestamp.valueOf(item.createdAt()),
                Timestamp.valueOf(item.updatedAt()));
    }

    @Override
    public void updateItem(DictionaryItem item) {
        jdbc.update(
                "UPDATE dictionary_item SET item_label = ?, description = ?, sort_order = ?, enabled = ?, "
                        + "updated_at = ? WHERE tenant_id = ? AND id = ? AND deleted = 0",
                item.label(), item.description(), item.sortOrder(), item.enabled(),
                Timestamp.valueOf(item.updatedAt()), item.tenantId(), item.id());
    }

    @Override
    public void deleteItem(String tenantId, String id) {
        jdbc.update("DELETE FROM dictionary_item WHERE tenant_id = ? AND id = ?", tenantId, id);
    }

    @Override
    public long countItems(String tenantId, String typeId) {
        Long total = jdbc.queryForObject(
                "SELECT COUNT(*) FROM dictionary_item WHERE tenant_id = ? AND type_id = ? AND deleted = 0",
                Long.class,
                tenantId,
                typeId);
        return total == null ? 0 : total;
    }

    @Override
    public DictionaryPage<DictionaryItem> listItems(String tenantId, String typeId, int page, int size) {
        long total = countItems(tenantId, typeId);
        List<DictionaryItem> records = jdbc.query(
                "SELECT " + ITEM_COLUMNS
                        + " FROM dictionary_item WHERE tenant_id = ? AND type_id = ? AND deleted = 0"
                        + " ORDER BY sort_order, item_label, item_value LIMIT ? OFFSET ?",
                JdbcDictionaryRepository::item,
                tenantId,
                typeId,
                size,
                (long) (page - 1) * size);
        return new DictionaryPage<>(records, total, page, size);
    }

    @Override
    public List<DictionaryItem> listEnabledItems(String tenantId, String typeId) {
        return jdbc.query(
                "SELECT " + ITEM_COLUMNS
                        + " FROM dictionary_item WHERE tenant_id = ? AND type_id = ? AND enabled = 1 AND deleted = 0"
                        + " ORDER BY sort_order, item_label, item_value",
                JdbcDictionaryRepository::item,
                tenantId,
                typeId);
    }

    private static DictionaryType type(ResultSet rs, int rowNum) throws SQLException {
        return new DictionaryType(
                rs.getString("id"), rs.getString("tenant_id"), rs.getString("type_code"),
                rs.getString("type_name"), rs.getString("description"), rs.getBoolean("enabled"),
                rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getTimestamp("updated_at").toLocalDateTime());
    }

    private static DictionaryItem item(ResultSet rs, int rowNum) throws SQLException {
        return new DictionaryItem(
                rs.getString("id"), rs.getString("tenant_id"), rs.getString("type_id"),
                rs.getString("item_value"), rs.getString("item_label"), rs.getString("description"),
                rs.getInt("sort_order"), rs.getBoolean("enabled"),
                rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getTimestamp("updated_at").toLocalDateTime());
    }
}
