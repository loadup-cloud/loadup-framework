/*
 * #%L
 * LoadUp Contract
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
package io.github.loadup.modules.contract;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import io.github.loadup.modules.contract.app.autoconfigure.ContractAutoConfiguration;
import io.github.loadup.modules.contract.app.converter.ContractConverter;
import io.github.loadup.modules.contract.app.service.CatalogService;
import io.github.loadup.modules.contract.app.service.ContractResolveService;
import io.github.loadup.modules.contract.app.service.MerchantContractService;
import io.github.loadup.modules.contract.domain.gateway.CatalogGateway;
import io.github.loadup.modules.contract.domain.gateway.MerchantContractGateway;
import io.github.loadup.modules.contract.infrastructure.converter.ContractStorageConverter;
import io.github.loadup.modules.contract.infrastructure.mapper.CatalogVersionDOMapper;
import io.github.loadup.modules.contract.infrastructure.mapper.ContractRevisionDOMapper;
import io.github.loadup.modules.contract.infrastructure.mapper.MerchantContractDOMapper;
import java.time.Clock;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.transaction.PlatformTransactionManager;
import tools.jackson.databind.json.JsonMapper;

/** Checks bean registration only; real persistence behavior is covered by ContractPersistenceIT. */
class ContractAutoConfigurationTest {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ContractAutoConfiguration.class))
            .withBean(Clock.class, Clock::systemUTC)
            .withBean(JsonMapper.class, () -> JsonMapper.builder().build())
            .withBean(PlatformTransactionManager.class, () -> mock(PlatformTransactionManager.class))
            .withBean("catalogVersionDOMapper", CatalogVersionDOMapper.class, () -> mock(CatalogVersionDOMapper.class))
            .withBean(
                    "merchantContractDOMapper",
                    MerchantContractDOMapper.class,
                    () -> mock(MerchantContractDOMapper.class))
            .withBean(
                    "contractRevisionDOMapper",
                    ContractRevisionDOMapper.class,
                    () -> mock(ContractRevisionDOMapper.class));

    @Test
    void registersServicesRepositoriesAndGeneratedConvertersWithSingleDataSource() {
        runner.withBean(DataSource.class, () -> mock(DataSource.class)).run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).hasSingleBean(CatalogService.class);
            assertThat(context).hasSingleBean(MerchantContractService.class);
            assertThat(context).hasSingleBean(ContractResolveService.class);
            assertThat(context).hasSingleBean(CatalogGateway.class);
            assertThat(context).hasSingleBean(MerchantContractGateway.class);
            assertThat(context).hasSingleBean(ContractConverter.class);
            assertThat(context).hasSingleBean(ContractStorageConverter.class);
        });
    }

    @Test
    void doesNotRegisterComponentsWithoutDataSource() {
        runner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).doesNotHaveBean(CatalogService.class);
            assertThat(context).doesNotHaveBean(CatalogGateway.class);
            assertThat(context).doesNotHaveBean(ContractConverter.class);
        });
    }

    @Test
    void disabledModuleDoesNotRegisterComponentsEvenWithDataSource() {
        runner.withBean(DataSource.class, () -> mock(DataSource.class))
                .withPropertyValues("loadup.modules.contract.enabled=false")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).doesNotHaveBean(CatalogService.class);
                    assertThat(context).doesNotHaveBean(MerchantContractGateway.class);
                    assertThat(context).doesNotHaveBean(ContractStorageConverter.class);
                });
    }

    @Test
    void multipleDataSourcesWithoutPrimaryDoNotActivateModule() {
        runner.withBean("firstDataSource", DataSource.class, () -> mock(DataSource.class))
                .withBean("secondDataSource", DataSource.class, () -> mock(DataSource.class))
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).doesNotHaveBean(CatalogService.class);
                    assertThat(context).doesNotHaveBean(CatalogGateway.class);
                });
    }
}
