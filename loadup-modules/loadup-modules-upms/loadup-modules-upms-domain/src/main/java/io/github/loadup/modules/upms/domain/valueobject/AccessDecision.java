package io.github.loadup.modules.upms.domain.valueobject;

public record AccessDecision(boolean allowed, String reason, String grantingRoleCode) {
    public static AccessDecision deny(String reason) {
        return new AccessDecision(false, reason, null);
    }

    public static AccessDecision permit(String roleCode) {
        return new AccessDecision(true, "GRANTED", roleCode);
    }
}
