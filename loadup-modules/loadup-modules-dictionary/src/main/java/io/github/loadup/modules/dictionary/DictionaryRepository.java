package io.github.loadup.modules.dictionary;

import java.util.List;
import java.util.Optional;

/** Persistence contract; tenant scope is mandatory on every operation. */
public interface DictionaryRepository {
    Optional<DictionaryType> findTypeById(String tenantId, String id);

    Optional<DictionaryType> findTypeByCode(String tenantId, String code);

    void insertType(DictionaryType type);

    void updateType(DictionaryType type);

    void deleteType(String tenantId, String id);

    DictionaryPage<DictionaryType> listTypes(String tenantId, int page, int size);

    Optional<DictionaryItem> findItemById(String tenantId, String id);

    void insertItem(DictionaryItem item);

    void updateItem(DictionaryItem item);

    void deleteItem(String tenantId, String id);

    long countItems(String tenantId, String typeId);

    DictionaryPage<DictionaryItem> listItems(String tenantId, String typeId, int page, int size);

    List<DictionaryItem> listEnabledItems(String tenantId, String typeId);
}
