package io.github.loadup.components.dfs.database.autoconfig;

import io.github.loadup.components.database.autoconfig.MyBatisFlexAutoConfiguration;
import io.github.loadup.components.dfs.DfsProvider;
import io.github.loadup.components.dfs.autoconfig.DfsAutoConfiguration;
import io.github.loadup.components.dfs.database.DatabaseDfsProvider;
import io.github.loadup.components.dfs.database.mapper.FileStorageDOMapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import tools.jackson.databind.ObjectMapper;

/** Auto-configuration for the transitional database DFS binder. */
@AutoConfiguration(after = MyBatisFlexAutoConfiguration.class, before = DfsAutoConfiguration.class)
@ConditionalOnClass({FileStorageDOMapper.class, ObjectMapper.class})
@ConditionalOnProperty(prefix = "loadup.dfs", name = "binder-type", havingValue = "database")
@MapperScan("io.github.loadup.components.dfs.database.mapper")
public class DatabaseDfsAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(DfsProvider.class)
    public DfsProvider databaseDfsProvider(
            FileStorageDOMapper mapper, ObjectProvider<ObjectMapper> objectMapperProvider) {
        return new DatabaseDfsProvider(mapper, objectMapperProvider.getIfAvailable(ObjectMapper::new));
    }
}
