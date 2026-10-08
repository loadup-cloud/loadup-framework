package io.github.loadup.components.globalunique.autoconfig;

import io.github.loadup.components.database.autoconfig.MyBatisFlexAutoConfiguration;
import io.github.loadup.components.database.config.DatabaseProperties;
import io.github.loadup.components.globalunique.DefaultGlobalUniqueTemplate;
import io.github.loadup.components.globalunique.GlobalUniqueProperties;
import io.github.loadup.components.globalunique.GlobalUniqueTemplate;
import io.github.loadup.components.globalunique.mapper.GlobalUniqueMapper;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/** Configures the database-backed global unique template. */
@AutoConfiguration(after = MyBatisFlexAutoConfiguration.class)
@ConditionalOnProperty(prefix = "loadup.global-unique", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(GlobalUniqueProperties.class)
public class GlobalUniqueAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(GlobalUniqueTemplate.class)
    public GlobalUniqueTemplate globalUniqueTemplate(GlobalUniqueMapper mapper, DatabaseProperties databaseProperties) {
        return new DefaultGlobalUniqueTemplate(mapper, databaseProperties);
    }
}
