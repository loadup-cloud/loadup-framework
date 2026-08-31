package io.github.loadup.components.globalunique.properties;

/*-
 * #%L
 * LoadUp Components :: Global Unique
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

import io.github.loadup.commons.enums.DbType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Global Unique 配置属性
 *
 * @author loadup
 */
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

    public GlobalUniqueProperties(boolean enabled, DbType dbType, String tablePrefix, String tableName) {
        this.enabled = enabled;
        this.dbType = dbType;
        this.tablePrefix = tablePrefix;
        this.tableName = tableName;
    }

    public GlobalUniqueProperties() {}

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setDbType(DbType dbType) {
        this.dbType = dbType;
    }

    public void setTablePrefix(String tablePrefix) {
        this.tablePrefix = tablePrefix;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public DbType getDbType() {
        return dbType;
    }

    public String getTablePrefix() {
        return tablePrefix;
    }

    public String getTableName() {
        return tableName;
    }
}
