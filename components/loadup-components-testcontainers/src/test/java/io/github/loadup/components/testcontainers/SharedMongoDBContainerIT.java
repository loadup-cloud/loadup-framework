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
package io.github.loadup.components.testcontainers;

import static org.junit.jupiter.api.Assertions.*;

import io.github.loadup.components.testcontainers.database.AbstractMongoDBContainerTest;
import io.github.loadup.components.testcontainers.database.SharedMongoDBContainer;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.mongodb.MongoDBContainer;

/**
 * Integration test class for SharedMongoDBContainer.
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Slf4j
@ActiveProfiles("test")
@SpringBootTest(classes = TestApplication.class)
@TestPropertySource(
    properties = {
      "loadup.testcontainers.enabled=true",
      "loadup.testcontainers.mongodb.enabled=true"
    })
class SharedMongoDBContainerIT extends AbstractMongoDBContainerTest {

  @Test
  void testContainerIsRunning() {
    MongoDBContainer container = SharedMongoDBContainer.getInstance();
    assertNotNull(container, "Container should not be null");
    assertTrue(container.isRunning(), "Container should be running");
  }

  @Test
  void testContainerProperties() {
    assertNotNull(
        SharedMongoDBContainer.getConnectionString(), "Connection string should not be null");
    assertNotNull(SharedMongoDBContainer.getHost(), "Host should not be null");
    assertNotNull(SharedMongoDBContainer.getPort(), "Port should not be null");

    log.info("Connection String: {}", SharedMongoDBContainer.getConnectionString());
    log.info("Host: {}", SharedMongoDBContainer.getHost());
    log.info("Port: {}", SharedMongoDBContainer.getPort());
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
