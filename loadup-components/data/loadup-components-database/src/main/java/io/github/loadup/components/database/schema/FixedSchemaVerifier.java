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
package io.github.loadup.components.database.schema;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;
import javax.sql.DataSource;

/**
 * Verifies that the runtime connection really is bound to the configured schema.
 *
 * <p>The check runs while the application context initializes, before any worker or scanner starts.
 * It proves the declared target only; database privileges and cross-schema rejection still require
 * a real database acceptance test.
 */
public final class FixedSchemaVerifier {

    private final DataSource dataSource;
    private final String schema;

    public FixedSchemaVerifier(DataSource dataSource, String schema) {
        this.dataSource = Objects.requireNonNull(dataSource, "data source");
        this.schema = FixedSchemaNames.require(schema);
    }

    public String schema() {
        return schema;
    }

    /** Fails startup when a physical connection does not resolve to the configured schema. */
    public void verify() {
        try (Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement();
                ResultSet result = statement.executeQuery("SELECT current_schema(), current_setting('search_path')")) {
            if (!result.next() || !Objects.equals(schema, result.getString(1))) {
                throw new IllegalStateException(
                        "Runtime connection does not resolve to loadup.database.schema " + schema);
            }
            String searchPath = result.getString(2);
            if (!Objects.equals(schema, searchPath) && !Objects.equals("\"" + schema + "\"", searchPath)) {
                throw new IllegalStateException(
                        "Runtime search_path must contain only loadup.database.schema " + schema);
            }
        } catch (SQLException failure) {
            throw new IllegalStateException("Unable to verify the configured database schema " + schema, failure);
        }
    }
}
