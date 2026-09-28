package io.github.loadup.modules.upms.authserver;

import io.github.loadup.commons.enums.CommonResultCodeEnum;
import io.github.loadup.commons.result.FailureResponse;
import io.github.loadup.commons.result.IResponse;
import io.github.loadup.commons.result.SuccessResponse;
import io.github.loadup.components.authserver.properties.LoadUpAuthServerProperties;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** First-party JSON login for frontend applications. */
@RestController
public class UpmsTokenController {
    private final UpmsAuthenticationProvider authenticationProvider;
    private final JwtEncoder jwtEncoder;
    private final LoadUpAuthServerProperties properties;

    public UpmsTokenController(
            UpmsAuthenticationProvider authenticationProvider,
            JwtEncoder jwtEncoder,
            LoadUpAuthServerProperties properties) {
        this.authenticationProvider = authenticationProvider;
        this.jwtEncoder = jwtEncoder;
        this.properties = properties;
    }

    @PostMapping(
            path = "/api/auth/login",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public IResponse<TokenData> login(@RequestBody LoginRequest request) {
        if (request == null
                || request.username() == null
                || request.username().isBlank()
                || request.password() == null
                || request.password().isBlank()) {
            return FailureResponse.of(CommonResultCodeEnum.PARAM_ILLEGAL);
        }
        try {
            Authentication authentication = authenticationProvider.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(request.username(), request.password()));
            UpmsPrincipal principal = (UpmsPrincipal) authentication.getPrincipal();
            Set<String> roles = new LinkedHashSet<>();
            Set<String> permissions = new LinkedHashSet<>();
            for (GrantedAuthority authority : authentication.getAuthorities()) {
                String name = authority.getAuthority();
                if (name.startsWith("ROLE_")) {
                    roles.add(name.substring("ROLE_".length()));
                } else {
                    permissions.add(name);
                }
            }
            Instant issuedAt = Instant.now();
            Instant expiresAt = issuedAt.plus(properties.getUserAccessTokenTtl());
            JwtClaimsSet claims = JwtClaimsSet.builder()
                    .issuer(properties.getIssuer())
                    .subject(principal.subjectId())
                    .audience(List.of(properties.getAudience()))
                    .issuedAt(issuedAt)
                    .expiresAt(expiresAt)
                    .claim("token_use", "access")
                    .claim("username", principal.username())
                    .claim("roles", roles)
                    .claim("permissions", permissions)
                    .build();
            String token = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
            return SuccessResponse.of(new TokenData(
                    token, "Bearer", properties.getUserAccessTokenTtl().toSeconds()));
        } catch (AuthenticationException ex) {
            return FailureResponse.of(CommonResultCodeEnum.UNAUTHENTICATED);
        }
    }

    public record LoginRequest(String username, String password) {}

    public record TokenData(String accessToken, String tokenType, long expiresIn) {}
}
