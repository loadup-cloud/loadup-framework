/*-
 * #%L
 * Loadup Components Database
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
package io.github.loadup.components.database.autoconfig;

import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.core.FlexGlobalConfig;
import com.mybatisflex.core.keygen.KeyGeneratorFactory;
import com.mybatisflex.core.logicdelete.LogicDeleteManager;
import com.mybatisflex.core.logicdelete.impl.DefaultLogicDeleteProcessor;
import com.mybatisflex.core.tenant.TenantManager;
import com.mybatisflex.spring.boot.MyBatisFlexCustomizer;
import io.github.loadup.commons.dataobject.BaseDO;
import io.github.loadup.commons.log.LogUtil;
import io.github.loadup.commons.util.TenantUtil;
import io.github.loadup.components.database.config.DatabaseProperties;
import io.github.loadup.components.database.id.DatabaseIdGenerator;
import io.github.loadup.components.database.id.IdGenerator;
import io.github.loadup.components.database.listener.BaseEntityListener;
import io.github.loadup.components.database.listener.TenantContextMissingException;
import java.time.Clock;
import java.util.Locale;
import java.util.Set;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/** Configures MyBatis-Flex with LoadUp persistence conventions. */
@AutoConfiguration
@EnableConfigurationProperties(DatabaseProperties.class)
public class MyBatisFlexAutoConfiguration {

    private final DatabaseProperties databaseProperties;

    @Bean
    public Clock databaseClock() {
        return Clock.systemUTC();
    }

    @Bean
    public DatabaseIdGenerator databaseIdGenerator() {
        return new DatabaseIdGenerator();
    }

    @Bean
    public MyBatisFlexCustomizer myBatisFlexCustomizer(
            DatabaseIdGenerator idGenerator,
            @org.springframework.beans.factory.annotation.Qualifier("databaseClock") Clock clock) {
        return globalConfig -> {
            configureIdGeneration(globalConfig, idGenerator);
            configureLogicalDelete(globalConfig);
            configureMultiTenant(globalConfig);

            BaseEntityListener listener = new BaseEntityListener(databaseProperties, idGenerator, clock);
            globalConfig.registerInsertListener(listener, BaseDO.class);
            globalConfig.registerUpdateListener(listener, BaseDO.class);
            LogUtil.info(MyBatisFlexAutoConfiguration.class, "Configured MyBatis-Flex persistence conventions");
        };
    }

    @Bean
    public static BeanFactoryPostProcessor persistenceConventionGuard(
            org.springframework.core.env.Environment environment) {
        return factory -> {
            var binder = org.springframework.boot.context.properties.bind.Binder.get(environment);
            for (String prefix : new String[] {
                "loadup.database.audit",
                "loadup.database.id-generator",
                "loadup.database.logical-delete",
                "mybatis-flex.global-config"
            }) {
                if (binder.bind(
                                prefix,
                                org.springframework.boot.context.properties.bind.Bindable.mapOf(
                                        String.class, Object.class))
                        .isBound()) {
                    throw new IllegalStateException(
                            "Framework-owned persistence configuration cannot be overridden: " + prefix);
                }
            }
            if (environment.containsProperty("loadup.database.multi-tenant.column-name")) {
                throw new IllegalStateException("The tenant column is fixed to tenant_id");
            }

            String[] customizers = factory.getBeanNamesForType(MyBatisFlexCustomizer.class, true, false);
            if (customizers.length != 1 || !customizers[0].equals("myBatisFlexCustomizer")) {
                throw new IllegalStateException(
                        "LoadUp owns MyBatis-Flex defaults; remove application MyBatisFlexCustomizer beans");
            }
        };
    }

    private void configureIdGeneration(FlexGlobalConfig globalConfig, IdGenerator idGenerator) {
        FlexGlobalConfig.KeyConfig keyConfig = new FlexGlobalConfig.KeyConfig();
        keyConfig.setKeyType(KeyType.Generator);
        keyConfig.setValue(DatabaseIdGenerator.KEY);
        keyConfig.setBefore(true);
        globalConfig.setKeyConfig(keyConfig);
        KeyGeneratorFactory.register(DatabaseIdGenerator.KEY, (entity, keyColumn) -> idGenerator.generate());
    }

    private void configureLogicalDelete(FlexGlobalConfig globalConfig) {
        globalConfig.setLogicDeleteColumn("deleted");
        globalConfig.setNormalValueOfLogicDelete(0);
        globalConfig.setDeletedValueOfLogicDelete(1);
        LogicDeleteManager.setProcessor(new DefaultLogicDeleteProcessor());
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
        globalConfig.setTenantColumn("tenant_id");
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
                String tenantId = TenantUtil.getTenantId();
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
