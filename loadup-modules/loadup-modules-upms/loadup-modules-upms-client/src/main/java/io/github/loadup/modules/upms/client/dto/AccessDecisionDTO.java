package io.github.loadup.modules.upms.client.dto;

public record AccessDecisionDTO(boolean allowed, String reason, String grantingRoleCode) {}
