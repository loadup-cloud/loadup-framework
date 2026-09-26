package io.github.loadup.gateway.webmvc.security;

/** Reserves an app nonce for at least five minutes; implementations must be atomic across their scope. */
public interface SignatureNonceStore {
    boolean reserve(String appId, String nonce);
}
