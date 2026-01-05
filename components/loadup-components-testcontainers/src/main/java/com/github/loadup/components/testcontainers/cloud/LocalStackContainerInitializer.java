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
package com.github.loadup.components.testcontainers.cloud;

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
public class LocalStackContainerInitializer
    implements ApplicationContextInitializer<ConfigurableApplicationContext> {

  /**
   * Initialize the application context with LocalStack container properties.
   *
   * @param applicationContext the application context to initialize
   */
  @Override
  public void initialize(ConfigurableApplicationContext applicationContext) {
    TestPropertyValues.of(
            "aws.s3.endpoint=" + SharedLocalStackContainer.getS3Endpoint(),
            "aws.access-key-id=" + SharedLocalStackContainer.getAccessKey(),
            "aws.secret-access-key=" + SharedLocalStackContainer.getSecretKey(),
            "aws.region=" + SharedLocalStackContainer.getRegion(),
            "cloud.aws.credentials.access-key=" + SharedLocalStackContainer.getAccessKey(),
            "cloud.aws.credentials.secret-key=" + SharedLocalStackContainer.getSecretKey(),
            "cloud.aws.region.static=" + SharedLocalStackContainer.getRegion(),
            // 新的配置键（简化后的）
            "loadup.dfs.s3.endpoint=" + SharedLocalStackContainer.getS3Endpoint(),
            "loadup.dfs.s3.accessKey=" + SharedLocalStackContainer.getAccessKey(),
            "loadup.dfs.s3.secretKey=" + SharedLocalStackContainer.getSecretKey(),
            "loadup.dfs.s3.region=" + SharedLocalStackContainer.getRegion(),
            "cloud.aws.s3.endpoint=" + SharedLocalStackContainer.getS3Endpoint())
        .applyTo(applicationContext.getEnvironment());
  }
}
