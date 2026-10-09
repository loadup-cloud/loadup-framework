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
package io.github.loadup.modules.contract.infrastructure.converter;

import io.github.loadup.commons.mapping.LoadUpMapStructConfig;
import io.github.loadup.modules.contract.domain.model.*;
import io.github.loadup.modules.contract.infrastructure.dataobject.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = LoadUpMapStructConfig.class)
public interface ContractStorageConverter {
    CatalogEntry toCatalog(CatalogVersionDO source);

    ContractRecord toContract(MerchantContractDO source);

    ContractRevisionRecord toRevision(ContractRevisionDO source);

    @Mapping(target = "deleted", constant = "0")
    CatalogVersionDO toCatalogDO(CatalogEntry source);

    @Mapping(target = "deleted", constant = "0")
    MerchantContractDO toContractDO(ContractRecord source);

    @Mapping(target = "deleted", constant = "0")
    ContractRevisionDO toRevisionDO(ContractRevisionRecord source);
}
