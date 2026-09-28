package io.github.loadup.components.gotone.store.config;

import io.github.loadup.components.database.autoconfig.MyBatisFlexAutoConfiguration;
import io.github.loadup.components.gotone.config.ChannelConfigProvider;
import io.github.loadup.components.gotone.config.ServiceConfigProvider;
import io.github.loadup.components.gotone.record.RecordHandler;
import io.github.loadup.components.gotone.store.mapper.NotificationRecordDOMapper;
import io.github.loadup.components.gotone.store.mapper.NotificationServiceDOMapper;
import io.github.loadup.components.gotone.store.mapper.ServiceChannelDOMapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for the default JDBC store of the gotone component.
 *
 * <p>Each storage SPI is only registered when no custom implementation is present, so integrators
 * can swap the JDBC store for their own {@link ServiceConfigProvider} / {@link ChannelConfigProvider}
 * / {@link RecordHandler} beans without touching the engine.
 */
@AutoConfiguration(after = MyBatisFlexAutoConfiguration.class)
@ConditionalOnClass({ChannelConfigProvider.class, MyBatisFlexAutoConfiguration.class})
@MapperScan("io.github.loadup.components.gotone.store.mapper")
public class GotoneStoreJdbcAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ServiceConfigProvider serviceConfigProvider(NotificationServiceDOMapper mapper) {
        return new JdbcServiceConfigProvider(mapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public ChannelConfigProvider channelConfigProvider(ServiceChannelDOMapper mapper) {
        return new JdbcChannelConfigProvider(mapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public RecordHandler recordHandler(NotificationRecordDOMapper mapper) {
        return new JdbcRecordHandler(mapper);
    }
}
