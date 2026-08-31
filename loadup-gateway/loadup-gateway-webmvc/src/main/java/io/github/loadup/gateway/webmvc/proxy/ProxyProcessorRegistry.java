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
