package io.github.loadup.components.resourceserver;

import io.github.loadup.commons.enums.CommonResultCodeEnum;
import io.github.loadup.commons.result.FailureResponse;
import io.github.loadup.components.observability.ApiResultMetrics;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.util.StringUtils;
import tools.jackson.databind.ObjectMapper;

/** JWT resource-server integration shared by Controller applications and managed gateways. */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(prefix = "loadup.security.resource-server", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(ResourceServerProperties.class)
public class ResourceServerAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public LoadUpJwtAuthenticationConverter loadUpJwtAuthenticationConverter() {
        return new LoadUpJwtAuthenticationConverter();
    }

    @Bean
    @ConditionalOnMissingBean(JwtDecoder.class)
    public JwtDecoder resourceServerJwtDecoder(ResourceServerProperties properties) {
        if (!StringUtils.hasText(properties.getIssuerUri()) || !StringUtils.hasText(properties.getAudience())) {
            throw new IllegalArgumentException("Resource server requires issuer-uri and audience");
        }
        NimbusJwtDecoder decoder = StringUtils.hasText(properties.getJwkSetUri())
                ? NimbusJwtDecoder.withJwkSetUri(properties.getJwkSetUri()).build()
                : NimbusJwtDecoder.withIssuerLocation(properties.getIssuerUri()).build();
        decoder.setJwtValidator(accessTokenValidator(properties));
        return decoder;
    }

    static OAuth2TokenValidator<Jwt> accessTokenValidator(ResourceServerProperties properties) {
        OAuth2TokenValidator<Jwt> issuer = JwtValidators.createDefaultWithIssuer(properties.getIssuerUri());
        return jwt -> {
            OAuth2TokenValidatorResult issuerResult = issuer.validate(jwt);
            if (issuerResult.hasErrors()) {
                return issuerResult;
            }
            boolean validUse = !StringUtils.hasText(properties.getAccessTokenUseClaim())
                    || (StringUtils.hasText(properties.getAccessTokenUseValue())
                            && properties
                                    .getAccessTokenUseValue()
                                    .equals(jwt.getClaimAsString(properties.getAccessTokenUseClaim())));
            return validUse && jwt.getAudience() != null && jwt.getAudience().contains(properties.getAudience())
                    ? OAuth2TokenValidatorResult.success()
                    : OAuth2TokenValidatorResult.failure(
                            new OAuth2Error("invalid_token", "Invalid token use or audience", null));
        };
    }

    @Bean
    @Order(100)
    @ConditionalOnProperty(
            prefix = "loadup.security.resource-server",
            name = "default-security-filter-chain",
            havingValue = "true",
            matchIfMissing = true)
    public SecurityFilterChain resourceServerApiSecurityFilterChain(
            HttpSecurity http,
            JwtDecoder jwtDecoder,
            LoadUpJwtAuthenticationConverter jwtAuthenticationConverter,
            ResourceServerProperties properties,
            ObjectProvider<ObjectMapper> objectMapperProvider,
            ApiResultMetrics resultMetrics)
            throws Exception {
        if (!StringUtils.hasText(properties.getIssuerUri()) || !StringUtils.hasText(properties.getAudience())) {
            throw new IllegalArgumentException("Resource server requires issuer-uri and audience");
        }
        OAuth2TokenValidator<Jwt> validator = accessTokenValidator(properties);
        JwtDecoder validatedDecoder = token -> {
            Jwt jwt = jwtDecoder.decode(token);
            OAuth2TokenValidatorResult result = validator.validate(jwt);
            if (result.hasErrors()) {
                throw new JwtValidationException("Access token validation failed", result.getErrors());
            }
            return jwt;
        };
        List<String> publicPaths = properties.getPermitAll();
        ObjectMapper objectMapper = objectMapperProvider.getIfAvailable(ObjectMapper::new);
        http.securityMatcher("/api/**")
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> {
                    if (publicPaths != null && !publicPaths.isEmpty()) {
                        authorize
                                .requestMatchers(publicPaths.toArray(String[]::new))
                                .permitAll();
                    }
                    authorize.anyRequest().authenticated();
                })
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, exception) ->
                                writeError(response, objectMapper, resultMetrics, CommonResultCodeEnum.UNAUTHENTICATED))
                        .accessDeniedHandler((request, response, exception) ->
                                writeError(response, objectMapper, resultMetrics, CommonResultCodeEnum.ACCESS_DENIED)))
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt ->
                                jwt.decoder(validatedDecoder).jwtAuthenticationConverter(jwtAuthenticationConverter))
                        .authenticationEntryPoint((request, response, exception) ->
                                writeError(response, objectMapper, resultMetrics, CommonResultCodeEnum.UNAUTHENTICATED)));
        return http.build();
    }

    private static void writeError(
            HttpServletResponse response,
            ObjectMapper objectMapper,
            ApiResultMetrics resultMetrics,
            CommonResultCodeEnum code)
            throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getOutputStream().write(objectMapper.writeValueAsBytes(FailureResponse.of(code)));
        resultMetrics.record(false);
    }
}
