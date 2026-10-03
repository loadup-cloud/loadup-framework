package io.github.loadup.modules.upms.app.service;

import io.github.loadup.commons.domain.PageResult;
import io.github.loadup.commons.dto.PageQuery;
import io.github.loadup.modules.upms.client.command.UserPasswordChangeCommand;
import io.github.loadup.modules.upms.domain.entity.LoginLog;
import io.github.loadup.modules.upms.domain.entity.User;
import io.github.loadup.modules.upms.domain.gateway.LoginLogGateway;
import io.github.loadup.modules.upms.domain.gateway.UserGateway;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

/** Self-service account security queries and password updates. */
@Service
public class AccountSecurityService {
    private final UserGateway users;
    private final LoginLogGateway logins;
    private final UserService userService;

    public AccountSecurityService(UserGateway users, LoginLogGateway logins, UserService userService) {
        this.users = users;
        this.logins = logins;
        this.userService = userService;
    }

    public SecurityOverview overview(String userId) {
        User user = users.findById(requiredUser(userId))
                .orElseThrow(() -> new IllegalArgumentException("user not found"));
        return new SecurityOverview(user.getId(), user.getUsername(), user.isActive(),
                Boolean.TRUE.equals(user.getAccountNonLocked()),
                user.getLoginFailCount() == null ? 0 : user.getLoginFailCount(),
                user.getPasswordUpdateTime(), user.getLastLoginTime(), user.getLastLoginIp());
    }

    public PageResult<LoginEntry> loginHistory(String userId, int page, int size) {
        if (page < 1 || size < 1 || size > 100) throw new IllegalArgumentException("invalid page or size");
        PageResult<LoginLog> result = logins.findByUserId(requiredUser(userId), PageQuery.of(page, size));
        List<LoginEntry> entries = result.records().stream()
                .map(log -> new LoginEntry(log.getId(), log.getLoginTime(), log.getIpAddress(),
                        log.isSuccess(), log.getLoginType()))
                .toList();
        return PageResult.of(entries, result.total(), result.page(), result.size());
    }

    public void changePassword(String userId, String oldPassword, String newPassword, String confirmPassword) {
        userService.changePassword(new UserPasswordChangeCommand(
                requiredUser(userId), oldPassword, newPassword, confirmPassword));
    }

    private static String requiredUser(String userId) {
        if (userId == null || userId.isBlank()) throw new IllegalArgumentException("userId is required");
        return userId;
    }

    public record SecurityOverview(String userId, String username, boolean active, boolean accountNonLocked,
            int loginFailCount, LocalDateTime passwordUpdatedAt, LocalDateTime lastLoginAt, String lastLoginIp) {}
    public record LoginEntry(String id, LocalDateTime loginAt, String ipAddress, boolean success,
            String loginType) {}
}
