package io.github.loadup.components.springdoc.autoconfigure;

import io.github.loadup.components.springdoc.properties.SpringDocProperties;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for LoadUp SpringDoc / knife4j component.
 *
 * <p>Activates when {@code knife4j-openapi3-jakarta-spring-boot-starter} is on the classpath
 * and {@code loadup.springdoc.enabled} is {@code true} (default).
 *
 * <p>Registers an {@link OpenAPI} bean with project metadata and an optional global
 * JWT Bearer security scheme. A custom bean of type {@link OpenAPI} defined elsewhere
 * takes precedence ({@link ConditionalOnMissingBean}).
 */
@Slf4j
@AutoConfiguration
@ConditionalOnClass(name = "com.github.xiaoymin.knife4j.spring.extension.OpenApiExtensionResolver")
@ConditionalOnProperty(prefix = "loadup.springdoc", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(SpringDocProperties.class)
public class SpringDocAutoConfiguration {

    /**
     * Builds the global {@link OpenAPI} descriptor from {@link SpringDocProperties}.
     *
     * @param props bound configuration properties
     * @return fully configured {@link OpenAPI} instance
     */
    @Bean
    @ConditionalOnMissingBean
    public OpenAPI loadupOpenAPI(SpringDocProperties props) {
        log.info("Initializing LoadUp SpringDoc component (knife4j + OpenAPI 3)");

        OpenAPI openAPI = new OpenAPI().info(buildInfo(props));

        if (props.isJwtEnabled()) {
            openAPI
                    .addSecurityItem(new SecurityRequirement().addList(props.getJwtSchemeName()))
                    .components(new Components()
                            .addSecuritySchemes(
                                    props.getJwtSchemeName(),
                                    new SecurityScheme()
                                            .name(props.getJwtSchemeName())
                                            .type(SecurityScheme.Type.HTTP)
                                            .scheme("bearer")
                                            .bearerFormat("JWT")
                                            .description(
                                                    "JWT Bearer token. Pass the token obtained from the"
                                                            + " authentication endpoint in this field.")));
        }

        return openAPI;
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Info buildInfo(SpringDocProperties props) {
        SpringDocProperties.Contact c = props.getContact();
        SpringDocProperties.License l = props.getLicense();

        return new Info()
                .title(props.getTitle())
                .description(props.getDescription())
                .version(props.getVersion())
                .contact(new Contact()
                        .name(c.getName())
                        .url(c.getUrl())
                        .email(c.getEmail()))
                .license(new License()
                        .name(l.getName())
                        .url(l.getUrl()));
    }
}

