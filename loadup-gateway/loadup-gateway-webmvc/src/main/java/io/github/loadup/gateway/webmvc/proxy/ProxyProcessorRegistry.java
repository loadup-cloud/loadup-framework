/*-
 * #%L
 * Loadup Gateway WebMVC Engine
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
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
 * #L%
 */
package io.github.loadup.gateway.webmvc.proxy;

import io.github.loadup.gateway.facade.spi.ProxyProcessor;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registry of {@link ProxyProcessor} implementations keyed by supported protocol (HTTP / BEAN / RPC).
 */
public class ProxyProcessorRegistry {
    private static final Logger log = LoggerFactory.getLogger(ProxyProcessorRegistry.class);

    private final Map<String, ProxyProcessor> processors = new ConcurrentHashMap<>();

    public ProxyProcessorRegistry(List<ProxyProcessor> proxyProcessors) {
        if (proxyProcessors != null) {
            for (ProxyProcessor processor : proxyProcessors) {
                String protocol = processor.getSupportedProtocol() == null
                        ? ""
                        : processor.getSupportedProtocol().toUpperCase(Locale.ROOT);
                if (processors.containsKey(protocol)) {
                    log.warn(
                            "Duplicate proxy processor for protocol '{}': keeping '{}'",
                            protocol,
                            processors.get(protocol).getName());
                } else {
                    processors.put(protocol, processor);
                    log.info("Registered proxy processor '{}' for protocol '{}'", processor.getName(), protocol);
                }
            }
        }
    }

    public ProxyProcessor get(String protocol) {
        if (protocol == null) {
            return null;
        }
        return processors.get(protocol.toUpperCase(Locale.ROOT));
    }
}
