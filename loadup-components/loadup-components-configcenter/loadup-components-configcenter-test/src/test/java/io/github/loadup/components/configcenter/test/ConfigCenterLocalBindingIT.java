package io.github.loadup.components.configcenter.test;

/*-
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

import static org.assertj.core.api.Assertions.assertThat;

import io.github.loadup.components.configcenter.ConfigCenterTemplate;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Integration test for the local binder.
 *
 * <p>Verifies the full {@link ConfigCenterTemplate} contract: read / write / remove / list /
 * listener. The same contract is exercised by other binders so that switching the backend does
 * not change business code.
 */
@SpringBootTest(classes = ConfigCenterTestApplication.class)
class ConfigCenterLocalBindingIT {

    @Autowired
    private ConfigCenterTemplate template;

    @Test
    void getConfig_withDefaultValue_returnsDefaultWhenMissing() {
        assertThat(template.getConfig("missing.key", "fallback")).isEqualTo("fallback");
    }

    @Test
    void setConfig_thenGetConfig_roundTrips() {
        template.setConfig("feature.enabled", "true");

        assertThat(template.getConfig("feature.enabled")).isEqualTo("true");
    }

    @Test
    void removeConfig_afterSet_returnsNull() {
        template.setConfig("temp.key", "some-value");
        assertThat(template.getConfig("temp.key")).isEqualTo("some-value");

        boolean removed = template.removeConfig("temp.key");

        assertThat(removed).isTrue();
        assertThat(template.getConfig("temp.key")).isNull();
    }

    @Test
    void listKeys_filtersByPrefix() {
        template.setConfig("list.a", "1");
        template.setConfig("list.b", "2");
        template.setConfig("other.c", "3");

        List<String> keys = template.listKeys("list.");

        assertThat(keys).containsExactlyInAnyOrder("list.a", "list.b");
    }

    @Test
    void addListener_receivesChange() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> received = new AtomicReference<>();
        template.addListener("watch.key", value -> {
            received.set(value);
            latch.countDown();
        });

        template.setConfig("watch.key", "new-value");

        assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
        assertThat(received.get()).isEqualTo("new-value");
    }

    @Test
    void addListener_receivesRemovalAsNull() throws Exception {
        template.setConfig("watch.remove", "value");
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> received = new AtomicReference<>("sentinel");
        template.addListener("watch.remove", value -> {
            received.set(value);
            latch.countDown();
        });

        template.removeConfig("watch.remove");

        assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
        assertThat(received.get()).isNull();
    }
}
