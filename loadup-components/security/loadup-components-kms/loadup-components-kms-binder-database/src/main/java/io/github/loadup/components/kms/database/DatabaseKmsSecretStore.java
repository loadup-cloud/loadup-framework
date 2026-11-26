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
import io.github.loadup.components.kms.keystore.KeyStoreKmsSecretManager;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.simple.JdbcClient;

public final class DatabaseKmsSecretStore implements KeyStoreKmsSecretManager.Storage {
    private final JdbcClient jdbc;

    public DatabaseKmsSecretStore(DataSource dataSource) {
        this.jdbc = JdbcClient.create(dataSource);
    }

    @Override
    public boolean create(String id, String ciphertext) {
        try {
            return jdbc.sql("""
                            INSERT INTO kms_secret (id, ciphertext, created_at, updated_at, deleted)
                            VALUES (?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE)
                            ON CONFLICT (id) DO NOTHING
                            """).param(id).param(ciphertext).update() == 1;
        } catch (DataAccessException failure) {
            throw new KmsException(KmsException.Code.UNAVAILABLE);
        }
    }

    @Override
    public boolean update(String id, String ciphertext) {
        try {
            return jdbc.sql("""
                            UPDATE kms_secret SET ciphertext = ?, updated_at = CURRENT_TIMESTAMP
                            WHERE id = ? AND deleted = FALSE
                            """).param(ciphertext).param(id).update() == 1;
        } catch (DataAccessException failure) {
            throw new KmsException(KmsException.Code.UNAVAILABLE);
        }
    }

    @Override
    public Optional<String> get(String id) {
        try {
            return jdbc.sql("SELECT ciphertext FROM kms_secret WHERE id = ? AND deleted = FALSE")
                    .param(id)
                    .query(String.class)
                    .optional();
        } catch (DataAccessException failure) {
            throw new KmsException(KmsException.Code.UNAVAILABLE);
        }
    }
}
