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
package io.github.loadup.modules.contract.web;

import io.github.loadup.commons.request.query.IdQuery;
import io.github.loadup.commons.result.PageResponse;
import io.github.loadup.commons.result.SuccessResponse;
import io.github.loadup.modules.contract.app.service.*;
import io.github.loadup.modules.contract.client.command.*;
import io.github.loadup.modules.contract.client.dto.*;
import io.github.loadup.modules.contract.client.query.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/contract")
@Tag(name = "Contract", description = "Product catalog, sales plans and merchant contracts")
public class ContractController {
    private final CatalogService catalog;
    private final MerchantContractService contracts;
    private final ContractResolveService runtime;

    public ContractController(
            CatalogService catalog, MerchantContractService contracts, ContractResolveService runtime) {
        this.catalog = catalog;
        this.contracts = contracts;
        this.runtime = runtime;
    }

    @PostMapping("/catalog/save")
    @PreAuthorize("hasAuthority('contract:catalog:write')")
    @Operation(summary = "Create or update a validated draft version")
    public SuccessResponse<CatalogVersionDTO> save(
            @Valid @RequestBody CatalogSaveCommand command, Authentication actor) {
        return SuccessResponse.of(catalog.save(command, actor.getName()));
    }

    @PostMapping("/catalog/page")
    @PreAuthorize("hasAuthority('contract:catalog:read')")
    @Operation(summary = "Page catalog versions in the current tenant")
    public PageResponse<CatalogVersionDTO> catalogPage(@Valid @RequestBody CatalogPageQuery query) {
        return PageResponse.of(catalog.page(query));
    }

    @PostMapping("/catalog/detail")
    @PreAuthorize("hasAuthority('contract:catalog:read')")
    @Operation(summary = "Read a fixed catalog version")
    public SuccessResponse<CatalogVersionDTO> catalogDetail(@Valid @RequestBody IdQuery query) {
        return SuccessResponse.of(catalog.detail(query.id()));
    }

    @PostMapping("/catalog/publish")
    @PreAuthorize("hasAuthority('contract:catalog:publish')")
    @Operation(summary = "Publish an immutable version after server validation")
    public SuccessResponse<CatalogVersionDTO> publish(
            @Valid @RequestBody CatalogActionCommand command, Authentication actor) {
        return SuccessResponse.of(catalog.publish(command, actor.getName()));
    }

    @PostMapping("/catalog/retire")
    @PreAuthorize("hasAuthority('contract:catalog:publish')")
    @Operation(summary = "Retire a catalog version without rewriting signed contracts")
    public SuccessResponse<CatalogVersionDTO> retire(
            @Valid @RequestBody CatalogActionCommand command, Authentication actor) {
        return SuccessResponse.of(catalog.retire(command, actor.getName()));
    }

    @PostMapping("/merchant-contracts/preview")
    @PreAuthorize("hasAuthority('contract:merchant:sign')")
    @Operation(summary = "Preview merchant terms using verified merchant facts")
    public SuccessResponse<MerchantContractDTO> preview(
            @Valid @RequestBody ContractSignCommand command, Authentication actor) {
        return SuccessResponse.of(contracts.preview(command, actor.getName()));
    }

    @PostMapping("/merchant-contracts/sign")
    @PreAuthorize("hasAuthority('contract:merchant:sign')")
    @Operation(summary = "Sign a fixed sales plan with a durable idempotency key")
    public SuccessResponse<MerchantContractDTO> sign(
            @Valid @RequestBody ContractSignCommand command, Authentication actor) {
        return SuccessResponse.of(contracts.sign(command, actor.getName()));
    }

    @PostMapping("/merchant-contracts/page")
    @PreAuthorize("hasAuthority('contract:merchant:read')")
    @Operation(summary = "Page signed merchant contracts")
    public PageResponse<MerchantContractDTO> contractPage(@Valid @RequestBody MerchantContractPageQuery query) {
        return PageResponse.of(contracts.page(query));
    }

    @PostMapping("/merchant-contracts/detail")
    @PreAuthorize("hasAuthority('contract:merchant:read')")
    @Operation(summary = "Read signed terms and their stored digest")
    public SuccessResponse<MerchantContractDTO> contractDetail(@Valid @RequestBody IdQuery query) {
        return SuccessResponse.of(contracts.detail(query.id()));
    }

    @PostMapping("/merchant-contracts/status")
    @PreAuthorize("hasAuthority('contract:merchant:manage')")
    @Operation(summary = "Suspend, resume or terminate with generation checking")
    public SuccessResponse<MerchantContractDTO> status(
            @Valid @RequestBody ContractStatusCommand command, Authentication actor) {
        return SuccessResponse.of(contracts.changeStatus(command, actor.getName()));
    }

    @PostMapping("/runtime/resolve")
    @PreAuthorize("hasAuthority('contract:runtime:resolve')")
    @Operation(summary = "Resolve signed capabilities; transaction facts must be validated by the caller")
    public SuccessResponse<ContractDecisionDTO> resolve(@Valid @RequestBody ContractResolveQuery query) {
        return SuccessResponse.of(runtime.resolve(query));
    }
}
