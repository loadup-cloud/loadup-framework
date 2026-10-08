package io.github.loadup.components.gotone.config;

import java.util.Optional;

public interface ServiceConfigProvider {
    Optional<ServiceConfig> findByServiceCode(String serviceCode);

    record ServiceConfig(String serviceCode, String serviceName, boolean enabled) {}
}
