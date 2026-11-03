/*
 * Copyright (C) 2025-2026 LoadUp Cloud
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

public final class HttpClientDefaults {
    private HttpClientDefaults() {}

    public static <T> T bind(Environment environment, String prefix, String application, Class<T> type) {
        String defaults = "loadup.http.client.";
        var fallback = new MapConfigurationPropertySource(Map.of(
                prefix + ".tenant-id", environment.getProperty(defaults + "tenant-id", "__default__"),
                prefix + ".service-id",
                        environment.getProperty(
                                defaults + "service-id",
                                environment.getProperty("spring.application.name", "application")),
                prefix + ".audience", environment.getProperty(defaults + "audience", application)));
        var sources = new ArrayList<ConfigurationPropertySource>();
        ConfigurationPropertySources.get(environment).forEach(sources::add);
        sources.add(fallback);
        return new Binder(sources, new PropertySourcesPlaceholdersResolver(environment))
                .bind(prefix, Bindable.of(type))
                .get();
    }
}
