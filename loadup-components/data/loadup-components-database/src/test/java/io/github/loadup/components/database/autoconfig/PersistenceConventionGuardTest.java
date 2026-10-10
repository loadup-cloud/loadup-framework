/*-
 * #%L
 * Loadup Components Database
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
package io.github.loadup.components.database.autoconfig;

import static org.assertj.core.api.Assertions.assertThat;

import com.mybatisflex.spring.boot.MyBatisFlexCustomizer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class PersistenceConventionGuardTest {
    private final ApplicationContextRunner runner =
            new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(MyBatisFlexAutoConfiguration.class));

    @Test
    void rejectsExternalCustomizer() {
        runner.withBean("externalCustomizer", MyBatisFlexCustomizer.class, () -> config -> {})
                .run(context -> assertThat(context).hasFailed());
    }

    @Test
    void rejectsLegacyGlobalSettings() {
        runner.withPropertyValues("loadup.database.logical-delete.enabled=false")
                .run(context -> assertThat(context).hasFailed());
    }

    @Test
    void acceptsDeploymentTenantSettings() {
        runner.withPropertyValues("loadup.database.multi-tenant.default-tenant-id=single")
                .run(context -> assertThat(context).hasNotFailed());
    }
}
