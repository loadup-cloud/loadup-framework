package io.github.loadup.modules.config.app.service;

/*-
 * #%L
 * Loadup Modules Config App
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import io.github.loadup.modules.config.client.command.ConfigItemCreateCommand;
import io.github.loadup.modules.config.client.command.ConfigItemUpdateCommand;
import io.github.loadup.modules.config.client.dto.ConfigItemDTO;
import io.github.loadup.modules.config.domain.enums.ChangeType;
import io.github.loadup.modules.config.domain.enums.ValueType;
import io.github.loadup.modules.config.domain.event.ConfigChangedEvent;
import io.github.loadup.modules.config.domain.gateway.ConfigHistoryGateway;
import io.github.loadup.modules.config.domain.gateway.ConfigItemGateway;
import io.github.loadup.modules.config.domain.model.ConfigHistory;
import io.github.loadup.modules.config.domain.model.ConfigItem;
import io.github.loadup.modules.config.infrastructure.cache.ConfigLocalCache;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * Application service for system configuration items.
 *
 * <p>Exposed to Gateway via {@code bean://configItemService:method}, no Controller needed.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConfigItemService {

    private final ConfigItemGateway gateway;
    private final ConfigHistoryGateway historyGateway;
    private final ConfigLocalCache localCache;
    private final ApplicationEventPublisher eventPublisher;

    /* ───────────── Queries ───────────── */

    public List<ConfigItemDTO> listAll() {
        return gateway.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<ConfigItemDTO> listByCategory(String category) {
        Assert.hasText(category, "category must not be blank");
        return gateway.findByCategory(category).stream().map(this::toDTO).collect(Collectors.toList());
    }

    public ConfigItemDTO getByKey(String configKey) {
        Assert.hasText(configKey, "configKey must not be blank");
        return gateway.findByKey(configKey).map(this::toDTO).orElse(null);
    }

    public String getValue(String configKey) {
        return gateway.findByKey(configKey).map(ConfigItem::getConfigValue).orElse(null);
    }

    @SuppressWarnings("unchecked")
    public <T> T getTypedValue(String configKey, Class<T> targetType, T defaultValue) {
        Optional<ConfigItem> opt = gateway.findByKey(configKey);
        if (opt.isEmpty() || opt.get().getConfigValue() == null) {
            return defaultValue;
        }
        String raw = opt.get().getConfigValue();
        try {
            Object v;
            if (targetType == String.class) v = raw;
            else if (targetType == Integer.class) v = Integer.valueOf(raw);
            else if (targetType == Long.class) v = Long.valueOf(raw);
            else if (targetType == Double.class) v = Double.valueOf(raw);
            else if (targetType == Boolean.class) v = Boolean.valueOf(raw);
            else {
                log.warn("Unsupported target type {} for key={}", targetType, configKey);
                return defaultValue;
            }
            return (T) v;
        } catch (Exception e) {
            log.warn("Failed to convert config value: key={}, raw={}, type={}", configKey, raw, targetType, e);
            return defaultValue;
        }
    }

    /* ───────────── Commands ───────────── */

    @Transactional(rollbackFor = Exception.class)
    public String create(@Valid ConfigItemCreateCommand cmd) {
        Assert.isTrue(!gateway.existsByKey(cmd.getConfigKey()), "Config key already exists: " + cmd.getConfigKey());
        ValueType.of(cmd.getValueType());

        LocalDateTime now = LocalDateTime.now();
        ConfigItem item = ConfigItem.builder()
                .id(UUID.randomUUID().toString().replace("-", ""))
                .configKey(cmd.getConfigKey())
                .configValue(cmd.getConfigValue())
                .valueType(cmd.getValueType().toUpperCase())
                .category(cmd.getCategory())
                .description(cmd.getDescription())
                .editable(Boolean.TRUE.equals(cmd.getEditable()))
                .encrypted(Boolean.TRUE.equals(cmd.getEncrypted()))
                .systemDefined(false)
                .sortOrder(cmd.getSortOrder() == null ? 0 : cmd.getSortOrder())
                .enabled(true)
                .createdAt(now)
                .updatedAt(now)
                .build();

        gateway.save(item);
        recordHistory(cmd.getConfigKey(), null, cmd.getConfigValue(), ChangeType.CREATE);
        publishEvent(cmd.getConfigKey(), cmd.getConfigValue(), ChangeType.CREATE);
        log.info("Config item created: key={}", cmd.getConfigKey());
        return item.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(@Valid ConfigItemUpdateCommand cmd) {
        ConfigItem existing = gateway.findByKey(cmd.getConfigKey())
                .orElseThrow(() -> new IllegalArgumentException("Config key not found: " + cmd.getConfigKey()));
        Assert.isTrue(Boolean.TRUE.equals(existing.getEditable()), "Config key is not editable: " + cmd.getConfigKey());
        String oldValue = existing.getConfigValue();
        existing.setConfigValue(cmd.getConfigValue());
        existing.setUpdatedAt(LocalDateTime.now());
        gateway.update(existing);
        recordHistory(cmd.getConfigKey(), oldValue, cmd.getConfigValue(), ChangeType.UPDATE);
        publishEvent(cmd.getConfigKey(), cmd.getConfigValue(), ChangeType.UPDATE);
        log.info("Config item updated: key={}", cmd.getConfigKey());
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(String configKey) {
        Assert.hasText(configKey, "configKey must not be blank");
        ConfigItem existing = gateway.findByKey(configKey)
                .orElseThrow(() -> new IllegalArgumentException("Config key not found: " + configKey));
        Assert.isTrue(
                !Boolean.TRUE.equals(existing.getSystemDefined()), "Cannot delete system-defined config: " + configKey);
        gateway.deleteByKey(configKey);
        recordHistory(configKey, existing.getConfigValue(), null, ChangeType.DELETE);
        publishEvent(configKey, null, ChangeType.DELETE);
        log.info("Config item deleted: key={}", configKey);
    }

    public void refreshCache() {
        localCache.evictAllConfigs();
        log.info("Config cache refreshed");
    }

    /* ───────────── History & Events ───────────── */

    private void recordHistory(String configKey, String oldValue, String newValue, ChangeType changeType) {
        ConfigHistory history = ConfigHistory.builder()
                .id(UUID.randomUUID().toString().replace("-", ""))
                .configKey(configKey)
                .oldValue(oldValue)
                .newValue(newValue)
                .changeType(changeType)
                .operator("system")
                .createdAt(LocalDateTime.now())
                .build();
        historyGateway.save(history);
    }

    private void publishEvent(String configKey, String newValue, ChangeType changeType) {
        eventPublisher.publishEvent(new ConfigChangedEvent(this, configKey, newValue, changeType, "system"));
    }

    /* ───────────── Converter ───────────── */

    private ConfigItemDTO toDTO(ConfigItem item) {
        ConfigItemDTO dto = new ConfigItemDTO();
        dto.setId(item.getId());
        dto.setConfigKey(item.getConfigKey());
        dto.setConfigValue(Boolean.TRUE.equals(item.getEncrypted()) ? "******" : item.getConfigValue());
        dto.setValueType(item.getValueType());
        dto.setCategory(item.getCategory());
        dto.setDescription(item.getDescription());
        dto.setEditable(item.getEditable());
        dto.setEncrypted(item.getEncrypted());
        dto.setSystemDefined(item.getSystemDefined());
        dto.setSortOrder(item.getSortOrder());
        dto.setEnabled(item.getEnabled());
        dto.setUpdatedAt(item.getUpdatedAt());
        return dto;
    }
}
