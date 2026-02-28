package io.github.loadup.modules.config.app.service;

// ...existing code...

import io.github.loadup.modules.config.infrastructure.cache.ConfigLocalCache;
import io.github.loadup.modules.config.client.command.DictItemCreateCommand;
import io.github.loadup.modules.config.client.command.DictTypeCreateCommand;
import io.github.loadup.modules.config.client.dto.DictItemDTO;
import io.github.loadup.modules.config.client.dto.DictTypeDTO;
import io.github.loadup.modules.config.domain.gateway.DictGateway;
import io.github.loadup.modules.config.domain.model.DictItem;
import io.github.loadup.modules.config.domain.model.DictType;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * Application service for data dictionary management.
 *
 * <p>Exposed to Gateway via {@code bean://dictService:method}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DictService {

    private final DictGateway dictGateway;
    private final ConfigLocalCache localCache;

    /* ───────────── Queries ───────────── */

    public List<DictTypeDTO> listAllTypes() {
        return dictGateway.findAllTypes().stream().map(this::toTypeDTO).collect(Collectors.toList());
    }

    public List<DictItemDTO> getDictData(String dictCode) {
        Assert.hasText(dictCode, "dictCode must not be blank");
        return dictGateway.findItemsByCode(dictCode).stream().map(this::toItemDTO).collect(Collectors.toList());
    }

    public List<DictItemDTO> getCascadeData(String dictCode, String parentValue) {
        Assert.hasText(dictCode, "dictCode must not be blank");
        Assert.hasText(parentValue, "parentValue must not be blank");
        return dictGateway.findItemsByCodeAndParent(dictCode, parentValue)
                .stream().map(this::toItemDTO).collect(Collectors.toList());
    }

    public String getDictLabel(String dictCode, String itemValue) {
        return getDictData(dictCode).stream()
                .filter(d -> itemValue.equals(d.getItemValue()))
                .map(DictItemDTO::getItemLabel)
                .findFirst().orElse(null);
    }

    /* ───────────── Commands ───────────── */

    @Transactional(rollbackFor = Exception.class)
    public String createType(@Valid DictTypeCreateCommand cmd) {
        Assert.isTrue(!dictGateway.existsTypeByCode(cmd.getDictCode()),
                "Dict code already exists: " + cmd.getDictCode());
        LocalDateTime now = LocalDateTime.now();
        DictType type = DictType.builder()
                .id(UUID.randomUUID().toString().replace("-", ""))
                .dictCode(cmd.getDictCode()).dictName(cmd.getDictName())
                .description(cmd.getDescription()).systemDefined(false)
                .sortOrder(cmd.getSortOrder() == null ? 0 : cmd.getSortOrder())
                .enabled(true).createdAt(now).updatedAt(now)
                .build();
        dictGateway.saveType(type);
        log.info("Dict type created: code={}", cmd.getDictCode());
        return type.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteType(String dictCode) {
        Assert.hasText(dictCode, "dictCode must not be blank");
        DictType type = dictGateway.findTypeByCode(dictCode)
                .orElseThrow(() -> new IllegalArgumentException("Dict code not found: " + dictCode));
        Assert.isTrue(!Boolean.TRUE.equals(type.getSystemDefined()),
                "Cannot delete system-defined dict: " + dictCode);
        dictGateway.deleteItemsByCode(dictCode);
        dictGateway.deleteTypeByCode(dictCode);
        log.info("Dict type deleted: code={}", dictCode);
    }

    @Transactional(rollbackFor = Exception.class)
    public String createItem(@Valid DictItemCreateCommand cmd) {
        Assert.isTrue(dictGateway.existsTypeByCode(cmd.getDictCode()),
                "Dict code does not exist: " + cmd.getDictCode());
        LocalDateTime now = LocalDateTime.now();
        DictItem item = DictItem.builder()
                .id(UUID.randomUUID().toString().replace("-", ""))
                .dictCode(cmd.getDictCode()).itemLabel(cmd.getItemLabel())
                .itemValue(cmd.getItemValue()).parentValue(cmd.getParentValue())
                .cssClass(cmd.getCssClass())
                .sortOrder(cmd.getSortOrder() == null ? 0 : cmd.getSortOrder())
                .enabled(true).createdAt(now).updatedAt(now)
                .build();
        dictGateway.saveItem(item);
        localCache.evictDict(cmd.getDictCode());
        log.info("Dict item created: code={}, value={}", cmd.getDictCode(), cmd.getItemValue());
        return item.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteItem(String id) {
        Assert.hasText(id, "id must not be blank");
        dictGateway.deleteItemById(id);
    }

    /* ───────────── Converters ───────────── */

    private DictTypeDTO toTypeDTO(DictType t) {
        DictTypeDTO dto = new DictTypeDTO();
        dto.setId(t.getId()); dto.setDictCode(t.getDictCode()); dto.setDictName(t.getDictName());
        dto.setDescription(t.getDescription()); dto.setSystemDefined(t.getSystemDefined());
        dto.setSortOrder(t.getSortOrder()); dto.setEnabled(t.getEnabled());
        dto.setUpdatedAt(t.getUpdatedAt());
        return dto;
    }

    private DictItemDTO toItemDTO(DictItem i) {
        DictItemDTO dto = new DictItemDTO();
        dto.setId(i.getId()); dto.setDictCode(i.getDictCode()); dto.setItemLabel(i.getItemLabel());
        dto.setItemValue(i.getItemValue()); dto.setParentValue(i.getParentValue());
        dto.setCssClass(i.getCssClass()); dto.setSortOrder(i.getSortOrder()); dto.setEnabled(i.getEnabled());
        return dto;
    }
}
