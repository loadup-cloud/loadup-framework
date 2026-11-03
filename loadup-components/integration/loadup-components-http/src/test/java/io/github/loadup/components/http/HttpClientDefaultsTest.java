package io.github.loadup.components.http;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

class HttpClientDefaultsTest {
    public record Settings(String tenantId, String serviceId, String audience) {}

    @Test
    void targetAudienceStaysSeparateFromCallerIdentity() {
        var environment = new MockEnvironment()
                .withProperty("spring.application.name", "payment")
                .withProperty("loadup.http.client.tenant-id", "tenant");
        var acquire = HttpClientDefaults.bind(environment, "loadup.modules.payment.acquire", "acquire", Settings.class);
        var realtime =
                HttpClientDefaults.bind(environment, "loadup.modules.payment.realtime", "realtime", Settings.class);
        assertThat(acquire.tenantId()).isEqualTo("tenant");
        assertThat(acquire.serviceId()).isEqualTo("payment");
        assertThat(acquire.audience()).isEqualTo("acquire");
        assertThat(realtime.audience()).isEqualTo("realtime");
    }

    @Test
    void explicitApplicationSettingsOverrideSharedDefaults() {
        var environment = new MockEnvironment()
                .withProperty("loadup.http.client.tenant-id", "shared")
                .withProperty("loadup.http.client.service-id", "default-caller")
                .withProperty("loadup.http.client.audience", "default-target")
                .withProperty("loadup.modules.payment.acquire.tenant-id", "specific")
                .withProperty("loadup.modules.payment.acquire.audience", "acquire-api");
        var settings =
                HttpClientDefaults.bind(environment, "loadup.modules.payment.acquire", "acquire", Settings.class);
        assertThat(settings.tenantId()).isEqualTo("specific");
        assertThat(settings.serviceId()).isEqualTo("default-caller");
        assertThat(settings.audience()).isEqualTo("acquire-api");
    }
}
