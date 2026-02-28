package io.github.loadup.modules.config.domain.gateway;

import io.github.loadup.modules.config.domain.model.ConfigHistory;
import java.util.List;

public interface ConfigHistoryGateway {

    void save(ConfigHistory history);

    List<ConfigHistory> findByKey(String configKey);
}

