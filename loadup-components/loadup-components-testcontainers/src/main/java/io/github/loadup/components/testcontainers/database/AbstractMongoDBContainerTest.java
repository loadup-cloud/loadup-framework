package io.github.loadup.components.testcontainers.database;

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
