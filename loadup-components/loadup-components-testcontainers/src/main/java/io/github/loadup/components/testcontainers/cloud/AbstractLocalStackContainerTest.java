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

import org.springframework.test.context.ContextConfiguration;

/**
 * Abstract base test class that automatically configures LocalStack TestContainer for S3.
 *
 * <p>Test classes can extend this class to automatically use the shared LocalStack container
 * without needing to manually configure the context initializer.
 *
 * <p>Usage example:
 *
 * <pre>
 * &#64;SpringBootTest
 * class MyS3Test extends AbstractLocalStackContainerTest {
 *     &#64;Autowired
 *     private S3Client s3Client;
 *
 *     &#64;Test
 *     void testS3Upload() {
 *         // Your test code here
 *     }
 * }
 * </pre>
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@ContextConfiguration(initializers = LocalStackContainerInitializer.class)
public abstract class AbstractLocalStackContainerTest {

  /**
   * Get the S3 endpoint URL.
   *
   * @return the S3 endpoint URL
   */
  protected String getS3Endpoint() {
    return SharedLocalStackContainer.getS3Endpoint();
  }

  /**
   * Get the AWS access key.
   *
   * @return the access key
   */
  protected String getAccessKey() {
    return SharedLocalStackContainer.getAccessKey();
  }

  /**
   * Get the AWS secret key.
   *
   * @return the secret key
   */
  protected String getSecretKey() {
    return SharedLocalStackContainer.getSecretKey();
  }

  /**
   * Get the AWS region.
   *
   * @return the region
   */
  protected String getRegion() {
    return SharedLocalStackContainer.getRegion();
  }
}
