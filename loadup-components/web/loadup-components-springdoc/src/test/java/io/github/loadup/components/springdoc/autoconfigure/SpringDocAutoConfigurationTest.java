/*
 * #%L
 * LoadUp Components SpringDoc
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package io.github.loadup.components.springdoc.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.loadup.components.springdoc.properties.SpringDocProperties;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class SpringDocAutoConfigurationTest {

    private final ApplicationContextRunner runner =
            new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(SpringDocAutoConfiguration.class));

    @Test
    void defaultsPublishMetadataAndResolvableBearerRequirement() {
        runner.run(context -> {
            assertThat(context).hasNotFailed().hasSingleBean(OpenAPI.class);
            SpringDocProperties properties = context.getBean(SpringDocProperties.class);
            assertThat(properties.isEnabled()).isTrue();
            OpenAPI api = context.getBean(OpenAPI.class);
            assertThat(api.getInfo().getTitle()).isEqualTo("LoadUp API");
            assertThat(api.getInfo().getDescription()).isEqualTo("LoadUp Framework REST API documentation.");
            assertThat(api.getInfo().getVersion()).isEqualTo("0.0.2-SNAPSHOT");
            assertThat(api.getInfo().getContact().getName()).isEqualTo("LoadUp Team");
            assertThat(api.getInfo().getLicense().getName()).isEqualTo("Apache-2.0");
            assertThat(api.getSecurity())
                    .singleElement()
                    .satisfies(requirement -> assertThat(requirement).containsKey("BearerAuth"));
            SecurityScheme scheme = api.getComponents().getSecuritySchemes().get("BearerAuth");
            assertThat(scheme.getType()).isEqualTo(SecurityScheme.Type.HTTP);
            assertThat(scheme.getScheme()).isEqualTo("bearer");
            assertThat(scheme.getBearerFormat()).isEqualTo("JWT");
        });
    }

    @Test
    void bindsCustomizedMetadataAndSecurityScheme() {
        runner.withPropertyValues(
                        "loadup.springdoc.enabled=true",
                        "loadup.springdoc.title=Merchant API",
                        "loadup.springdoc.description=Merchant integration endpoints",
                        "loadup.springdoc.version=2.1",
                        "loadup.springdoc.jwt-enabled=true",
                        "loadup.springdoc.jwt-scheme-name=MerchantToken",
                        "loadup.springdoc.contact.name=Merchant Team",
                        "loadup.springdoc.contact.url=https://example.com/support",
                        "loadup.springdoc.contact.email=team@example.com",
                        "loadup.springdoc.license.name=Custom License",
                        "loadup.springdoc.license.url=https://example.com/license")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    OpenAPI api = context.getBean(OpenAPI.class);
                    Info info = api.getInfo();
                    assertThat(info.getTitle()).isEqualTo("Merchant API");
                    assertThat(info.getDescription()).isEqualTo("Merchant integration endpoints");
                    assertThat(info.getVersion()).isEqualTo("2.1");
                    assertThat(info.getContact().getName()).isEqualTo("Merchant Team");
                    assertThat(info.getContact().getUrl()).isEqualTo("https://example.com/support");
                    assertThat(info.getContact().getEmail()).isEqualTo("team@example.com");
                    assertThat(info.getLicense().getName()).isEqualTo("Custom License");
                    assertThat(info.getLicense().getUrl()).isEqualTo("https://example.com/license");
                    assertThat(api.getSecurity())
                            .singleElement()
                            .satisfies(requirement -> assertThat(requirement).containsOnlyKeys("MerchantToken"));
                    assertThat(api.getComponents().getSecuritySchemes()).containsOnlyKeys("MerchantToken");
                    assertThat(api.getComponents()
                                    .getSecuritySchemes()
                                    .get("MerchantToken")
                                    .getName())
                            .isEqualTo("MerchantToken");
                });
    }

    @Test
    void disabledJwtDoesNotAdvertiseAnAuthenticationRequirement() {
        runner.withPropertyValues("loadup.springdoc.jwt-enabled=false").run(context -> {
            assertThat(context).hasNotFailed().hasSingleBean(OpenAPI.class);
            OpenAPI api = context.getBean(OpenAPI.class);
            assertThat(api.getSecurity()).isNull();
            assertThat(api.getComponents()).isNull();
            assertThat(api.getInfo()).isNotNull();
        });
    }

    @Test
    void disabledComponentDoesNotRegisterMetadataOrProperties() {
        runner.withPropertyValues("loadup.springdoc.enabled=false")
                .run(context -> assertThat(context)
                        .hasNotFailed()
                        .doesNotHaveBean(OpenAPI.class)
                        .doesNotHaveBean(SpringDocProperties.class));
    }

    @Test
    void missingOpenApiDependencyDoesNotActivateConfiguration() {
        runner.withClassLoader(new FilteredClassLoader(OpenAPI.class))
                .run(context -> assertThat(context)
                        .hasNotFailed()
                        .doesNotHaveBean(SpringDocProperties.class)
                        .doesNotHaveBean("loadupOpenAPI"));
    }

    @Test
    void consumerOpenApiBeanTakesPrecedenceWithoutAddingSecurity() {
        OpenAPI custom = new OpenAPI().info(new Info().title("Consumer descriptor"));
        runner.withBean("consumerOpenApi", OpenAPI.class, () -> custom).run(context -> {
            assertThat(context).hasNotFailed().hasSingleBean(OpenAPI.class).doesNotHaveBean("loadupOpenAPI");
            assertThat(context.getBean(OpenAPI.class)).isSameAs(custom);
            assertThat(custom.getSecurity()).isNull();
            assertThat(custom.getComponents()).isNull();
        });
    }

    @Test
    void publicFactoryUsesReplacementNestedMetadata() {
        SpringDocProperties properties = new SpringDocProperties();
        properties.setEnabled(false);
        assertThat(properties.isEnabled()).isFalse();
        SpringDocProperties.Contact contact = new SpringDocProperties.Contact();
        contact.setName("Replacement contact");
        contact.setUrl("https://example.com/contact");
        contact.setEmail("contact@example.com");
        SpringDocProperties.License license = new SpringDocProperties.License();
        license.setName("Replacement license");
        license.setUrl("https://example.com/terms");
        properties.setContact(contact);
        properties.setLicense(license);
        OpenAPI api = new SpringDocAutoConfiguration().loadupOpenAPI(properties);
        assertThat(api.getInfo().getContact().getName()).isEqualTo("Replacement contact");
        assertThat(api.getInfo().getLicense().getName()).isEqualTo("Replacement license");
    }
}
