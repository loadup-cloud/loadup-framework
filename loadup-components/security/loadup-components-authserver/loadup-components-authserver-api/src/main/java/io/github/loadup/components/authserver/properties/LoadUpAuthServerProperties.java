package io.github.loadup.components.authserver.properties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Auth server configuration (prefix {@code loadup.security.auth-server}).
 *
 * <p>Configures embedded Spring Authorization Server endpoints.
 */
@ConfigurationProperties(prefix = "loadup.security.auth-server")
public class LoadUpAuthServerProperties {

    /**
     * Issuer identifier of this authorization server (e.g. {@code http://localhost:8080}).
     */
    private String issuer = "http://localhost:8080";

    private String audience;

    private boolean protocolEndpointsEnabled = true;

    private Duration userAccessTokenTtl = Duration.ofMinutes(30);

    public boolean isProtocolEndpointsEnabled() {
        return protocolEndpointsEnabled;
    }

    public void setProtocolEndpointsEnabled(boolean protocolEndpointsEnabled) {
        this.protocolEndpointsEnabled = protocolEndpointsEnabled;
    }

    public Duration getUserAccessTokenTtl() {
        return userAccessTokenTtl;
    }

    public void setUserAccessTokenTtl(Duration userAccessTokenTtl) {
        this.userAccessTokenTtl = userAccessTokenTtl;
    }

    /**
     * Signing JWK configuration used by the SAS binder.
     */
    private Jwk jwk = new Jwk();

    /**
     * OAuth2 clients registered in the embedded authorization server.
     */
    private List<Client> clients = new ArrayList<>();

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getAudience() {
        return audience;
    }

    public void setAudience(String audience) {
        this.audience = audience;
    }

    public Jwk getJwk() {
        return jwk;
    }

    public void setJwk(Jwk jwk) {
        this.jwk = jwk;
    }

    public List<Client> getClients() {
        return clients;
    }

    public void setClients(List<Client> clients) {
        this.clients = clients;
    }

    /**
     * Signing key settings for the embedded authorization server.
     */
    public static class Jwk {

        /**
         * Key ID included in the JWT header and the JWK set.
         */
        private String kid = "loadup";

        /**
         * RSA private key as Base64-encoded PKCS#8 DER. When empty, an ephemeral
         * RSA-2048 key is generated at startup (tokens become invalid after restart).
         */
        private String rsaPrivateKeyBase64;

        public String getKid() {
            return kid;
        }

        public void setKid(String kid) {
            this.kid = kid;
        }

        public String getRsaPrivateKeyBase64() {
            return rsaPrivateKeyBase64;
        }

        public void setRsaPrivateKeyBase64(String rsaPrivateKeyBase64) {
            this.rsaPrivateKeyBase64 = rsaPrivateKeyBase64;
        }
    }

    /**
     * OAuth2 client registration.
     */
    public static class Client {

        private String clientId;

        private String clientSecret;

        /**
         * Allowed redirect URIs (authorization code flow).
         */
        private List<String> redirectUris = new ArrayList<>();

        /**
         * Allowed scopes.
         */
        private List<String> scopes = new ArrayList<>();

        /**
         * Allowed grant types, e.g. {@code authorization_code}, {@code client_credentials},
         * {@code refresh_token}.
         */
        private List<String> grantTypes = new ArrayList<>(List.of("client_credentials", "refresh_token"));

        /**
         * Access token time-to-live.
         */
        private Duration accessTokenTtl = Duration.ofMinutes(30);

        /**
         * Whether explicit user consent is required for authorization requests.
         */
        private boolean requireAuthorizationConsent;

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getClientSecret() {
            return clientSecret;
        }

        public void setClientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
        }

        public List<String> getRedirectUris() {
            return redirectUris;
        }

        public void setRedirectUris(List<String> redirectUris) {
            this.redirectUris = redirectUris;
        }

        public List<String> getScopes() {
            return scopes;
        }

        public void setScopes(List<String> scopes) {
            this.scopes = scopes;
        }

        public List<String> getGrantTypes() {
            return grantTypes;
        }

        public void setGrantTypes(List<String> grantTypes) {
            this.grantTypes = grantTypes;
        }

        public Duration getAccessTokenTtl() {
            return accessTokenTtl;
        }

        public void setAccessTokenTtl(Duration accessTokenTtl) {
            this.accessTokenTtl = accessTokenTtl;
        }

        public boolean isRequireAuthorizationConsent() {
            return requireAuthorizationConsent;
        }

        public void setRequireAuthorizationConsent(boolean requireAuthorizationConsent) {
            this.requireAuthorizationConsent = requireAuthorizationConsent;
        }
    }
}
