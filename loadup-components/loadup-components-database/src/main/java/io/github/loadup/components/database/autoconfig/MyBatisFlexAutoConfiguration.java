package io.github.loadup.components.database.autoconfig;

/*-
 * #%L
 * loadup-components-database
 * %%
 * Copyright (C) 2022 - 2026 loadup_cloud
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

import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.core.FlexGlobalConfig;
import com.mybatisflex.core.audit.AuditManager;
import com.mybatisflex.core.audit.ConsoleMessageCollector;
import com.mybatisflex.core.audit.MessageCollector;
import com.mybatisflex.core.keygen.KeyGenerators;
import com.mybatisflex.core.logicdelete.LogicDeleteManager;
import com.mybatisflex.core.logicdelete.impl.BooleanLogicDeleteProcessor;
import com.mybatisflex.spring.boot.MyBatisFlexCustomizer;
import io.github.loadup.commons.dataobject.BaseDO;
import io.github.loadup.components.database.config.DatabaseProperties;
import io.github.loadup.components.database.listener.BaseEntityListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * MyBatis-Flex Auto Configuration
 *
 * <p>MyBatis-Flex 基础配置，包含多租户、逻辑删除、实体监听器等功能
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(DatabaseProperties.class)
@RequiredArgsConstructor
public class MyBatisFlexAutoConfiguration {

    private final DatabaseProperties databaseProperties;

    /**
     * Configure MyBatis-Flex global settings
     *
     * @return MyBatisFlexCustomizer
     */
    @Bean
    public MyBatisFlexCustomizer myBatisFlexCustomizer() {
        return globalConfig -> {
            FlexGlobalConfig.KeyConfig keyConfig = new FlexGlobalConfig.KeyConfig();
            keyConfig.setKeyType(KeyType.Generator);
            keyConfig.setValue(KeyGenerators.flexId);
            keyConfig.setBefore(true);
            globalConfig.setKeyConfig(keyConfig);

            // Register entity listener
            globalConfig.registerInsertListener(new BaseEntityListener(databaseProperties), BaseDO.class);
            globalConfig.registerUpdateListener(new BaseEntityListener(databaseProperties), BaseDO.class);
            log.info("Registered BaseEntityListener for automatic field population");

            if (databaseProperties.getLogicalDelete().isEnabled()) {
                // Configure logical delete
                globalConfig.setLogicDeleteColumn("deleted");
                LogicDeleteManager.setProcessor(new BooleanLogicDeleteProcessor());
            }
            // Enable SQL audit in development mode (optional)
            MyBatisFlexAutoConfiguration.this.enableSqlAuditIfNeeded();
        };
    }

    /**
     * Enable SQL audit for debugging (logs all SQL statements)
     *
     * <p>This is useful for development and debugging. In production, consider using more
     * sophisticated monitoring tools.
     */
    private void enableSqlAuditIfNeeded() {
        // Enable audit in non-production environments
        String env = System.getProperty("spring.profiles.active", "");
        if (env.contains("dev") || env.contains("local")) {
            MessageCollector collector = new ConsoleMessageCollector();
            AuditManager.setMessageCollector(collector);
            AuditManager.setAuditEnable(true);
            log.info("Enabled SQL audit with ConsoleMessageCollector (dev mode)");
        }
    }

    // MyBatis-Flex 会自动扫描 Mapper 接口并配置
}
