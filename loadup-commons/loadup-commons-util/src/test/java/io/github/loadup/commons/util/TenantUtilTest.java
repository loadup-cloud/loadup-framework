package io.github.loadup.commons.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class TenantUtilTest {
    @AfterEach
    void clearContext() {
        TenantUtil.clear();
    }

    @Test
    void restoresNestedTenantContext() {
        TenantUtil.setTenantId("outer");

        TenantUtil.runWithTenant(
                "inner", () -> assertThat(TenantUtil.getTenantId()).isEqualTo("inner"));

        assertThat(TenantUtil.getTenantId()).isEqualTo("outer");
    }

    @Test
    void clearsContextAfterCallbackFailure() {
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> TenantUtil.runWithTenant("tenant", () -> {
                    throw new IllegalStateException("failed");
                }))
                .isInstanceOf(IllegalStateException.class);

        assertThat(TenantUtil.hasTenantId()).isFalse();
    }
}
