package io.github.loadup.gateway.test.unit;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.loadup.gateway.core.security.SecurityStrategyManager;
import io.github.loadup.gateway.facade.context.GatewayContext;
import io.github.loadup.gateway.facade.spi.SecurityStrategy;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("SecurityStrategyManager")
class SecurityStrategyManagerTest {

    @Nested
    @DisplayName("strategy registration")
    class Registration {

        @Test
        @DisplayName("registers custom strategy by code")
        void registersCustomStrategy() {
            SecurityStrategy custom = createStrategy("custom");
            SecurityStrategyManager mgr = new SecurityStrategyManager(List.of(custom));
            assertThat(mgr.getStrategy("custom")).isNotNull();
        }

        @Test
        @DisplayName("returns null for unknown code")
        void returnsNullForUnknown() {
            SecurityStrategyManager mgr = new SecurityStrategyManager(Collections.emptyList());
            assertThat(mgr.getStrategy("unknown")).isNull();
        }

        @Test
        @DisplayName("OFF strategy auto-registered when not provided")
        void offStrategyAutoRegistered() {
            SecurityStrategyManager mgr = new SecurityStrategyManager(Collections.emptyList());
            assertThat(mgr.getStrategy("OFF")).isNotNull();
            assertThat(mgr.getStrategy("OFF").getCode()).isEqualTo("OFF");
        }

        @Test
        @DisplayName("OFF strategy not duplicated if provided")
        void offStrategyNotDuplicated() {
            SecurityStrategy off = createStrategy("OFF");
            SecurityStrategyManager mgr = new SecurityStrategyManager(List.of(off));
            assertThat(mgr.getStrategy("OFF")).isNotNull();
        }

        @Test
        @DisplayName("null strategy list is handled")
        void nullStrategyList() {
            SecurityStrategyManager mgr = new SecurityStrategyManager(null);
            assertThat(mgr.getStrategy("OFF")).isNotNull();
        }

        @Test
        @DisplayName("multiple strategies registered")
        void multipleStrategies() {
            SecurityStrategyManager mgr =
                    new SecurityStrategyManager(List.of(createStrategy("a"), createStrategy("b"), createStrategy("c")));
            assertThat(mgr.getStrategy("a")).isNotNull();
            assertThat(mgr.getStrategy("b")).isNotNull();
            assertThat(mgr.getStrategy("c")).isNotNull();
        }
    }

    @Nested
    @DisplayName("OFF strategy behavior")
    class OffStrategy {

        @Test
        @DisplayName("OFF strategy process does nothing")
        void offStrategyProcessDoesNothing() {
            SecurityStrategyManager mgr = new SecurityStrategyManager(null);
            SecurityStrategy off = mgr.getStrategy("OFF");
            GatewayContext ctx = new GatewayContext();
            off.process(ctx);
        }
    }

    private SecurityStrategy createStrategy(String code) {
        return new SecurityStrategy() {
            @Override
            public String getCode() {
                return code;
            }

            @Override
            public void process(GatewayContext context) {}
        };
    }
}
