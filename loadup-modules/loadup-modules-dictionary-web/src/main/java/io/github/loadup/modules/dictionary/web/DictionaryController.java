package io.github.loadup.modules.dictionary.web;

import io.github.loadup.commons.result.PageDTO;
import io.github.loadup.commons.result.SuccessResponse;
import io.github.loadup.commons.util.TenantUtil;
import io.github.loadup.modules.dictionary.DictionaryItem;
import io.github.loadup.modules.dictionary.DictionaryPage;
import io.github.loadup.modules.dictionary.DictionaryService;
import io.github.loadup.modules.dictionary.DictionaryType;
import io.github.loadup.modules.dictionary.ItemCreate;
import io.github.loadup.modules.dictionary.ItemUpdate;
import io.github.loadup.modules.dictionary.TypeCreate;
import io.github.loadup.modules.dictionary.TypeUpdate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** HTTP administration and option lookup for business dictionaries. */
@RestController
@RequestMapping("/api/dictionaries")
@Tag(name = "Data Dictionaries", description = "Tenant-scoped business dictionary management")
public class DictionaryController {
    private final DictionaryService service;

    public DictionaryController(DictionaryService service) {
        this.service = service;
    }

    @PostMapping("/types/create")
    @Operation(summary = "Create a dictionary type")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public DictionaryType createType(@RequestBody TypeCreate command) {
        return service.createType(TenantUtil.getTenantId(), command);
    }

    @PostMapping("/types/update")
    @Operation(summary = "Update a dictionary type or its enabled state")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public DictionaryType updateType(@RequestBody TypeUpdateRequest request) {
        return service.updateType(TenantUtil.getTenantId(), request.id(), request.command());
    }

    @PostMapping("/types/delete")
    @Operation(summary = "Delete an empty dictionary type")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public SuccessResponse<Void> deleteType(@RequestBody IdRequest request) {
        service.deleteType(TenantUtil.getTenantId(), request.id());
        return SuccessResponse.success();
    }

    @PostMapping("/types/list")
    @Operation(summary = "List dictionary types, including disabled types")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public PageDTO<DictionaryType> listTypes(@RequestBody PageRequest request) {
        DictionaryPage<DictionaryType> result = service.listTypes(TenantUtil.getTenantId(),
                request.page() == null ? 1 : request.page(), request.size() == null ? 20 : request.size());
        return PageDTO.of(result.records(), result.total(), result.page(), result.size());
    }

    @PostMapping("/items/create")
    @Operation(summary = "Create a dictionary item")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public DictionaryItem createItem(@RequestBody ItemCreateRequest request) {
        return service.createItem(TenantUtil.getTenantId(), request.typeCode(), request.command());
    }

    @PostMapping("/items/update")
    @Operation(summary = "Update a dictionary item or its enabled state")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public DictionaryItem updateItem(@RequestBody ItemUpdateRequest request) {
        return service.updateItem(TenantUtil.getTenantId(), request.id(), request.command());
    }

    @PostMapping("/items/delete")
    @Operation(summary = "Delete a dictionary item")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public SuccessResponse<Void> deleteItem(@RequestBody IdRequest request) {
        service.deleteItem(TenantUtil.getTenantId(), request.id());
        return SuccessResponse.success();
    }

    @PostMapping("/items/list")
    @Operation(summary = "List dictionary items, including disabled items")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public PageDTO<DictionaryItem> listItems(@RequestBody ItemListRequest request) {
        DictionaryPage<DictionaryItem> result = service.listItems(TenantUtil.getTenantId(), request.typeCode(),
                request.page() == null ? 1 : request.page(), request.size() == null ? 20 : request.size());
        return PageDTO.of(result.records(), result.total(), result.page(), result.size());
    }

    @PostMapping("/options")
    @Operation(summary = "Get enabled options for an enabled dictionary type")
    public List<DictionaryOption> options(@RequestBody TypeCodeRequest request) {
        return service.listEnabledItems(TenantUtil.getTenantId(), request.typeCode()).stream()
                .map(item -> new DictionaryOption(item.value(), item.label()))
                .toList();
    }

    public record DictionaryOption(String value, String label) {}
    public record IdRequest(String id) {}
    public record PageRequest(Integer page, Integer size) {}
    public record TypeCodeRequest(String typeCode) {}
    public record ItemListRequest(String typeCode, Integer page, Integer size) {}
    public record TypeUpdateRequest(String id, TypeUpdate command) {}
    public record ItemCreateRequest(String typeCode, ItemCreate command) {}
    public record ItemUpdateRequest(String id, ItemUpdate command) {}
}
