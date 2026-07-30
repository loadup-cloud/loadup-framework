package io.github.loadup.gateway.test.unit;

import io.github.loadup.gateway.core.plugin.PluginManager;
import io.github.loadup.gateway.facade.model.GatewayRequest;
import io.github.loadup.gateway.facade.model.GatewayResponse;
import io.github.loadup.gateway.facade.model.RouteConfig;
import io.github.loadup.gateway.facade.spi.ProxyProcessor;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("PluginManager")
class PluginManagerTest {

    @Nested
    @DisplayName("initialization")
    class Initialization {

        @Test
        @DisplayName("does not throw on null list")
        void nullListOk() {
            PluginManager pm = new PluginManager(null);
            pm.init();
        }

        @Test
        @DisplayName("does not throw on empty list")
        void emptyListOk() {
            PluginManager pm = new PluginManager(Collections.emptyList());
            pm.init();
        }

        @Test
        @DisplayName("initializes with multiple processors")
        void multipleProcessors() {
            PluginManager pm = new PluginManager(List.of(createProcessor("HTTP"), createProcessor("BEAN")));
            pm.init();
        }
    }

    private ProxyProcessor createProcessor(String protocol) {
        return new ProxyProcessor() {
            @Override
            public String getName() {
                return protocol + "-plugin";
            }

            @Override
            public String getType() {
                return "PROXY";
            }

            @Override
            public String getVersion() {
                return "1.0";
            }

            @Override
            public int getPriority() {
                return 100;
            }

            @Override
            public void initialize() {}

            @Override
            public void destroy() {}

            @Override
            public String getSupportedProtocol() {
                return protocol;
            }

            @Override
            public GatewayResponse proxy(GatewayRequest request, RouteConfig route) {
                return null;
            }
        };
    }
}
