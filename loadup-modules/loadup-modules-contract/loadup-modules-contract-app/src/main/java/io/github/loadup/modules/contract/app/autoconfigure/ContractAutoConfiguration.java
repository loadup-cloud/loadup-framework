/*
 * #%L
 * LoadUp Contract App
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
package io.github.loadup.modules.contract.app.autoconfigure;

import io.github.loadup.modules.contract.app.converter.ContractConverterImpl;
import io.github.loadup.modules.contract.app.service.CatalogService;
import io.github.loadup.modules.contract.app.service.ContractPersistenceService;
import io.github.loadup.modules.contract.app.service.ContractResolveService;
import io.github.loadup.modules.contract.app.service.MerchantContractService;
import io.github.loadup.modules.contract.app.support.CatalogAssembler;
import io.github.loadup.modules.contract.app.support.ContractCodec;
import io.github.loadup.modules.contract.app.support.ContractMappingSupport;
import io.github.loadup.modules.contract.app.support.MerchantFacts;
import io.github.loadup.modules.contract.infrastructure.repository.CatalogGatewayImpl;
import io.github.loadup.modules.contract.infrastructure.repository.MerchantContractGatewayImpl;
import javax.sql.DataSource;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.context.annotation.Import;

@AutoConfiguration(
        afterName = {
            "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration",
            "org.springframework.boot.jdbc.autoconfigure.JdbcTemplateAutoConfiguration"
        })
@ConditionalOnSingleCandidate(DataSource.class)
@ConditionalOnProperty(
        prefix = "loadup.modules.contract",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
@Import({
    CatalogService.class,
    MerchantContractService.class,
    ContractResolveService.class,
    ContractPersistenceService.class,
    CatalogAssembler.class,
    ContractCodec.class,
    ContractMappingSupport.class,
    MerchantFacts.class,
    ContractConverterImpl.class,
    CatalogGatewayImpl.class,
    MerchantContractGatewayImpl.class
})
public class ContractAutoConfiguration {}
