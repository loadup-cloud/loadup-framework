package io.github.loadup.components.configcenter.test;

/*-
 * #%L
 * LoadUp ConfigCenter Test
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import static org.assertj.core.api.Assertions.assertThat;

import io.github.loadup.components.configcenter.binding.ConfigCenterBinding;
import io.github.loadup.components.configcenter.manager.ConfigCenterBindingManager;
import io.github.loadup.components.configcenter.model.ConfigChangeEvent;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * ConfigCenter 集成测试 — Local binder。
 *
 * <p>使用 JUnit 5 @TempDir 创建隔离的文件目录，
 * 验证 getConfig / publishConfig / removeConfig / addListener 完整链路。
 */
@SpringBootTest(classes = ConfigCenterTestApplication.class)
@TestPropertySource(properties = "loadup.configcenter.binders.local.base-path=${java.io.tmpdir}/configcenter-it")
class ConfigCenterLocalBindingIT {

    @Autowired
    private ConfigCenterBindingManager manager;

    @TempDir
    Path tempDir;

    private ConfigCenterBinding binding;

    @BeforeEach
    void setUp() {
        binding = manager.getBinding();
    }

    @Test
    void publishConfig_thenGetConfig_shouldReturnPublishedValue() {
        binding.publishConfig("feature.enabled", "DEFAULT_GROUP", "true");

        Optional<String> result = binding.getConfig("feature.enabled", "DEFAULT_GROUP");

        assertThat(result).contains("true");
    }

    @Test
    void getConfig_whenNotExists_shouldReturnEmpty() {
        Optional<String> result = binding.getConfig("nonexistent.key.xyz");

        assertThat(result).isEmpty();
    }

    @Test
    void removeConfig_afterPublish_shouldReturnEmpty() {
        binding.publishConfig("temp.key", "DEFAULT_GROUP", "some-value");
        assertThat(binding.getConfig("temp.key", "DEFAULT_GROUP")).isPresent();

        boolean removed = binding.removeConfig("temp.key", "DEFAULT_GROUP");

        assertThat(removed).isTrue();
        assertThat(binding.getConfig("temp.key", "DEFAULT_GROUP")).isEmpty();
    }

    @Test
    void addListener_shouldReceiveChange_whenFileIsModified() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<ConfigChangeEvent> receivedEvent = new AtomicReference<>();

        binding.addListener("watch.key", "DEFAULT_GROUP", event -> {
            receivedEvent.set(event);
            latch.countDown();
        });

        // Trigger a change by publishing
        binding.publishConfig("watch.key", "DEFAULT_GROUP", "new-value");

        // Wait for listener (polling interval is 1s in test profile)
        boolean triggered = latch.await(5, TimeUnit.SECONDS);
        assertThat(triggered).isTrue();
        assertThat(receivedEvent.get()).isNotNull();
        assertThat(receivedEvent.get().getDataId()).isEqualTo("watch.key");
        assertThat(receivedEvent.get().getNewContent()).isEqualTo("new-value");
    }

    @Test
    void publishConfig_multipleKeys_shouldBeReadableIndependently() {
        binding.publishConfig("key.a", "DEFAULT_GROUP", "value-a");
        binding.publishConfig("key.b", "DEFAULT_GROUP", "value-b");

        assertThat(binding.getConfig("key.a", "DEFAULT_GROUP")).contains("value-a");
        assertThat(binding.getConfig("key.b", "DEFAULT_GROUP")).contains("value-b");
    }
}
