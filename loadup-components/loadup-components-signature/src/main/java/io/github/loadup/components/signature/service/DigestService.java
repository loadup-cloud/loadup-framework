package io.github.loadup.components.signature.service;

import io.github.loadup.components.signature.enums.DigestAlgorithm;

/** Facade for digest (MD5 / SHA) and HMAC computation. */
public interface DigestService {

    /**
     * Computes the digest of the given bytes.
     *
     * @param data the input bytes
     * @param algorithm the digest algorithm
     * @return the hex-encoded digest
     */
    String digest(byte[] data, DigestAlgorithm algorithm);

    /**
     * Computes the digest of the given string.
     *
     * @param data the input string
     * @param algorithm the digest algorithm
     * @return the hex-encoded digest
     */
    String digest(String data, DigestAlgorithm algorithm);

    /**
     * Computes the HMAC of the given bytes.
     *
     * @param data the input bytes
     * @param key the secret key
     * @param algorithm the HMAC algorithm
     * @return the hex-encoded MAC
     */
    String hmac(byte[] data, byte[] key, DigestAlgorithm algorithm);

    /**
     * Computes the HMAC of the given string.
     *
     * @param data the input string
     * @param key the secret key
     * @param algorithm the HMAC algorithm
     * @return the hex-encoded MAC
     */
    String hmac(String data, String key, DigestAlgorithm algorithm);
}
