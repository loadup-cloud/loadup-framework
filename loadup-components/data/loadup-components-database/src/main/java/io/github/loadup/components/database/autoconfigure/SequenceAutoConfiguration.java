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

import io.github.loadup.components.database.sequence.DatabaseSequenceRangeAllocator;
import io.github.loadup.components.database.sequence.SegmentSequence;
import io.github.loadup.components.database.sequence.SequenceProperties;
import io.github.loadup.components.database.sequence.SequenceRangeAllocator;
import io.github.loadup.components.database.sequence.mapper.SequenceAllocationDOMapper;
import java.time.Clock;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.PlatformTransactionManager;

@AutoConfiguration(after = MyBatisFlexAutoConfiguration.class)
@ConditionalOnProperty(
        prefix = "loadup.database.sequence",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
@EnableConfigurationProperties(SequenceProperties.class)
@MapperScan("io.github.loadup.components.database.sequence.mapper")
public class SequenceAutoConfiguration {
    @Bean
    public SequenceRangeAllocator sequenceRangeAllocator(
            SequenceAllocationDOMapper mapper, PlatformTransactionManager manager) {
        return new DatabaseSequenceRangeAllocator(mapper, manager);
    }

    @Bean(destroyMethod = "close")
    public SegmentSequence sequence(
            SequenceRangeAllocator allocator,
            SequenceProperties properties,
            @Qualifier("databaseClock") Clock clock,
            org.springframework.beans.factory.ObjectProvider<io.micrometer.core.instrument.MeterRegistry> registry) {
        return new SegmentSequence(allocator, properties, clock, registry.getIfAvailable());
    }
}
