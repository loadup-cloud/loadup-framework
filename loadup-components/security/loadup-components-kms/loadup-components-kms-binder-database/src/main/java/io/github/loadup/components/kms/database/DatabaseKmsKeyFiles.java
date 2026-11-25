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
package io.github.loadup.components.kms.database;

import io.github.loadup.components.kms.KmsException;
import io.github.loadup.components.kms.KmsKeyRef;
import io.github.loadup.components.kms.KmsTemplate;
import io.github.loadup.components.kms.keystore.KeyStoreKmsTemplate;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyStore;
import java.security.interfaces.RSAPrivateKey;
import java.util.Arrays;
import java.util.regex.Pattern;
import javax.crypto.SecretKey;
import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.simple.JdbcClient;

/** Stores password-protected PKCS#12 files under deployment-assigned, immutable IDs. */
public final class DatabaseKmsKeyFiles {
    private static final int MAX_BYTES = 4 * 1024 * 1024;
    private static final Pattern ID = Pattern.compile("[A-Za-z0-9_-]{1,64}");
    private final JdbcClient jdbc;

    public DatabaseKmsKeyFiles(DataSource dataSource) {
        this.jdbc = JdbcClient.create(dataSource);
    }

    /** Leaves the input stream and both caller-owned passwords under the caller's control. */
    public void importFile(String id, InputStream input, char[] storePassword, char[] keyPassword) throws IOException {
        validateId(id);
        byte[] content = input.readNBytes(MAX_BYTES + 1);
        try {
            open(content, storePassword, keyPassword);
            jdbc.sql("""
                            INSERT INTO kms_key_file (id, content, created_at, updated_at, deleted)
                            VALUES (?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE)
                            """).param(id).param(content).update();
        } catch (DuplicateKeyException duplicate) {
            throw new IllegalStateException("KMS file ID already exists");
        } catch (DataAccessException failure) {
            throw new KmsException(KmsException.Code.UNAVAILABLE);
        } finally {
            Arrays.fill(content, (byte) 0);
        }
    }

    /** Retiring a file prevents future loads. Already loaded templates keep their snapshot. */
    public boolean retireFile(String id) {
        validateId(id);
        try {
            return jdbc.sql("""
                            UPDATE kms_key_file SET deleted = TRUE, updated_at = CURRENT_TIMESTAMP
                            WHERE id = ? AND deleted = FALSE
                            """).param(id).update() == 1;
        } catch (DataAccessException failure) {
            throw new KmsException(KmsException.Code.UNAVAILABLE);
        }
    }

    public KmsTemplate loadTemplate(String id, char[] storePassword, char[] keyPassword) {
        validateId(id);
        byte[] content;
        try {
            content = jdbc.sql("SELECT content FROM kms_key_file WHERE id = ? AND deleted = FALSE")
                    .param(id)
                    .query((rs, row) -> rs.getBytes(1))
                    .optional()
                    .orElseThrow(() -> new KmsException(KmsException.Code.NOT_FOUND));
        } catch (DataAccessException failure) {
            throw new KmsException(KmsException.Code.UNAVAILABLE);
        }
        try {
            return new KeyStoreKmsTemplate(open(content, storePassword, keyPassword), keyPassword);
        } finally {
            Arrays.fill(content, (byte) 0);
        }
    }

    private static void validateId(String id) {
        if (id == null || !ID.matcher(id).matches()) {
            throw new IllegalArgumentException("Invalid KMS file ID");
        }
    }

    private static KeyStore open(byte[] content, char[] storePassword, char[] keyPassword) {
        if (storePassword == null || storePassword.length == 0 || keyPassword == null || keyPassword.length == 0) {
            throw new IllegalArgumentException("KMS store and entry passwords are required");
        }
        // The JDK PKCS12 provider can also load JKS. Only accept a PKCS#12 (ASN.1 sequence) here.
        if (content.length == 0 || content.length > MAX_BYTES || content[0] != 0x30) {
            throw new KmsException(KmsException.Code.INVALID_REQUEST);
        }
        try {
            var store = KeyStore.getInstance("PKCS12");
            store.load(new ByteArrayInputStream(content), storePassword);
            boolean hasKey = false;
            for (var aliases = store.aliases(); aliases.hasMoreElements(); ) {
                String alias = aliases.nextElement();
                if (store.isKeyEntry(alias)) {
                    KmsKeyRef.latest(alias);
                    if (!supported(store.getKey(alias, keyPassword))) {
                        throw new KmsException(KmsException.Code.INVALID_REQUEST);
                    }
                    hasKey = true;
                }
            }
            if (!hasKey) {
                throw new KmsException(KmsException.Code.INVALID_REQUEST);
            }
            return store;
        } catch (IOException | GeneralSecurityException failure) {
            throw new KmsException(KmsException.Code.INVALID_REQUEST);
        }
    }

    private static boolean supported(Key key) {
        if (key instanceof RSAPrivateKey rsa) {
            int bits = rsa.getModulus().bitLength();
            return bits == 2048 || bits == 3072 || bits == 4096;
        }
        if (key instanceof SecretKey && "AES".equalsIgnoreCase(key.getAlgorithm())) {
            byte[] encoded = key.getEncoded();
            if (encoded != null) {
                try {
                    return encoded.length == 32;
                } finally {
                    Arrays.fill(encoded, (byte) 0);
                }
            }
        }
        return false;
    }
}
