/*
 * #%L
 * Loadup Components Extension
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
package io.github.loadup.components.extension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.loadup.components.extension.annotation.Extension;
import io.github.loadup.components.extension.api.IExtensionPoint;
import io.github.loadup.components.extension.context.BizContextHolder;
import io.github.loadup.components.extension.core.BizScenario;
import io.github.loadup.components.extension.exector.ExtensionExecutor;
import io.github.loadup.components.extension.register.ExtensionRegistry;
import io.github.loadup.components.extension.spi.ExtensionProvider;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;

class ExtensionExecutionContractTest {
    private static BizScenario scene(String useCase, String scenario) {
        return BizScenario.valueOf("retail", useCase, scenario);
    }

    private static AnnotationConfigApplicationContext catalog() {
        var context = new AnnotationConfigApplicationContext();
        context.registerBean(ExtensionRegistry.class);
        context.registerBean(Mobile.class);
        context.registerBean(Secondary.class);
        context.registerBean(Web.class);
        context.registerBean(Default.class);
        context.registerBean(OtherImpl.class);
        context.registerBean(ExtensionProvider.class, () -> new ExtensionProvider() {
            public Class<? extends IExtensionPoint> getExtensionType() {
                return Greeting.class;
            }

            public String getBizCode() {
                return "spi";
            }

            public IExtensionPoint createExtension() {
                return (Greeting) () -> "spi";
            }
        });
        context.refresh();
        return context;
    }

    @Test
    void exactMatchWinsThenUseCaseThenBusinessAndPriorityBreaksTies() {
        try (var context = catalog()) {
            var executor = new ExtensionExecutor(context.getBean(ExtensionRegistry.class));
            assertThat(executor.execute(Greeting.class, scene("checkout", "mobile"), Greeting::text))
                    .isEqualTo("mobile");
            assertThat(executor.execute(Greeting.class, scene("checkout", "unknown"), Greeting::text))
                    .isEqualTo("mobile");
            assertThat(executor.execute(Greeting.class, scene("unknown", "unknown"), Greeting::text))
                    .isEqualTo("mobile");
            assertThatThrownBy(() -> executor.execute(Greeting.class, BizScenario.valueOf("absent"), Greeting::text))
                    .isInstanceOf(ExtensionExecutor.ExtensionNotFoundException.class);
            assertThatThrownBy(() -> executor.execute(Unregistered.class, scene("checkout", "mobile"), value -> value))
                    .isInstanceOf(ExtensionExecutor.ExtensionNotFoundException.class);
        }
    }

    @Test
    void bulkExecutionIsExactForScenarioAndFiltersOtherInterfacesForBusiness() {
        try (var context = catalog()) {
            var executor = new ExtensionExecutor(context.getBean(ExtensionRegistry.class));
            List<String> expected = List.of("mobile", "secondary");
            assertThat(executor.collect(Greeting.class, scene("checkout", "mobile"), Greeting::text))
                    .isEqualTo(expected);
            List<String> seen = new ArrayList<>();
            executor.executeAll(Greeting.class, scene("checkout", "mobile"), greeting -> seen.add(greeting.text()));
            assertThat(seen).isEqualTo(expected);
            assertThat(executor.collect(Greeting.class, "retail", Greeting::text))
                    .containsExactly("mobile", "secondary", "web", "default");
            seen.clear();
            executor.executeAll(Greeting.class, "retail", greeting -> seen.add(greeting.text()));
            assertThat(seen).containsExactly("mobile", "secondary", "web", "default");
            executor.executeAll(Greeting.class, "absent", greeting -> {
                throw new AssertionError();
            });
            executor.executeAll(Greeting.class, scene("unknown", "unknown"), greeting -> {
                throw new AssertionError();
            });
            assertThat(executor.collect(Greeting.class, "absent", Greeting::text))
                    .isEmpty();
            assertThat(executor.collect(Greeting.class, scene("unknown", "unknown"), Greeting::text))
                    .isEmpty();
        }
    }

    @Test
    void contextOverloadsAndUserFailuresPreserveTheirContracts() {
        try (var context = catalog()) {
            var executor = new ExtensionExecutor(context.getBean(ExtensionRegistry.class));
            assertThatThrownBy(() -> executor.execute(Greeting.class, Greeting::text))
                    .isInstanceOf(IllegalStateException.class);
            assertThatThrownBy(() -> executor.run(Greeting.class, greeting -> {}))
                    .isInstanceOf(IllegalStateException.class);
            BizContextHolder.set(scene("checkout", "mobile"));
            try {
                assertThat(executor.execute(Greeting.class, Greeting::text)).isEqualTo("mobile");
                List<String> seen = new ArrayList<>();
                executor.run(Greeting.class, greeting -> seen.add(greeting.text()));
                executor.run(Greeting.class, scene("checkout", "web"), greeting -> seen.add(greeting.text()));
                assertThat(seen).containsExactly("mobile", "web");
                var failure = new IllegalStateException("synthetic extension failure");
                assertThatThrownBy(() -> executor.execute(Greeting.class, greeting -> {
                            throw failure;
                        }))
                        .isSameAs(failure);
            } finally {
                BizContextHolder.clear();
            }
        }
    }

    @Test
    void registryIndexesAnnotationAndSpiContributionsAndIgnoresChildRefresh() {
        var empty = new ExtensionRegistry();
        assertThat(empty.getExtensionsByScenario(Greeting.class, scene("checkout", "mobile")))
                .isEmpty();
        try (var context = catalog();
                var child = new AnnotationConfigApplicationContext()) {
            var registry = context.getBean(ExtensionRegistry.class);
            assertThat(registry.getAllExtensionTypes()).contains(Greeting.class, Other.class);
            assertThat(registry.getAllBizCodes()).containsExactlyInAnyOrder("retail", "spi");
            assertThat(registry.getExtensionCoordinates(Unregistered.class)).isEmpty();
            assertThat(registry.getExtensionsByBizCode("absent")).isEmpty();
            var spi = registry.getExtensionsByBizCode("spi").getFirst();
            assertThat(spi.extensionMetadata().annotationType()).isEqualTo(Extension.class);
            assertThat(spi.extensionMetadata().priority()).isZero();
            assertThat(spi.extensionMetadata().useCase()).isEqualTo("default");
            assertThat(spi.extensionMetadata().scenario()).isEqualTo("default");
            assertThat(new ExtensionExecutor(registry)
                            .execute(Greeting.class, BizScenario.valueOf("spi", "default", "default"), Greeting::text))
                    .isEqualTo("spi");
            child.setParent(context);
            child.refresh();
            registry.onApplicationEvent(new ContextRefreshedEvent(child));
            assertThat(registry.getExtensionCoordinates(Greeting.class)).hasSize(5);
            assertThat(registry.getExtensionsByScenario(Greeting.class, scene("checkout", "mobile")))
                    .hasSize(2);
        }
    }

    public interface Greeting extends IExtensionPoint {
        String text();
    }

    public interface Other extends IExtensionPoint {}

    public interface Unregistered extends IExtensionPoint {}

    @Extension(bizCode = "retail", useCase = "checkout", scenario = "mobile", priority = 1)
    public static class Mobile implements Greeting {
        public String text() {
            return "mobile";
        }
    }

    @Extension(bizCode = "retail", useCase = "checkout", scenario = "mobile", priority = 2)
    public static class Secondary implements Greeting {
        public String text() {
            return "secondary";
        }
    }

    @Extension(bizCode = "retail", useCase = "checkout", scenario = "web", priority = 3)
    public static class Web implements Greeting {
        public String text() {
            return "web";
        }
    }

    @Extension(bizCode = "retail", priority = 10)
    public static class Default implements Greeting {
        public String text() {
            return "default";
        }
    }

    @Extension(bizCode = "retail")
    public static class OtherImpl implements Other, Runnable {
        public void run() {}
    }
}
