/*
 * Copyright (c) 2026 LoadUp Framework
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.loadup.components.testcontainers.cloud;

/*-
 * #%L
 * Loadup Components TestContainers
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Spring Boot test initializer for LocalStack TestContainer (S3).
 *
 * <p>This initializer configures Spring Boot test context to use the shared LocalStack container.
 * It automatically sets the necessary AWS S3 configuration properties.
 *
 * <p>Usage example:
 *
 * <pre>
 * &#64;SpringBootTest
 * &#64;ContextConfiguration(initializers = LocalStackContainerInitializer.class)
 * class MyS3Test {
 *     &#64;Autowired
 *     private S3Client s3Client;
 *
 *     &#64;Test
 *     void testS3Operations() {
 *         // Your test code here
 *     }
 * }
 * </pre>
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Slf4j
public class LocalStackContainerInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    /**
     * Initialize the application context with LocalStack container properties.
     *
     * @param applicationContext the application context to initialize
     */
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        // Check if TestContainers is enabled (global switch AND individual switch)
        String globalEnabled = applicationContext.getEnvironment().getProperty("loadup.testcontainers.enabled", "true");
        String localstackEnabled =
                applicationContext.getEnvironment().getProperty("loadup.testcontainers.localstack.enabled", "true");

        boolean enabled = Boolean.parseBoolean(globalEnabled) && Boolean.parseBoolean(localstackEnabled);

        if (enabled) {
            // Use TestContainers
            log.info("Using LocalStack TestContainer for tests");
            TestPropertyValues.of(
                            "aws.s3.endpoint=" + SharedLocalStackContainer.getS3Endpoint(),
                            "aws.access-key-id=" + SharedLocalStackContainer.getAccessKey(),
                            "aws.secret-access-key=" + SharedLocalStackContainer.getSecretKey(),
                            "aws.region=" + SharedLocalStackContainer.getRegion(),
                            "spring.cloud.aws.credentials.access-key=" + SharedLocalStackContainer.getAccessKey(),
                            "spring.cloud.aws.credentials.secret-key=" + SharedLocalStackContainer.getSecretKey(),
                            "spring.cloud.aws.s3.endpoint=" + SharedLocalStackContainer.getS3Endpoint(),
                            "spring.cloud.aws.region.static=" + SharedLocalStackContainer.getRegion(),
                            // LoadUp DFS configuration
                            "loadup.dfs.binders.s3.endpoint=" + SharedLocalStackContainer.getS3Endpoint(),
                            "loadup.dfs.binders.s3.accessKey=" + SharedLocalStackContainer.getAccessKey(),
                            "loadup.dfs.binders.s3.secretKey=" + SharedLocalStackContainer.getSecretKey(),
                            "loadup.dfs.binders.s3.region=" + SharedLocalStackContainer.getRegion())
                    .applyTo(applicationContext.getEnvironment());
        } else {
            // Use real AWS S3 from configuration
            log.info("Using real AWS S3 from application configuration");
        }
    }
}
