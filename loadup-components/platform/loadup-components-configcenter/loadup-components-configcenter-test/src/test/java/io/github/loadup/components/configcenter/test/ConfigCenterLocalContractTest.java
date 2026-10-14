/*
 * #%L
 * LoadUp ConfigCenter Test
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
package io.github.loadup.components.configcenter.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.loadup.components.configcenter.ConfigCenterTemplate;
import io.github.loadup.components.configcenter.DefaultConfigCenterTemplate;
import io.github.loadup.components.configcenter.local.LocalConfigCenterProvider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

class ConfigCenterLocalContractTest {
    @Test
    void readsWritesReplacesAndDeletesThroughTemplateWithDefaultFallback() {
        LocalConfigCenterProvider provider = new LocalConfigCenterProvider();
        ConfigCenterTemplate template = new DefaultConfigCenterTemplate(provider);
        assertThat(provider.getBinderType()).isEqualTo("local");
        assertThat(template.getConfig("missing")).isNull();
        assertThat(template.getConfig("missing", "fallback")).isEqualTo("fallback");
        assertThat(template.setConfig("payments.timeout", "10")).isTrue();
        assertThat(template.getConfig("payments.timeout", "fallback")).isEqualTo("10");
        assertThat(template.setConfig("payments.timeout", "20")).isTrue();
        assertThat(template.getConfig("payments.timeout")).isEqualTo("20");
        template.setConfig("payments.empty", "");
        template.setConfig("orders.timeout", "30");
        assertThat(template.getConfig("payments.empty", "fallback")).isEmpty();
        assertThat(template.listKeys("payments.")).containsExactlyInAnyOrder("payments.timeout", "payments.empty");
        assertThat(template.listKeys("unknown.")).isEmpty();
        assertThat(template.listKeys("")).hasSize(3);
        assertThat(template.removeConfig("payments.timeout")).isTrue();
        assertThat(template.getConfig("payments.timeout", "fallback")).isEqualTo("fallback");
        assertThat(template.listKeys("payments.")).containsExactly("payments.empty");
        assertThat(template.removeConfig("never-present")).isTrue();
    }

    @Test
    void listenersObserveUpdatesDeletionAndStopAfterRemoval() {
        ConfigCenterTemplate template = new DefaultConfigCenterTemplate(new LocalConfigCenterProvider());
        List<String> first = new ArrayList<>();
        List<String> second = new ArrayList<>();
        List<String> unrelated = new ArrayList<>();
        template.addListener("key", first::add);
        template.addListener("key", second::add);
        template.addListener("other", unrelated::add);
        template.setConfig("key", "one");
        template.setConfig("key", "two");
        template.removeConfig("key");
        assertThat(first).containsExactlyElementsOf(Arrays.asList("one", "two", null));
        assertThat(second).containsExactlyElementsOf(Arrays.asList("one", "two", null));
        assertThat(unrelated).isEmpty();
        template.removeListener("key");
        template.removeListener("never-present");
        template.setConfig("key", "three");
        template.removeConfig("key");
        assertThat(first).hasSize(3);
        assertThat(second).hasSize(3);
    }

    @Test
    void listenerRegistrationDuringNotificationAppliesToFollowingChanges() {
        ConfigCenterTemplate template = new DefaultConfigCenterTemplate(new LocalConfigCenterProvider());
        List<String> late = new ArrayList<>();
        AtomicBoolean registered = new AtomicBoolean();
        template.addListener("key", value -> {
            if (registered.compareAndSet(false, true)) template.addListener("key", late::add);
        });
        template.setConfig("key", "one");
        assertThat(late).isEmpty();
        template.setConfig("key", "two");
        assertThat(late).containsExactly("two");
    }

    @Test
    void synchronousListenerFailurePropagatesAfterTheValueIsStored() {
        ConfigCenterTemplate template = new DefaultConfigCenterTemplate(new LocalConfigCenterProvider());
        IllegalStateException failure = new IllegalStateException("synthetic listener failure");
        template.addListener("key", value -> {
            throw failure;
        });
        assertThatThrownBy(() -> template.setConfig("key", "committed value")).isSameAs(failure);
        assertThat(template.getConfig("key")).isEqualTo("committed value");
        template.removeListener("key");
        assertThat(template.setConfig("key", "recovered value")).isTrue();
    }
}
