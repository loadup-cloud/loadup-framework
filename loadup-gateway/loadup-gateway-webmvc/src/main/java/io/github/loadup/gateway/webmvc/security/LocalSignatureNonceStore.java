package io.github.loadup.gateway.webmvc.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;

/** Bounded nonce storage for a single gateway process. */
public final class LocalSignatureNonceStore implements SignatureNonceStore {
    private static final long MAX_NONCES = 100_000;
    private final Cache<String, Boolean> nonces = Caffeine.newBuilder()
            .maximumSize(MAX_NONCES)
            .expireAfterWrite(Duration.ofMinutes(5))
            .build();

    @Override
    public synchronized boolean reserve(String appId, String nonce) {
        String key = appId + ":" + nonce;
        if (nonces.getIfPresent(key) != null) {
            return false;
        }
        if (nonces.estimatedSize() >= MAX_NONCES) {
            nonces.cleanUp();
            if (nonces.estimatedSize() >= MAX_NONCES) {
                return false;
            }
        }
        nonces.put(key, Boolean.TRUE);
        return true;
    }
}
