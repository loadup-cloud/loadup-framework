package io.github.loadup.modules.dictionary;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Transactional;

/** Dictionary administration and effective-value queries. */
public class DictionaryService {
    private static final String DEFAULT_TENANT = "__default__";
    private final DictionaryRepository repository;

    public DictionaryService(DictionaryRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public DictionaryType createType(String tenantId, TypeCreate command) {
        if (command == null) throw new IllegalArgumentException("type is required");
        String tenant = tenant(tenantId);
        String code = code(command.code());
        String name = required(command.name(), "name", 100);
        if (repository.findTypeByCode(tenant, code).isPresent()) {
            throw new IllegalArgumentException("dictionary type code already exists");
        }
        LocalDateTime now = LocalDateTime.now();
        DictionaryType type = new DictionaryType(
                UUID.randomUUID().toString(), tenant, code, name, optional(command.description(), 500),
                command.enabled() == null || command.enabled(), now, now);
        try {
            repository.insertType(type);
        } catch (DuplicateKeyException duplicate) {
            throw new IllegalArgumentException("dictionary type code already exists", duplicate);
        }
        return type;
    }

    @Transactional
    public DictionaryType updateType(String tenantId, String id, TypeUpdate command) {
        if (command == null || command.enabled() == null) {
            throw new IllegalArgumentException("type and enabled are required");
        }
        String tenant = tenant(tenantId);
        DictionaryType previous = requireType(tenant, id);
        DictionaryType updated = new DictionaryType(
                previous.id(), tenant, previous.code(), required(command.name(), "name", 100),
                optional(command.description(), 500), command.enabled(), previous.createdAt(), LocalDateTime.now());
        repository.updateType(updated);
        return updated;
    }

    @Transactional
    public void deleteType(String tenantId, String id) {
        String tenant = tenant(tenantId);
        DictionaryType type = requireType(tenant, id);
        if (repository.countItems(tenant, type.id()) != 0) {
            throw new IllegalStateException("delete dictionary items before deleting their type");
        }
        repository.deleteType(tenant, type.id());
    }

    public DictionaryPage<DictionaryType> listTypes(String tenantId, int page, int size) {
        validatePage(page, size);
        return repository.listTypes(tenant(tenantId), page, size);
    }

    @Transactional
    public DictionaryItem createItem(String tenantId, String typeCode, ItemCreate command) {
        if (command == null) throw new IllegalArgumentException("item is required");
        String tenant = tenant(tenantId);
        DictionaryType type = requireTypeByCode(tenant, typeCode);
        LocalDateTime now = LocalDateTime.now();
        DictionaryItem item = new DictionaryItem(
                UUID.randomUUID().toString(), tenant, type.id(), required(command.value(), "value", 128),
                required(command.label(), "label", 200), optional(command.description(), 500),
                command.sortOrder() == null ? 0 : command.sortOrder(),
                command.enabled() == null || command.enabled(), now, now);
        try {
            repository.insertItem(item);
        } catch (DuplicateKeyException duplicate) {
            throw new IllegalArgumentException("dictionary item value already exists in this type", duplicate);
        }
        return item;
    }

    @Transactional
    public DictionaryItem updateItem(String tenantId, String id, ItemUpdate command) {
        if (command == null || command.enabled() == null || command.sortOrder() == null) {
            throw new IllegalArgumentException("item, enabled and sortOrder are required");
        }
        String tenant = tenant(tenantId);
        DictionaryItem previous = requireItem(tenant, id);
        DictionaryItem updated = new DictionaryItem(
                previous.id(), tenant, previous.typeId(), previous.value(),
                required(command.label(), "label", 200), optional(command.description(), 500),
                command.sortOrder(), command.enabled(), previous.createdAt(), LocalDateTime.now());
        repository.updateItem(updated);
        return updated;
    }

    @Transactional
    public void deleteItem(String tenantId, String id) {
        String tenant = tenant(tenantId);
        repository.deleteItem(tenant, requireItem(tenant, id).id());
    }

    public DictionaryPage<DictionaryItem> listItems(String tenantId, String typeCode, int page, int size) {
        validatePage(page, size);
        String tenant = tenant(tenantId);
        return repository.listItems(tenant, requireTypeByCode(tenant, typeCode).id(), page, size);
    }

    public List<DictionaryItem> listEnabledItems(String tenantId, String typeCode) {
        String tenant = tenant(tenantId);
        DictionaryType type = requireTypeByCode(tenant, typeCode);
        return type.enabled() ? repository.listEnabledItems(tenant, type.id()) : List.of();
    }

    private DictionaryType requireType(String tenant, String id) {
        return repository.findTypeById(tenant, required(id, "id", 64))
                .orElseThrow(() -> new IllegalArgumentException("dictionary type not found"));
    }

    private DictionaryType requireTypeByCode(String tenant, String typeCode) {
        return repository.findTypeByCode(tenant, code(typeCode))
                .orElseThrow(() -> new IllegalArgumentException("dictionary type not found"));
    }

    private DictionaryItem requireItem(String tenant, String id) {
        return repository.findItemById(tenant, required(id, "id", 64))
                .orElseThrow(() -> new IllegalArgumentException("dictionary item not found"));
    }

    private static String tenant(String tenantId) {
        return tenantId == null || tenantId.isBlank() ? DEFAULT_TENANT : required(tenantId, "tenantId", 64);
    }

    private static String code(String code) {
        String normalized = required(code, "code", 64).toLowerCase(Locale.ROOT);
        if (!normalized.matches("[a-z][a-z0-9_.-]*")) {
            throw new IllegalArgumentException("code must start with a letter and contain only letters, digits, _, . or -");
        }
        return normalized;
    }

    private static String required(String value, String name, int maxLength) {
        if (value == null || value.isBlank() || value.trim().length() > maxLength) {
            throw new IllegalArgumentException(name + " must contain 1 to " + maxLength + " characters");
        }
        return value.trim();
    }

    private static String optional(String value, int maxLength) {
        if (value == null || value.isBlank()) return null;
        if (value.trim().length() > maxLength) {
            throw new IllegalArgumentException("description must be at most " + maxLength + " characters");
        }
        return value.trim();
    }

    private static void validatePage(int page, int size) {
        if (page < 1 || size < 1 || size > 100) {
            throw new IllegalArgumentException("page must be positive and size must be between 1 and 100");
        }
    }
}
