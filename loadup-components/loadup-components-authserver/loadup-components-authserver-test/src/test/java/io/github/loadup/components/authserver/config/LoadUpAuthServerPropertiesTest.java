package io.github.loadup.components.authserver.config;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.loadup.components.authserver.properties.LoadUpAuthServerProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

@DisplayName("LoadUpAuthServerProperties")
class LoadUpAuthServerPropertiesTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner().withUserConfiguration(PropertiesConfiguration.class);

    @Test
    @DisplayName("binds issuer and audience")
    void bindsIssuerAndAudience() {
        contextRunner
                .withPropertyValues(
                        "loadup.security.auth-server.issuer=https://sso.example.com/realms/loadup",
                        "loadup.security.auth-server.audience=loadup-api")
                .run(context -> {
                    LoadUpAuthServerProperties properties = context.getBean(LoadUpAuthServerProperties.class);
                    assertThat(properties.getIssuer()).isEqualTo("https://sso.example.com/realms/loadup");
                    assertThat(properties.getAudience()).isEqualTo("loadup-api");
                });
    }

    @Test
    @DisplayName("sas binder defaults are applied")
    void bindsSasDefaults() {
        contextRunner.run(context -> {
            LoadUpAuthServerProperties properties = context.getBean(LoadUpAuthServerProperties.class);
            assertThat(properties.getIssuer()).isEqualTo("http://localhost:8080");
            assertThat(properties.getJwk().getKid()).isEqualTo("loadup");
        });
    }

    @EnableConfigurationProperties(LoadUpAuthServerProperties.class)
    static class PropertiesConfiguration {}
}
