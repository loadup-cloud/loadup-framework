package io.github.loadup.modules.upms.web;

import io.github.loadup.commons.result.PageDTO;
import io.github.loadup.commons.result.SuccessResponse;
import io.github.loadup.components.authorization.model.LoadUpUser;
import io.github.loadup.modules.upms.app.service.AccountSecurityService;
import io.github.loadup.modules.upms.app.service.AccountSecurityService.LoginEntry;
import io.github.loadup.modules.upms.app.service.AccountSecurityService.SecurityOverview;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Current user's security view; user ID is never accepted from the request. */
@RestController
@RequestMapping("/account/security")
@Tag(name = "Account Security", description = "Self-service password and login history")
public class AccountSecurityController {
    private final AccountSecurityService service;

    public AccountSecurityController(AccountSecurityService service) { this.service = service; }

    @PostMapping("/overview")
    @Operation(summary = "Get my account security status")
    @PreAuthorize("isAuthenticated()")
    public SecurityOverview overview(@RequestBody EmptyRequest request, Authentication authentication) {
        return service.overview(actor(authentication));
    }

    @PostMapping("/logins")
    @Operation(summary = "Get my recent login attempts")
    @PreAuthorize("isAuthenticated()")
    public PageDTO<LoginEntry> logins(@RequestBody PageRequest request, Authentication authentication) {
        var result = service.loginHistory(actor(authentication), request.page() == null ? 1 : request.page(),
                request.size() == null ? 20 : request.size());
        return PageDTO.of(result.records(), result.total(), result.page(), result.size());
    }

    @PostMapping("/password")
    @Operation(summary = "Change my password using the current password")
    @PreAuthorize("isAuthenticated()")
    public SuccessResponse<Void> changePassword(@RequestBody PasswordChangeRequest request,
            Authentication authentication) {
        if (request == null) throw new IllegalArgumentException("password change request is required");
        service.changePassword(actor(authentication), request.oldPassword(), request.newPassword(),
                request.confirmPassword());
        return SuccessResponse.success();
    }

    private static String actor(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof LoadUpUser user)
                || user.getUserId() == null || user.getUserId().isBlank()) {
            throw new IllegalArgumentException("authenticated user is required");
        }
        return user.getUserId();
    }

    public record PasswordChangeRequest(String oldPassword, String newPassword, String confirmPassword) {}
    public record EmptyRequest() {}
    public record PageRequest(Integer page, Integer size) {}
}
