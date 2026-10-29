/*
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
package io.github.loadup.components.database.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.loadup.components.database.id.BusinessIdFormatter;
import io.github.loadup.components.database.id.BusinessIdGenerator;
import io.github.loadup.components.database.id.IdGenerator;
import io.github.loadup.components.database.id.UuidV7IdGenerator;
import io.github.loadup.components.database.sequence.Sequence;
import io.github.loadup.components.database.sequence.SequenceScope;
import java.time.Clock;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class IdGeneratorAutoConfigurationTest {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(IdGeneratorAutoConfiguration.class))
            .withBean("databaseClock", Clock.class, Clock::systemDefaultZone);

    private Sequence sequence() {
        return new Sequence() {
            public long next(SequenceScope scope) {
                return 7;
            }

            public long[] nextBatch(SequenceScope scope, int count) {
                return new long[] {7};
            }
        };
    }

    @Test
    void defaultsToBusinessIdentifiers() {
        runner.withBean(Sequence.class, this::sequence).run(context -> {
            assertThat(context).hasNotFailed().hasSingleBean(IdGenerator.class);
            IdGenerator generator = context.getBean(IdGenerator.class);
            assertThat(generator).isInstanceOf(BusinessIdGenerator.class);
            var parts = context.getBean(BusinessIdFormatter.class).parse(generator.generate());
            assertThat(parts.command().eventCode()).isEqualTo("00000000");
            assertThat(parts.command().systemVersion()).isEqualTo(1);
            assertThat(parts.sequence()).isEqualTo(7);
        });
    }

    @Test
    void bindsDefaultBusinessMetadata() {
        runner.withBean(Sequence.class, this::sequence)
                .withPropertyValues(
                        "loadup.database.business-id.event-code=12345678",
                        "loadup.database.business-id.site-id=7",
                        "loadup.database.business-id.shard=123")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    var parts = context.getBean(BusinessIdFormatter.class)
                            .parse(context.getBean(IdGenerator.class).generate());
                    assertThat(parts.command().eventCode()).isEqualTo("12345678");
                    assertThat(parts.command().siteId()).isEqualTo(7);
                    assertThat(parts.command().shard()).isEqualTo(123);
                });
    }

    @Test
    void customStrategyBacksOffWithoutSequence() {
        runner.withBean(IdGenerator.class, () -> new UuidV7IdGenerator(false)).run(context -> {
            assertThat(context)
                    .hasNotFailed()
                    .hasSingleBean(IdGenerator.class)
                    .doesNotHaveBean(BusinessIdGenerator.class);
            assertThat(context.getBean(IdGenerator.class).generate()).matches("[0-9a-f]{32}");
        });
    }

    @Test
    void transitiveDatabaseJarDoesNotRequirePersistenceWithoutDataSource() {
        runner.withConfiguration(AutoConfigurations.of(SequenceAutoConfiguration.class))
                .run(context -> assertThat(context)
                        .hasNotFailed()
                        .doesNotHaveBean(Sequence.class)
                        .doesNotHaveBean(IdGenerator.class)
                        .hasSingleBean(BusinessIdFormatter.class));
    }
}
