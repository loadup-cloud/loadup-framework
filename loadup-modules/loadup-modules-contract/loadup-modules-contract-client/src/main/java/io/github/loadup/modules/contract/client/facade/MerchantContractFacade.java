/*
 * #%L
 * LoadUp Contract Client
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
package io.github.loadup.modules.contract.client.facade;

import io.github.loadup.modules.contract.client.command.*;
import io.github.loadup.modules.contract.client.dto.*;
import io.github.loadup.modules.contract.client.query.MerchantContractQuery;
import java.time.*;
import java.util.*;

/** Public business contract for MerchantContractService. */
public interface MerchantContractFacade {
    MerchantContractDTO preview(ContractSignCommand command, String actor);

    MerchantContractDTO sign(ContractSignCommand command, String actor);

    MerchantContractDTO detail(String id);

    ContractPageDTO<MerchantContractDTO> page(MerchantContractQuery query);

    MerchantContractDTO changeStatus(ContractStatusCommand command, String actor);
}
