package io.github.loadup.components.testcontainers.database;

/*-
 * #%L
 * Loadup Components TestContainers
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

import io.github.loadup.components.testcontainers.annotation.ContainerType;
import io.github.loadup.components.testcontainers.annotation.EnableTestContainers;

/**
 * Abstract base test class that automatically configures MongoDB TestContainer.
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@EnableTestContainers(ContainerType.MONGODB)
public abstract class AbstractMongoDBContainerTest {

    protected String getConnectionString() {
        return SharedMongoDBContainer.getConnectionString();
    }

    protected String getHost() {
        return SharedMongoDBContainer.getHost();
    }

    protected Integer getPort() {
        return SharedMongoDBContainer.getMappedPort();
    }

    protected String getReplicaSetUrl() {
        return SharedMongoDBContainer.getReplicaSetUrl();
    }
}
