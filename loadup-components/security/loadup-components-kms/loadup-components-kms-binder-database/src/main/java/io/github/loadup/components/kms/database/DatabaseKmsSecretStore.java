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
