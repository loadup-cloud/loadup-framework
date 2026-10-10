/*
 * #%L
 * LoadUp Merchant
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
package io.github.loadup.modules.merchant.web;

import io.github.loadup.commons.result.SuccessResponse;
import io.github.loadup.modules.merchant.client.command.*;
import io.github.loadup.modules.merchant.client.dto.*;
import io.github.loadup.modules.merchant.client.facade.MerchantFacade;
import io.github.loadup.modules.merchant.client.query.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/merchants")
@Tag(name = "Merchants", description = "Tenant-scoped merchant basic information")
public class MerchantController {
    private final MerchantFacade service;

    public MerchantController(MerchantFacade service) {
        this.service = service;
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('merchant:write')")
    @Operation(summary = "Create merchant basic information")
    public SuccessResponse<MerchantDTO> create(
            @Valid @RequestBody MerchantCreateCommand command, Authentication actor) {
        return SuccessResponse.of(service.create(command, actor.getName()));
    }

    @PostMapping("/update")
    @PreAuthorize("hasAuthority('merchant:write')")
    @Operation(summary = "Update merchant with expected version; null private fields preserve data")
    public SuccessResponse<MerchantDTO> update(
            @Valid @RequestBody MerchantUpdateCommand command, Authentication actor) {
        return SuccessResponse.of(service.update(command, actor.getName()));
    }

    @PostMapping("/page")
    @PreAuthorize("hasAuthority('merchant:read')")
    @Operation(summary = "Page merchants in current tenant")
    public SuccessResponse<MerchantPageDTO> page(@Valid @RequestBody MerchantQuery query) {
        return SuccessResponse.of(service.page(query));
    }

    @PostMapping("/detail")
    @PreAuthorize("hasAuthority('merchant:read')")
    @Operation(summary = "Read merchant with masked private data")
    public SuccessResponse<MerchantDTO> detail(@Valid @RequestBody MerchantIdQuery query) {
        return SuccessResponse.of(service.detail(query.id()));
    }

    @PostMapping("/status")
    @PreAuthorize("hasAuthority('merchant:manage')")
    @Operation(summary = "Enable or disable merchant with expected version")
    public SuccessResponse<MerchantDTO> status(
            @Valid @RequestBody MerchantStatusCommand command, Authentication actor) {
        return SuccessResponse.of(service.changeStatus(command, actor.getName()));
    }
}
