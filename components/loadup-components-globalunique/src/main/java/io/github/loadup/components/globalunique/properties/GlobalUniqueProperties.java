package io.github.loadup.components.globalunique.properties;

/*-
 * #%L
 * LoadUp Components :: Global Unique
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

import io.github.loadup.components.globalunique.enums.DbType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Global Unique 配置属性
 *
 * @author loadup
 */
@Data
@Validated
@ConfigurationProperties(prefix = "loadup.components.globalunique")
public class GlobalUniqueProperties {

    /**
     * 是否启用全局唯一性控制组件
     */
    private boolean enabled = true;

    /**
     * 数据库类型
     */
    @NotNull
    private DbType dbType = DbType.MYSQL;

    /**
     * 表名前缀（默认为空，允许空字符串）
     * <p>例如：设置为 "t_" 则表名为 "t_global_unique"</p>
     */
    @NotNull
    private String tablePrefix = "";

    /**
     * 表名（默认为 "global_unique"）
     */
    @NotBlank
    private String tableName = "global_unique";

    /**
     * 获取完整表名（带前缀）
     *
     * @return 完整表名
     */
    public String getFullTableName() {
        return tablePrefix + tableName;
    }
}
