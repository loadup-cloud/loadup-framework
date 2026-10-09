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
package io.github.loadup.modules.contract.client.query;

import io.github.loadup.modules.contract.client.dto.ValueDTO;
import jakarta.validation.constraints.*;
import java.util.Map;

public record ContractResolveQuery(
        @NotBlank String merchantId,
        @NotBlank String scopeKey,
        @NotBlank String itemKey,
        @NotNull @Size(max = 256) Map<String, ValueDTO> transactionFacts) {}
