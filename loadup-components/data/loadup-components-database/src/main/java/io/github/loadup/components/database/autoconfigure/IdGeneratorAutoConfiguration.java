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

import io.github.loadup.components.database.config.BusinessIdProperties;
import io.github.loadup.components.database.id.BusinessIdCommand;
import io.github.loadup.components.database.id.BusinessIdFormatter;
import io.github.loadup.components.database.id.BusinessIdGenerator;
import io.github.loadup.components.database.id.IdGenerator;
import io.github.loadup.components.database.sequence.Sequence;
import java.time.Clock;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/** Selects the default strategy without resolving it during SqlSessionFactory creation. */
@AutoConfiguration(after = SequenceAutoConfiguration.class)
@EnableConfigurationProperties(BusinessIdProperties.class)
public class IdGeneratorAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(IdGenerator.class)
    public BusinessIdGenerator businessIdGenerator(
            Sequence sequence,
            @Qualifier("databaseClock") Clock clock,
            BusinessIdProperties properties,
            BusinessIdFormatter formatter) {
        BusinessIdCommand defaults = new BusinessIdCommand(
                properties.dataVersion(),
                properties.systemVersion(),
                properties.eventCode(),
                properties.extensionCode(),
                properties.siteId(),
                properties.shard());
        return new BusinessIdGenerator(sequence, clock, formatter, defaults);
    }

    @Bean
    @ConditionalOnMissingBean
    public BusinessIdFormatter businessIdFormatter() {
        return new BusinessIdFormatter();
    }
}
