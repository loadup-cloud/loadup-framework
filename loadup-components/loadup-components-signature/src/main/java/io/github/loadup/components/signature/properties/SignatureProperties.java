package io.github.loadup.components.signature.properties;

/*-
 * #%L
 * LoadUp Components :: Signature
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

import io.github.loadup.components.signature.enums.DigestAlgorithm;
import io.github.loadup.components.signature.enums.SignatureAlgorithm;
import jakarta.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/** Signature component configuration ({@code loadup.components.signature.*}). */
@Validated
@ConfigurationProperties(prefix = "loadup.components.signature")
public class SignatureProperties {

    /** Whether the signature component is enabled. */
    private boolean enabled = true;

    /** The default signature algorithm. */
    @NotNull
    private SignatureAlgorithm defaultSignatureAlgorithm = SignatureAlgorithm.SHA256_WITH_RSA;

    /** The default digest algorithm. */
    @NotNull
    private DigestAlgorithm defaultDigestAlgorithm = DigestAlgorithm.SHA256;

    /** Per-algorithm key sizes in bits. */
    private Map<String, Integer> keySize = createDefaultKeySizeMap();

    private static Map<String, Integer> createDefaultKeySizeMap() {
        Map<String, Integer> map = new HashMap<>();
        map.put("rsa", 2048);
        map.put("dsa", 2048);
        map.put("ec", 256);
        return map;
    }

    /**
     * Returns the configured key size for the given algorithm.
     *
     * @param algorithm the algorithm name (rsa / dsa / ec)
     * @return the key size in bits
     */
    public int getKeySize(String algorithm) {
        return keySize.getOrDefault(algorithm.toLowerCase(Locale.ROOT), 2048);
    }

    public SignatureProperties(
            boolean enabled,
            SignatureAlgorithm defaultSignatureAlgorithm,
            DigestAlgorithm defaultDigestAlgorithm,
            Map<String, Integer> keySize) {
        this.enabled = enabled;
        this.defaultSignatureAlgorithm = defaultSignatureAlgorithm;
        this.defaultDigestAlgorithm = defaultDigestAlgorithm;
        this.keySize = keySize;
    }

    public SignatureProperties() {}

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setDefaultSignatureAlgorithm(SignatureAlgorithm defaultSignatureAlgorithm) {
        this.defaultSignatureAlgorithm = defaultSignatureAlgorithm;
    }

    public void setDefaultDigestAlgorithm(DigestAlgorithm defaultDigestAlgorithm) {
        this.defaultDigestAlgorithm = defaultDigestAlgorithm;
    }

    public void setKeySize(Map<String, Integer> keySize) {
        this.keySize = keySize;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public SignatureAlgorithm getDefaultSignatureAlgorithm() {
        return this.defaultSignatureAlgorithm;
    }

    public DigestAlgorithm getDefaultDigestAlgorithm() {
        return this.defaultDigestAlgorithm;
    }

    public Map<String, Integer> getKeySize() {
        return this.keySize;
    }

    @Override
    public String toString() {
        return org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString(
                this, org.apache.commons.lang3.builder.ToStringStyle.JSON_STYLE);
    }
}
