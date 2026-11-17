/*
 * Copyright (C) 2026 LoadUp Cloud
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
package io.github.loadup.components.runtime.deployment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class DeploymentAutoConfigurationTest {

    private final ApplicationContextRunner runner =
            new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(DeploymentAutoConfiguration.class));

    @Test
    void exposesConfiguredDeploymentCode() {
        runner.withPropertyValues("loadup.deployment.code=customer-a")
                .run(context -> assertThat(context).hasNotFailed().hasSingleBean(DeploymentIdentity.class));
        runner.withPropertyValues("loadup.deployment.code=customer-a")
                .run(context -> assertThat(
                                context.getBean(DeploymentIdentity.class).code())
                        .isEqualTo("customer-a"));
    }

    @Test
    void failsStartupForBlankDeploymentCode() {
        runner.withPropertyValues("loadup.deployment.code=")
                .run(context -> assertThat(context).hasFailed());
    }

    @Test
    void failsStartupForMalformedDeploymentCode() {
        for (String invalid :
                new String[] {"", " Customer-A ", "Customer-A", "1customer", "customer_a", "a".repeat(65)}) {
            runner.withPropertyValues("loadup.deployment.code=" + invalid)
                    .run(context -> assertThat(context).hasFailed());
        }
    }

    @Test
    void matchesOnlyAnExactCaseSensitiveValue() {
        var identity = new DeploymentIdentity("customer-a");
        assertThat(identity.matches("customer-a")).isTrue();
        assertThat(identity.matches("customer-b")).isFalse();
        assertThat(identity.matches(" customer-a")).isFalse();
        assertThat(identity.matches("Customer-A")).isFalse();
        assertThat(identity.matches("")).isFalse();
        assertThat(identity.matches(null)).isFalse();
    }

    @Test
    void rejectsMissingCodeInTheValueObject() {
        assertThatThrownBy(() -> new DeploymentIdentity(null)).isInstanceOf(NullPointerException.class);
        assertThat(Objects.requireNonNull(new DeploymentIdentity("a").code())).isEqualTo("a");
        assertThat(new DeploymentIdentity("customer-a").isBlank()).isFalse();
    }
}
