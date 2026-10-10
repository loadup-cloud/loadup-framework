/*
 * #%L
 * LoadUp Dictionary App
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
package io.github.loadup.modules.dictionary.app.autoconfigure;

import io.github.loadup.modules.dictionary.app.converter.DictionaryDTOConverter;
import io.github.loadup.modules.dictionary.app.converter.DictionaryDTOConverterImpl;
import io.github.loadup.modules.dictionary.app.service.DictionaryService;
import io.github.loadup.modules.dictionary.domain.gateway.DictionaryGateway;
import javax.sql.DataSource;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/** Wires the dictionary service to its JDBC store. */
@Import(DictionaryDTOConverterImpl.class)
@AutoConfiguration(
        afterName =
                "io.github.loadup.modules.dictionary.infrastructure.autoconfigure.DictionaryPersistenceAutoConfiguration")
@ConditionalOnSingleCandidate(DataSource.class)
@ConditionalOnBean(DictionaryGateway.class)
@ConditionalOnProperty(
        prefix = "loadup.modules.dictionary",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
public class DictionaryAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(io.github.loadup.modules.dictionary.client.facade.DictionaryFacade.class)
    public DictionaryService dictionaryService(DictionaryGateway repository, DictionaryDTOConverter converter) {
        return new DictionaryService(repository, converter);
    }
}
