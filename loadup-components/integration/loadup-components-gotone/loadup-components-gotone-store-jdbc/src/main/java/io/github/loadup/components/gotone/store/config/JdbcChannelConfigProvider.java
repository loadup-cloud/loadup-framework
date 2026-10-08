package io.github.loadup.components.gotone.store.config;

import com.mybatisflex.core.query.QueryWrapper;
import io.github.loadup.commons.log.LogUtil;
import io.github.loadup.components.gotone.config.ChannelConfigProvider;
import io.github.loadup.components.gotone.store.dataobject.ServiceChannelDO;
import io.github.loadup.components.gotone.store.mapper.ServiceChannelDOMapper;
import java.util.List;
import java.util.Map;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

/**
 * JDBC-backed {@link ChannelConfigProvider} backed by {@code gotone_service_channel}.
 */
public class JdbcChannelConfigProvider implements ChannelConfigProvider {

    private final ServiceChannelDOMapper mapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JdbcChannelConfigProvider(ServiceChannelDOMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<ChannelConfig> findEnabledByServiceCode(String serviceCode) {
        List<ServiceChannelDO> entities = mapper.selectListByQuery(QueryWrapper.create()
                .where(ServiceChannelDO::getServiceCode)
                .eq(serviceCode)
                .and(ServiceChannelDO::isEnabled)
                .eq(true)
                .orderBy(ServiceChannelDO::getPriority)
                .asc());
        if (entities == null) {
            return List.of();
        }
        return entities.stream().map(this::toConfig).toList();
    }

    private ChannelConfig toConfig(ServiceChannelDO entity) {
        return new ChannelConfig(
                entity.getChannel(),
                entity.getProvider(),
                parseJsonList(entity.getFallbackProviders()),
                entity.getTemplateContent(),
                parseJsonMap(entity.getChannelConfig()));
    }

    private Map<String, Object> parseJsonMap(String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(
                    json, objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class));
        } catch (JacksonException e) {
            LogUtil.warn(JdbcChannelConfigProvider.class, "Failed to parse channelConfig JSON, using empty config", e);
            return Map.of();
        }
    }

    private List<String> parseJsonList(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(
                    json, objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
        } catch (JacksonException e) {
            LogUtil.warn(
                    JdbcChannelConfigProvider.class, "Failed to parse fallbackProviders JSON, using empty list", e);
            return List.of();
        }
    }
}
