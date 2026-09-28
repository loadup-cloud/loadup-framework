package io.github.loadup.components.authserver.config;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.loadup.components.authserver.sas.SasAuthServerAutoConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

@DisplayName("SasAuthServerAutoConfiguration")
class SasAuthServerAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SasAuthServerAutoConfiguration.class));

    @Test
    @DisplayName("registers the standard authorization server beans from yml clients")
    void registersStandardBeans() {
        contextRunner
                .withPropertyValues(
                        "loadup.security.auth-server.issuer=http://localhost:8080",
                        "loadup.security.auth-server.audience=loadup-api",
                        "loadup.security.auth-server.clients[0].client-id=loadup-app",
                        "loadup.security.auth-server.clients[0].client-secret=change-me",
                        "loadup.security.auth-server.clients[0].scopes[0]=openid",
                        "loadup.security.auth-server.clients[0].grant-types[0]=client_credentials",
                        "loadup.security.auth-server.clients[0].grant-types[1]=refresh_token")
                .run(context -> {
                    assertThat(context).hasSingleBean(RegisteredClientRepository.class);
                    assertThat(context).hasSingleBean(AuthorizationServerSettings.class);
                    assertThat(context).hasSingleBean(OAuth2TokenCustomizer.class);
                    assertThat(context).hasNotFailed();

                    RegisteredClientRepository repository = context.getBean(RegisteredClientRepository.class);
                    assertThat(repository.findByClientId("loadup-app")).isNotNull();
                    assertThat(repository.findByClientId("missing")).isNull();
                    assertThat(context.getBean(AuthorizationServerSettings.class)
                                    .getIssuer())
                            .isEqualTo("http://localhost:8080");
                });
    }
}
