/* Copyright 2026 LoadUp Cloud. Licensed under the Apache License, Version 2.0. */
package io.github.loadup.components.database.tenant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

import io.github.loadup.commons.util.TenantUtil;
import io.github.loadup.components.database.config.DatabaseProperties;
import org.junit.jupiter.api.Test;

class DatabaseTenantTest {
    @Test
    void singleTenantAlwaysUsesConfiguredDefault() {
        var properties = new DatabaseProperties();
        properties.getMultiTenant().setDefaultTenantId("single");
        var resolver = new DatabaseTenant(properties);
        TenantUtil.runWithTenant(
                "untrusted", () -> assertThat(resolver.current()).isEqualTo("single"));
    }

    @Test
    void requiredTenantUsesContextAndRejectsMissingContext() {
        var properties = new DatabaseProperties();
        properties.getMultiTenant().setEnabled(true);
        var resolver = new DatabaseTenant(properties);
        TenantUtil.runWithTenant(
                " tenant-a ", () -> assertThat(resolver.current()).isEqualTo("tenant-a"));
        TenantUtil.runWithTenant(null, () -> assertThatIllegalStateException().isThrownBy(resolver::current));
    }

    @Test
    void optionalTenantMayBeAbsent() {
        var properties = new DatabaseProperties();
        properties.getMultiTenant().setEnabled(true);
        properties.getMultiTenant().setRequired(false);
        var resolver = new DatabaseTenant(properties);
        TenantUtil.runWithTenant(null, () -> assertThat(resolver.current()).isNull());
    }
}
