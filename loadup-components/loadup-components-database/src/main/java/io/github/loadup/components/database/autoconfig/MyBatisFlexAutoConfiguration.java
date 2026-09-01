/*-
 * #%L
 * loadup-components-database
 * %%
 * Copyright (C) 2022 - 2026 loadup_cloud
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

package io.github.loadup.components.database.autoconfig;

import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.core.FlexGlobalConfig;
import com.mybatisflex.core.keygen.KeyGeneratorFactory;
import com.mybatisflex.core.logicdelete.LogicDeleteManager;
import com.mybatisflex.core.logicdelete.impl.DefaultLogicDeleteProcessor;
import com.mybatisflex.core.tenant.TenantManager;
import com.mybatisflex.spring.boot.MyBatisFlexCustomizer;
import io.github.loadup.commons.dataobject.BaseDO;
import io.github.loadup.components.database.config.DatabaseProperties;
import io.github.loadup.components.database.id.DatabaseIdGenerator;
import io.github.loadup.components.database.id.IdGenerator;
import io.github.loadup.components.database.listener.BaseEntityListener;
import io.github.loadup.components.database.listener.TenantContextMissingException;
import io.github.loadup.components.database.tenant.TenantContextHolder;
import java.time.Clock;
import java.util.Locale;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/** Configures MyBatis-Flex with LoadUp persistence conventions. */
@AutoConfiguration
@EnableConfigurationProperties(DatabaseProperties.class)
public class MyBatisFlexAutoConfiguration {
    private static final Logger log = LoggerFactory.getLogger(MyBatisFlexAutoConfiguration.class);

    private final DatabaseProperties databaseProperties;

    @Bean
    @ConditionalOnMissingBean
    public Clock databaseClock() {
        return Clock.systemUTC();
    }

    @Bean
    @ConditionalOnMissingBean
    public IdGenerator databaseIdGenerator(DatabaseProperties properties) {
        return new DatabaseIdGenerator(properties.getIdGenerator());
    }

    @Bean
    public MyBatisFlexCustomizer myBatisFlexCustomizer(IdGenerator idGenerator, Clock clock) {
        return globalConfig -> {
            configureIdGeneration(globalConfig);
            configureLogicalDelete(globalConfig);
            configureMultiTenant(globalConfig);

            BaseEntityListener listener = new BaseEntityListener(databaseProperties, idGenerator, clock);
            globalConfig.registerInsertListener(listener, BaseDO.class);
            globalConfig.registerUpdateListener(listener, BaseDO.class);
            log.info("Configured MyBatis-Flex persistence conventions");
        };
    }

    private void configureIdGeneration(FlexGlobalConfig globalConfig) {
        DatabaseProperties.IdGenerator properties = databaseProperties.getIdGenerator();
        if (properties.isEnabled()) {
            FlexGlobalConfig.KeyConfig keyConfig = new FlexGlobalConfig.KeyConfig();
            keyConfig.setKeyType(KeyType.Generator);
            keyConfig.setValue(DatabaseIdGenerator.KEY);
            keyConfig.setBefore(true);
            globalConfig.setKeyConfig(keyConfig);
            KeyGeneratorFactory.register(DatabaseIdGenerator.KEY, new DatabaseIdGenerator(properties));
        } else {
            globalConfig.setKeyConfig(null);
        }
    }

    private void configureLogicalDelete(FlexGlobalConfig globalConfig) {
        DatabaseProperties.LogicalDelete properties = databaseProperties.getLogicalDelete();
        if (properties.isEnabled()) {
            globalConfig.setLogicDeleteColumn(properties.getColumnName());
            globalConfig.setNormalValueOfLogicDelete(properties.getNormalValue());
            globalConfig.setDeletedValueOfLogicDelete(properties.getDeletedValue());
            LogicDeleteManager.setProcessor(new DefaultLogicDeleteProcessor());
        } else {
            globalConfig.setLogicDeleteColumn(null);
        }
    }

    private void configureMultiTenant(FlexGlobalConfig globalConfig) {
        DatabaseProperties.MultiTenant properties = databaseProperties.getMultiTenant();
        if (!properties.isEnabled()) {
            globalConfig.setTenantColumn(null);
            TenantManager.setTenantFactory(null);
            return;
        }

        Set<String> ignoredTables = properties.getIgnoreTables().stream()
                .filter(table -> table != null && !table.isBlank())
                .map(table -> table.trim().toLowerCase(Locale.ROOT))
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
        globalConfig.setTenantColumn(properties.getColumnName());
        TenantManager.setTenantFactory(new com.mybatisflex.core.tenant.TenantFactory() {
            @Override
            @SuppressWarnings("deprecation")
            public Object[] getTenantIds() {
                return getTenantIds(null);
            }

            @Override
            public Object[] getTenantIds(String tableName) {
                if (tableName != null && ignoredTables.contains(tableName.toLowerCase(Locale.ROOT))) {
                    return null;
                }
                String tenantId = TenantContextHolder.getTenantId();
                if (!org.springframework.util.StringUtils.hasText(tenantId)) {
                    tenantId = properties.getDefaultTenantId();
                }
                if (!org.springframework.util.StringUtils.hasText(tenantId)) {
                    if (properties.isRequired()) {
                        throw new TenantContextMissingException(tableName);
                    }
                    return null;
                }
                return new Object[] {tenantId};
            }
        });
    }

    // MyBatis-Flex scans mapper interfaces through its starter.

    public MyBatisFlexAutoConfiguration(DatabaseProperties databaseProperties) {
        this.databaseProperties = databaseProperties;
    }
}
