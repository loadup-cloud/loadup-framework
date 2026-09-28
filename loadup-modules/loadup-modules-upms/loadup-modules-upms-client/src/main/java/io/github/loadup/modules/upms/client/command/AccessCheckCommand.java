package io.github.loadup.modules.upms.client.command;

public record AccessCheckCommand(
        String userId, String permissionCode, String resourceOwnerUserId, String resourceDepartmentId) {}
