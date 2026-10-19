/*
 * #%L
 * LoadUp Transfer App
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
package io.github.loadup.modules.transfer.app.autoconfigure;

import io.github.loadup.modules.file.client.facade.FileResourceFacade;
import io.github.loadup.modules.transfer.app.config.TransferTaskProperties;
import io.github.loadup.modules.transfer.app.converter.TransferDTOConverter;
import io.github.loadup.modules.transfer.app.converter.TransferDTOConverterImpl;
import io.github.loadup.modules.transfer.app.service.TransferTaskService;
import io.github.loadup.modules.transfer.client.facade.TransferTaskFacade;
import io.github.loadup.modules.transfer.client.spi.TransferHandler;
import io.github.loadup.modules.transfer.domain.gateway.TransferGateway;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@Import(TransferDTOConverterImpl.class)
@AutoConfiguration(
        afterName = {
            "io.github.loadup.modules.transfer.infrastructure.autoconfigure.TransferPersistenceAutoConfiguration",
            "io.github.loadup.modules.file.app.autoconfigure.FileResourceAutoConfiguration"
        })
@ConditionalOnSingleCandidate(DataSource.class)
@ConditionalOnBean({TransferGateway.class, FileResourceFacade.class})
@ConditionalOnProperty(
        prefix = "loadup.modules.transfer",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
@EnableConfigurationProperties(TransferTaskProperties.class)
public class TransferAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(TransferTaskFacade.class)
    public TransferTaskService transferTaskService(
            TransferGateway repository,
            FileResourceFacade files,
            List<TransferHandler> handlers,
            TransferTaskProperties properties,
            TransferDTOConverter converter,
            @org.springframework.beans.factory.annotation.Qualifier("transferClock") java.time.Clock clock) {
        return new TransferTaskService(repository, files, handlers, properties, converter, clock);
    }

    @Bean
    @ConditionalOnBean(TransferTaskService.class)
    @ConditionalOnMissingBean(name = "transferTaskProcessor")
    public io.github.loadup.modules.transfer.app.service.TransferTaskProcessor transferTaskProcessor(
            TransferTaskService service) {
        return new io.github.loadup.modules.transfer.app.service.TransferTaskProcessor(service);
    }

    @Bean
    @ConditionalOnMissingBean(name = "transferClock")
    public java.time.Clock transferClock() {
        return java.time.Clock.systemDefaultZone();
    }
}
