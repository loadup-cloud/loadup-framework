package io.github.loadup.modules.config.domain.gateway;

/*-
 * #%L
 * Loadup Modules Config Domain
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * #L%
 */

import io.github.loadup.modules.config.domain.model.ConfigItem;
import java.util.List;
import java.util.Optional;

/**
 * Gateway interface for config item persistence.
 *
 * @author LoadUp Framework
 */
public interface ConfigItemGateway {

    Optional<ConfigItem> findByKey(String configKey);

    List<ConfigItem> findByCategory(String category);

    List<ConfigItem> findAll();

    void save(ConfigItem item);

    void update(ConfigItem item);

    void deleteByKey(String configKey);

    boolean existsByKey(String configKey);
}
