package io.github.loadup.components.gotone.store.config;

import com.mybatisflex.core.query.QueryWrapper;
import io.github.loadup.components.gotone.config.ServiceConfigProvider;
import io.github.loadup.components.gotone.store.dataobject.NotificationServiceDO;
import io.github.loadup.components.gotone.store.mapper.NotificationServiceDOMapper;
import java.util.Optional;

/**
 * JDBC-backed {@link ServiceConfigProvider} backed by {@code gotone_notification_service}.
 */
public class JdbcServiceConfigProvider implements ServiceConfigProvider {

    private final NotificationServiceDOMapper mapper;

    public JdbcServiceConfigProvider(NotificationServiceDOMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Optional<ServiceConfig> findByServiceCode(String serviceCode) {
        NotificationServiceDO entity = mapper.selectOneByQuery(QueryWrapper.create()
                .where(NotificationServiceDO::getServiceCode)
                .eq(serviceCode)
                .and(NotificationServiceDO::isEnabled)
                .eq(true));
        if (entity == null) {
            return Optional.empty();
        }
        return Optional.of(new ServiceConfig(entity.getServiceCode(), entity.getServiceName(), entity.isEnabled()));
    }
}
