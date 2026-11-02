package io.github.loadup.components.http;

import java.util.ArrayList;
import java.util.Map;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.bind.PropertySourcesPlaceholdersResolver;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.core.env.Environment;

/** Trusted deployment defaults; explicit application-specific credential settings take precedence. */
public final class HttpClientDefaults {
    private HttpClientDefaults() {}

    public static <T> T bind(Environment environment, String prefix, String application, Class<T> type) {
        String defaults = "loadup.http.client.";
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
