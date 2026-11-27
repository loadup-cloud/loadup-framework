/*
 * Copyright (C) 2026 LoadUp Cloud
 *
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
 */
package io.github.loadup.components.kms.keystore;

import io.github.loadup.components.kms.KmsCiphertext;
import io.github.loadup.components.kms.KmsException;
import io.github.loadup.components.kms.KmsKeyMetadata;
import io.github.loadup.components.kms.KmsKeyRef;
import io.github.loadup.components.kms.KmsProviderVersion;
import io.github.loadup.components.kms.KmsSignature;
import io.github.loadup.components.kms.KmsSignatureAlgorithm;
import io.github.loadup.components.kms.KmsTemplate;
import io.github.loadup.components.kms.KmsVerificationKey;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/** Performs KMS operations on an already loaded Java KeyStore. */
public class KeyStoreKmsTemplate implements KmsTemplate {
    private final KeyStore store;
    private final KeyStore.ProtectionParameter protection;

    public KeyStoreKmsTemplate(KeyStore store, char[] password) {
        this.store = Objects.requireNonNull(store);
        this.protection = new KeyStore.PasswordProtection(password.clone());
    }

    private KeyStore.Entry entry(KmsKeyRef ref) throws Exception {
        if (ref == null || ref.providerVersion() != null || (ref.version() != 0 && ref.version() != 1)) {
            throw new IllegalArgumentException("Unknown key version");
        }
        KeyStore.Entry entry = store.getEntry(ref.name(), protection);
        if (entry == null) {
            throw new IllegalArgumentException("Unknown key");
        }
        return entry;
    }

    private static void expected(KmsKeyRef ref, String name, int version) {
        if (ref == null
                || !ref.name().equals(name)
                || version != 1
                || (ref.version() != 0 && ref.version() != version)) {
            throw new IllegalArgumentException("Key mismatch");
        }
    }

    private static Signature signature(KmsSignatureAlgorithm algorithm) throws Exception {
        Signature s = Signature.getInstance(
                algorithm == KmsSignatureAlgorithm.RSA_SHA256_PSS ? "RSASSA-PSS" : "SHA256withRSA");
        if (algorithm == KmsSignatureAlgorithm.RSA_SHA256_PSS) {
            s.setParameter(new PSSParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, 32, 1));
        }
        return s;
    }

    @Override
    public KmsSignature sign(KmsKeyRef key, KmsSignatureAlgorithm algorithm, byte[] message) {
        if (algorithm == null || message == null) {
            throw new IllegalArgumentException("Missing input");
        }
        try {
            var e = (KeyStore.PrivateKeyEntry) entry(key);
            Signature s = signature(algorithm);
            s.initSign(e.getPrivateKey());
            s.update(message);
            return new KmsSignature(key.name(), 1, algorithm, s.sign());
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new KmsException(KmsException.Code.INVALID_REQUEST);
        }
    }

    @Override
    public List<KmsVerificationKey> verificationKeys(String alias) {
        var active = new KmsKeyRef(alias, 1);
        var keys = new LinkedHashMap<String, KmsVerificationKey>();
        String prefix = alias + ".history.";
        try {
            var certificate = store.getCertificate(alias);
            if (certificate != null && certificate.getPublicKey() instanceof RSAPublicKey rsa) {
                var key = new KmsVerificationKey(active, rsa);
                keys.put(key.fingerprint(), key);
            }
            for (var entries = store.aliases(); entries.hasMoreElements(); ) {
                String name = entries.nextElement();
                if (name.length() <= prefix.length() || !name.regionMatches(true, 0, prefix, 0, prefix.length())) {
                    continue;
                }
                // Let the KeyStore decide alias case sensitivity when looking up the history entry.
                String retainedAlias = prefix + name.substring(prefix.length());
                if (store.isCertificateEntry(retainedAlias)
                        && store.getCertificate(retainedAlias).getPublicKey() instanceof RSAPublicKey rsa) {
                    String fingerprint = KmsVerificationKey.fingerprint(rsa);
                    var version = new KmsProviderVersion("keystore", "spki-" + fingerprint);
                    keys.putIfAbsent(fingerprint, new KmsVerificationKey(new KmsKeyRef(alias, 0, version), rsa));
                }
            }
            return List.copyOf(keys.values());
        } catch (KeyStoreException failure) {
            throw new KmsException(KmsException.Code.UNAVAILABLE);
        }
    }

    @Override
    public boolean verify(
            String alias, String fingerprint, KmsSignatureAlgorithm algorithm, byte[] message, byte[] signature) {
        return verificationKeys(alias).stream()
                .anyMatch(key -> key.fingerprint().equals(fingerprint)
                        && KmsVerificationKey.verify(key.publicKey(), algorithm, message, signature));
    }

    private KmsVerificationKey retainedKey(KmsKeyRef ref) {
        return verificationKeys(ref.name()).stream()
                .filter(key -> key.actual().equals(ref))
                .findFirst()
                .orElseThrow(() -> new KmsException(KmsException.Code.NOT_FOUND));
    }

    @Override
    public boolean verify(KmsKeyRef key, KmsSignature value, byte[] message) {
        if (value == null || message == null) {
            throw new IllegalArgumentException("Missing input");
        }
        if (key != null && key.providerVersion() != null) {
            if (!key.equals(value.keyRef())) {
                throw new IllegalArgumentException("Key mismatch");
            }
            return KmsVerificationKey.verify(retainedKey(key).publicKey(), value.algorithm(), message, value.bytes());
        }
        expected(key, value.keyName(), value.version());
        try {
            PublicKey pub =
                    ((KeyStore.PrivateKeyEntry) entry(key)).getCertificate().getPublicKey();
            Signature s = signature(value.algorithm());
            s.initVerify(pub);
            s.update(message);
            return s.verify(value.bytes());
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new KmsException(KmsException.Code.INVALID_REQUEST);
        }
    }

    @Override
    public KmsCiphertext encrypt(KmsKeyRef key, byte[] plaintext) {
        if (plaintext == null) {
            throw new IllegalArgumentException("Missing input");
        }
        try {
            SecretKey secret = ((KeyStore.SecretKeyEntry) entry(key)).getSecretKey();
            Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
            c.init(Cipher.ENCRYPT_MODE, secret);
            byte[] encrypted = c.doFinal(plaintext);
            byte[] iv = c.getIV();
            byte[] result = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, result, 0, iv.length);
            System.arraycopy(encrypted, 0, result, iv.length, encrypted.length);
            return new KmsCiphertext(
                    key.name(), 1, "local:v1:" + Base64.getEncoder().encodeToString(result));
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new KmsException(KmsException.Code.INVALID_REQUEST);
        }
    }

    @Override
    public byte[] decrypt(KmsKeyRef key, KmsCiphertext ciphertext) {
        if (ciphertext == null) {
            throw new IllegalArgumentException("Missing ciphertext");
        }
        expected(key, ciphertext.keyName(), ciphertext.version());
        if (!ciphertext.value().startsWith("local:v1:")) {
            throw new IllegalArgumentException("Invalid local ciphertext");
        }
        try {
            byte[] data = Base64.getDecoder().decode(ciphertext.value().substring(9));
            if (data.length < 28) {
                throw new IllegalArgumentException("Invalid ciphertext");
            }
            SecretKey secret = ((KeyStore.SecretKeyEntry) entry(key)).getSecretKey();
            Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
            c.init(Cipher.DECRYPT_MODE, secret, new GCMParameterSpec(128, data, 0, 12));
            return c.doFinal(data, 12, data.length - 12);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new KmsException(KmsException.Code.INVALID_REQUEST);
        }
    }

    @Override
    public String publicKey(KmsKeyRef key) {
        try {
            PublicKey rsa = key != null && key.providerVersion() != null
                    ? retainedKey(key).publicKey()
                    : ((KeyStore.PrivateKeyEntry) entry(key)).getCertificate().getPublicKey();
            byte[] encoded = rsa.getEncoded();
            return "-----BEGIN PUBLIC KEY-----\n"
                    + Base64.getMimeEncoder(64, new byte[] {'\n'}).encodeToString(encoded)
                    + "\n-----END PUBLIC KEY-----\n";
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new KmsException(KmsException.Code.INVALID_REQUEST);
        }
    }

    @Override
    public KmsKeyMetadata metadata(String name) {
        KmsKeyRef key = KmsKeyRef.latest(name);
        try {
            var e = entry(key);
            if (e instanceof KeyStore.PrivateKeyEntry) {
                return new KmsKeyMetadata(name, "rsa", 1, 1, 1, false, false, true, Map.of(1, publicKey(key)));
            }
            if (e instanceof KeyStore.SecretKeyEntry) {
                return new KmsKeyMetadata(name, "aes256-gcm96", 1, 1, 1, true, true, false, Map.of());
            }
            throw new IllegalArgumentException("Unsupported key");
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new KmsException(KmsException.Code.INVALID_REQUEST);
        }
    }
}
