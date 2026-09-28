package io.github.loadup.components.testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import io.github.loadup.components.testcontainers.database.AbstractMongoDBContainerTest;
import io.github.loadup.components.testcontainers.database.SharedMongoDBContainer;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.mongodb.MongoDBContainer;

/**
 * Integration test class for SharedMongoDBContainer.
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@SpringBootTest(classes = TestApplication.class)
@TestPropertySource(properties = {"loadup.testcontainers.enabled=true", "loadup.testcontainers.mongodb.enabled=true"})
class SharedMongoDBContainerIT extends AbstractMongoDBContainerTest {
    private static final Logger log = LoggerFactory.getLogger(SharedMongoDBContainerIT.class);

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
