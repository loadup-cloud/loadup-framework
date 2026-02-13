package io.github.loadup.components.testcontainers;

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

import static org.junit.jupiter.api.Assertions.*;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import io.github.loadup.components.testcontainers.database.AbstractMongoDBContainerTest;
import io.github.loadup.components.testcontainers.database.SharedMongoDBContainer;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.mongodb.MongoDBContainer;

/**
 * Integration test class for SharedMongoDBContainer.
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Slf4j
@SpringBootTest(classes = TestApplication.class)
@TestPropertySource(properties = {"loadup.testcontainers.enabled=true", "loadup.testcontainers.mongodb.enabled=true"})
class SharedMongoDBContainerIT extends AbstractMongoDBContainerTest {

    @Test
    void testContainerIsRunning() {
        MongoDBContainer container = SharedMongoDBContainer.getInstance();
        assertNotNull(container, "Container should not be null");
        assertTrue(container.isRunning(), "Container should be running");
    }

    @Test
    void testContainerProperties() {
        assertNotNull(SharedMongoDBContainer.getConnectionString(), "Connection string should not be null");
        assertNotNull(SharedMongoDBContainer.getHost(), "Host should not be null");
        assertNotNull(SharedMongoDBContainer.getMappedPort(), "Port should not be null");

        log.info("Connection String: {}", SharedMongoDBContainer.getConnectionString());
        log.info("Host: {}", SharedMongoDBContainer.getHost());
        log.info("Port: {}", SharedMongoDBContainer.getMappedPort());
    }

    @Test
    void testDatabaseConnection() {
        String connectionString = SharedMongoDBContainer.getConnectionString();

        try (MongoClient mongoClient = MongoClients.create(connectionString)) {
            assertNotNull(mongoClient, "MongoClient should not be null");

            // Test connection by listing databases
            mongoClient.listDatabaseNames().first();
            log.info("Successfully connected to MongoDB");
        }
    }

    @Test
    void testCreateCollection() {
        String connectionString = SharedMongoDBContainer.getConnectionString();

        try (MongoClient mongoClient = MongoClients.create(connectionString)) {
            MongoDatabase database = mongoClient.getDatabase("testdb");
            assertNotNull(database, "Database should not be null");

            // Create a collection
            database.createCollection("testCollection");

            // Insert a document
            MongoCollection<Document> collection = database.getCollection("testCollection");
            Document doc = new Document("name", "test").append("value", 123);
            collection.insertOne(doc);

            // Query the document
            Document found = collection.find(new Document("name", "test")).first();
            assertNotNull(found, "Document should be found");
            assertEquals("test", found.getString("name"), "Name should be 'test'");
            assertEquals(123, found.getInteger("value"), "Value should be 123");

            // Clean up
            collection.drop();
        }
    }

    @Test
    void testSameContainerAcrossTests() {
        MongoDBContainer container1 = SharedMongoDBContainer.getInstance();
        MongoDBContainer container2 = SharedMongoDBContainer.getInstance();

        assertSame(container1, container2, "Should return the same container instance");
    }
}
