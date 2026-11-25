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
package io.github.loadup.components.kms.database.autoconfigure;

import io.github.loadup.components.kms.KmsTemplate;
import io.github.loadup.components.kms.database.DatabaseKmsKeyFiles;
import io.github.loadup.components.kms.database.config.DatabaseKmsProperties;
import java.util.Arrays;
import javax.sql.DataSource;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

@AutoConfiguration(after = DataSourceAutoConfiguration.class)
@ConditionalOnProperty(prefix = "loadup.kms", name = "enabled", havingValue = "true")
@ConditionalOnProperty(prefix = "loadup.kms", name = "binder", havingValue = "database")
@EnableConfigurationProperties(DatabaseKmsProperties.class)
public class DatabaseKmsAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(KmsTemplate.class)
    @DependsOnDatabaseInitialization
    KmsTemplate databaseKmsTemplate(DataSource dataSource, DatabaseKmsProperties properties, Environment environment) {
        char[] storePassword = password(environment, properties.keyStorePasswordEnv());
        char[] keyPassword = null;
        try {
            keyPassword = properties.keyPasswordEnv() == null
                    ? storePassword
                    : password(environment, properties.keyPasswordEnv());
            return new DatabaseKmsKeyFiles(dataSource).loadTemplate(properties.fileId(), storePassword, keyPassword);
        } finally {
            Arrays.fill(storePassword, '\0');
            if (keyPassword != null) {
                Arrays.fill(keyPassword, '\0');
            }
        }
    }

    @Bean
    @ConditionalOnProperty(prefix = "loadup.kms", name = "management-enabled", havingValue = "true")
    @ConditionalOnMissingBean
    DatabaseKmsKeyFiles databaseKmsKeyFiles(DataSource dataSource) {
        return new DatabaseKmsKeyFiles(dataSource);
    }

    private static char[] password(Environment environment, String name) {
        if (name == null || !name.matches("[A-Z][A-Z0-9_]*")) {
            throw new IllegalArgumentException("Invalid KMS password environment-property name");
        }
        String value = environment.getProperty(name);
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Required KMS password environment property is missing");
        }
        return value.toCharArray();
    }
}
