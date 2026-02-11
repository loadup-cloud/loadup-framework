package io.github.loadup.components.dfs.properties;

/*-
 * #%L
 * LoadUp Components DFS Starter
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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@Getter
@Setter
@ConfigurationProperties(prefix = "loadup.dfs")
public class DfsGroupProperties {

    /**
     * 绑定的存储类型
     */
    private BinderType defaultBinder = BinderType.LOCAL;

    /**
     * 启用的绑定器列表
     */
    private List<BinderType> enabledBinders;

    /**
     * 文件类型与存储绑定的映射
     */
    private Map<String, BindingConfig> bindings;

    /**
     * 各种存储类型的配置
     */
    @NestedConfigurationProperty
    @JsonDeserialize(using = MapBinderConverter.class)
    private Map<BinderType, BinderConfig> binders;
}
