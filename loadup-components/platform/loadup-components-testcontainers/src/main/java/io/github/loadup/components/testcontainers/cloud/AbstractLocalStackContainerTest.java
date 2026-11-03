/*
 * Copyright (C) 2025-2026 LoadUp Cloud
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
package io.github.loadup.components.testcontainers.cloud;

import io.github.loadup.components.testcontainers.annotation.ContainerType;
import io.github.loadup.components.testcontainers.annotation.EnableTestContainers;

@EnableTestContainers(ContainerType.LOCALSTACK)
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
