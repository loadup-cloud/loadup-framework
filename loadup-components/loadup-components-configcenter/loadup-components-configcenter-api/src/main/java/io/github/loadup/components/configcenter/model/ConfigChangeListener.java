package io.github.loadup.components.configcenter.model;

/*-
 * #%L
 * LoadUp ConfigCenter Components API
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
