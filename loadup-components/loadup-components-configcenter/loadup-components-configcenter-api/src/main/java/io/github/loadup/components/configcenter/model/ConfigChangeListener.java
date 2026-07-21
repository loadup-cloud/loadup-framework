package io.github.loadup.components.configcenter.model;

/*-
 * #%L
 * LoadUp ConfigCenter Components API
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

/**
 * 配置变更监听器。
 *
 * <p>调用方实现此接口并通过 {@code ConfigCenterBinding#addListener} 注册，
 * 配置中心在检测到变更时回调 {@link #onChange(ConfigChangeEvent)}。
 */
@FunctionalInterface
public interface ConfigChangeListener {

    /**
     * 配置变更回调。
     *
     * @param event 变更事件，包含 dataId / group / 新旧内容 / 变更类型
     */
    void onChange(ConfigChangeEvent event);
}
