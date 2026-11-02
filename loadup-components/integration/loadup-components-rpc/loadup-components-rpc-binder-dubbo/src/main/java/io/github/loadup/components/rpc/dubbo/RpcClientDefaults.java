/* Copyright 2026 LoadUp Cloud. Licensed under the Apache License, Version 2.0. */
package io.github.loadup.components.rpc.dubbo;

import java.util.ArrayList;
import java.util.Map;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.bind.PropertySourcesPlaceholdersResolver;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.core.env.Environment;

/** Shared caller defaults; explicit settings at the supplied application/API prefix take precedence. */
public final class RpcClientDefaults {
    private RpcClientDefaults() {}

    public static <T> T bind(Environment environment, String prefix, String application, Class<T> type) {
        String defaults = "loadup.rpc.client.";
        var fallback = new MapConfigurationPropertySource(Map.of(
                prefix + ".tenant-id", environment.getProperty(defaults + "tenant-id", "__default__"),
                prefix + ".service-id", environment.getProperty(defaults + "service-id",
                        environment.getProperty("spring.application.name", "application")),
                prefix + ".audience", environment.getProperty(defaults + "audience", application),
                prefix + ".token-file", environment.getProperty(defaults + "token-file",
                        "/var/run/loadup/secrets/" + application + ".jwt")));
        var sources = new ArrayList<ConfigurationPropertySource>();
        ConfigurationPropertySources.get(environment).forEach(sources::add);
        sources.add(fallback);
        return new Binder(sources, new PropertySourcesPlaceholdersResolver(environment))
                .bind(prefix, Bindable.of(type)).get();
    }
}
