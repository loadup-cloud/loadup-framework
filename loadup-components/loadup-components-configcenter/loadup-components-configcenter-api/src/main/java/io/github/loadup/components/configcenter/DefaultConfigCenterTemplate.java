package io.github.loadup.components.configcenter;

/*-
 * #%L
 * LoadUp ConfigCenter Components API
 * %%
 * Copyright (C) 2025 - 2026 loadup_cloud
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

import java.util.List;
import java.util.function.Consumer;

public class DefaultConfigCenterTemplate implements ConfigCenterTemplate {
    private final ConfigCenterProvider provider;

    public DefaultConfigCenterTemplate(ConfigCenterProvider provider) {
        this.provider = provider;
    }

    @Override public String getConfig(String key) { return provider.getConfig(key); }
    @Override public String getConfig(String key, String defaultValue) {
        String val = provider.getConfig(key);
        return val != null ? val : defaultValue;
    }
    @Override public boolean setConfig(String key, String value) { return provider.setConfig(key, value); }
    @Override public boolean removeConfig(String key) { return provider.removeConfig(key); }
    @Override public List<String> listKeys(String prefix) { return provider.listKeys(prefix); }
    @Override public void addListener(String key, Consumer<String> listener) { provider.addListener(key, listener); }
    @Override public void removeListener(String key) { provider.removeListener(key); }
}
