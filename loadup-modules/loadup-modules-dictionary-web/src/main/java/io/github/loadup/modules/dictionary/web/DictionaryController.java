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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @PostMapping("/types")
    @Operation(summary = "Create a dictionary type")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public DictionaryType createType(@RequestBody TypeCreate command) {
        return service.createType(TenantUtil.getTenantId(), command);
    }

    @PutMapping("/types/{id}")
    @Operation(summary = "Update a dictionary type or its enabled state")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public DictionaryType updateType(@PathVariable String id, @RequestBody TypeUpdate command) {
        return service.updateType(TenantUtil.getTenantId(), id, command);
    }

    @DeleteMapping("/types/{id}")
    @Operation(summary = "Delete an empty dictionary type")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public SuccessResponse<Void> deleteType(@PathVariable String id) {
        service.deleteType(TenantUtil.getTenantId(), id);
        return SuccessResponse.success();
    }

    @GetMapping("/types")
    @Operation(summary = "List dictionary types, including disabled types")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public PageDTO<DictionaryType> listTypes(
            @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size) {
        DictionaryPage<DictionaryType> result = service.listTypes(TenantUtil.getTenantId(), page, size);
        return PageDTO.of(result.records(), result.total(), result.page(), result.size());
    }

    @PostMapping("/types/{typeCode}/items")
    @Operation(summary = "Create a dictionary item")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public DictionaryItem createItem(@PathVariable String typeCode, @RequestBody ItemCreate command) {
        return service.createItem(TenantUtil.getTenantId(), typeCode, command);
    }

    @PutMapping("/items/{id}")
    @Operation(summary = "Update a dictionary item or its enabled state")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public DictionaryItem updateItem(@PathVariable String id, @RequestBody ItemUpdate command) {
        return service.updateItem(TenantUtil.getTenantId(), id, command);
    }

    @DeleteMapping("/items/{id}")
    @Operation(summary = "Delete a dictionary item")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public SuccessResponse<Void> deleteItem(@PathVariable String id) {
        service.deleteItem(TenantUtil.getTenantId(), id);
        return SuccessResponse.success();
    }

    @GetMapping("/types/{typeCode}/items")
    @Operation(summary = "List dictionary items, including disabled items")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public PageDTO<DictionaryItem> listItems(
            @PathVariable String typeCode,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        DictionaryPage<DictionaryItem> result = service.listItems(TenantUtil.getTenantId(), typeCode, page, size);
        return PageDTO.of(result.records(), result.total(), result.page(), result.size());
    }

    @GetMapping("/{typeCode}/options")
    @Operation(summary = "Get enabled options for an enabled dictionary type")
    public List<DictionaryOption> options(@PathVariable String typeCode) {
        return service.listEnabledItems(TenantUtil.getTenantId(), typeCode).stream()
                .map(item -> new DictionaryOption(item.value(), item.label()))
                .toList();
    }

    public record DictionaryOption(String value, String label) {}
}
