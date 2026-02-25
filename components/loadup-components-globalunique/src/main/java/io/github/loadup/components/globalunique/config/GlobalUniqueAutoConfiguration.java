package io.github.loadup.components.globalunique.config;

import io.github.loadup.components.globalunique.properties.GlobalUniqueProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

/**
 * GlobalUnique 自动配置
 *
 * @author loadup
 */
@Slf4j
@AutoConfiguration
@ConditionalOnProperty(prefix = "loadup.components.globalunique", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(GlobalUniqueProperties.class)
@ComponentScan(basePackages = "io.github.loadup.components.globalunique")
public class GlobalUniqueAutoConfiguration {

    public GlobalUniqueAutoConfiguration(GlobalUniqueProperties properties) {
        log.info("LoadUp GlobalUnique 组件已启用: dbType={}, tablePrefix={}, tableName={}",
                properties.getDbType(), properties.getTablePrefix(), properties.getTableName());
    }
}

