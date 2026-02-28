package io.github.loadup.modules.config.infrastructure.cache;

/*-
 * #%L
 * Loadup Modules Config Infrastructure
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * #L%
 */

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.loadup.modules.config.domain.model.ConfigItem;
import io.github.loadup.modules.config.domain.model.DictItem;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Local (Caffeine) cache for config items and dictionary items.
 *
 * <p>Config items are cached by key. Dict items are cached by dictCode.
 * Entries expire after 5 minutes since last write; the max sizes are conservative
 * because config data is typically small.
 *
 * @author LoadUp Framework
 */
@Slf4j
@Component
public class ConfigLocalCache {

    private static final int CONFIG_MAX_SIZE = 2_000;
    private static final int DICT_MAX_SIZE = 500;
    private static final Duration TTL = Duration.ofMinutes(5);

    private final Cache<String, ConfigItem> configCache =
            Caffeine.newBuilder().maximumSize(CONFIG_MAX_SIZE).expireAfterWrite(TTL).build();

    /** Key: dictCode, Value: list of enabled items sorted by sortOrder. */
    private final Cache<String, List<DictItem>> dictCache =
            Caffeine.newBuilder().maximumSize(DICT_MAX_SIZE).expireAfterWrite(TTL).build();

    /* ---------- config ---------- */

    public Optional<ConfigItem> getConfig(String key) {
        return Optional.ofNullable(configCache.getIfPresent(key));
    }

    public void putConfig(ConfigItem item) {
        configCache.put(item.getConfigKey(), item);
    }

    public void evictConfig(String key) {
        configCache.invalidate(key);
        log.debug("Config cache evicted: key={}", key);
    }

    public void evictAllConfigs() {
        configCache.invalidateAll();
        log.info("Config cache cleared");
    }

    /* ---------- dict ---------- */

    public Optional<List<DictItem>> getDictItems(String dictCode) {
        return Optional.ofNullable(dictCache.getIfPresent(dictCode));
    }

    public void putDictItems(String dictCode, List<DictItem> items) {
        dictCache.put(dictCode, items);
    }

    public void evictDict(String dictCode) {
        dictCache.invalidate(dictCode);
        log.debug("Dict cache evicted: dictCode={}", dictCode);
    }

    public void evictAllDicts() {
        dictCache.invalidateAll();
        log.info("Dict cache cleared");
    }
}
