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

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.DeleteResponse;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import co.elastic.clients.elasticsearch.indices.DeleteIndexResponse;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import io.github.loadup.components.testcontainers.search.AbstractElasticsearchContainerTest;
import io.github.loadup.components.testcontainers.search.SharedElasticsearchContainer;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.elasticsearch.ElasticsearchContainer;

/**
 * Integration test class for SharedElasticsearchContainer.
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Slf4j
@ActiveProfiles("test")
@SpringBootTest(classes = TestApplication.class)
@TestPropertySource(
        properties = {"loadup.testcontainers.enabled=true", "loadup.testcontainers.elasticsearch.enabled=true"})
class SharedElasticsearchContainerIT extends AbstractElasticsearchContainerTest {

    @Test
    void testContainerIsRunning() {
        ElasticsearchContainer container = SharedElasticsearchContainer.getInstance();
        assertNotNull(container, "Container should not be null");
        assertTrue(container.isRunning(), "Container should be running");
    }

    @Test
    void testContainerProperties() {
        assertNotNull(SharedElasticsearchContainer.getHttpHostAddress(), "HTTP host address should not be null");
        assertNotNull(SharedElasticsearchContainer.getHost(), "Host should not be null");
        assertNotNull(SharedElasticsearchContainer.getPort(), "Port should not be null");

        log.info("HTTP Host Address: {}", SharedElasticsearchContainer.getHttpHostAddress());
        log.info("Host: {}", SharedElasticsearchContainer.getHost());
        log.info("Port: {}", SharedElasticsearchContainer.getPort());
    }

    @Test
    void testElasticsearchConnection() throws Exception {
        String httpHostAddress = SharedElasticsearchContainer.getHttpHostAddress();
        String[] parts = httpHostAddress.split(":");
        String host = parts[0];
        int port = Integer.parseInt(parts[1]);

        try (RestClient restClient =
                RestClient.builder(new HttpHost(host, port, "http")).build()) {
            RestClientTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
            ElasticsearchClient client = new ElasticsearchClient(transport);

            // Ping Elasticsearch to verify connection
            boolean pingResult = client.ping().value();
            assertTrue(pingResult, "Ping should return true");

            log.info("Successfully connected to Elasticsearch");
            transport.close();
        }
    }

    @Test
    void testCreateIndex() throws Exception {
        String indexName = "test-index-" + System.currentTimeMillis();
        String httpHostAddress = SharedElasticsearchContainer.getHttpHostAddress();
        String[] parts = httpHostAddress.split(":");
        String host = parts[0];
        int port = Integer.parseInt(parts[1]);

        try (RestClient restClient =
                RestClient.builder(new HttpHost(host, port, "http")).build()) {
            RestClientTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
            ElasticsearchClient client = new ElasticsearchClient(transport);

            // Create an index
            CreateIndexResponse createResponse = client.indices().create(c -> c.index(indexName));
            assertTrue(createResponse.acknowledged(), "Index creation should be acknowledged");
            log.info("Created index: {}", indexName);

            // Verify index exists
            co.elastic.clients.elasticsearch.indices.ExistsRequest existsRequest =
                    co.elastic.clients.elasticsearch.indices.ExistsRequest.of(e -> e.index(indexName));
            boolean exists = client.indices().exists(existsRequest).value();
            assertTrue(exists, "Index should exist");

            // Delete the index
            DeleteIndexResponse deleteResponse = client.indices().delete(d -> d.index(indexName));
            assertTrue(deleteResponse.acknowledged(), "Index deletion should be acknowledged");
            log.info("Deleted index: {}", indexName);

            transport.close();
        }
    }

    @Test
    void testIndexDocument() throws Exception {
        String indexName = "test-index-" + System.currentTimeMillis();
        String docId = "1";
        String httpHostAddress = SharedElasticsearchContainer.getHttpHostAddress();
        String[] parts = httpHostAddress.split(":");
        String host = parts[0];
        int port = Integer.parseInt(parts[1]);

        try (RestClient restClient =
                RestClient.builder(new HttpHost(host, port, "http")).build()) {
            RestClientTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
            ElasticsearchClient client = new ElasticsearchClient(transport);

            // Create an index
            client.indices().create(c -> c.index(indexName));

            // Index a document
            TestDocument doc = new TestDocument("John Doe", 30);
            IndexResponse indexResponse =
                    client.index(i -> i.index(indexName).id(docId).document(doc));
            assertEquals(docId, indexResponse.id(), "Document ID should match");
            log.info("Indexed document with ID: {}", docId);

            // Refresh to make document searchable
            client.indices().refresh(r -> r.index(indexName));

            // Get the document
            GetResponse<TestDocument> getResponse =
                    client.get(g -> g.index(indexName).id(docId), TestDocument.class);
            assertTrue(getResponse.found(), "Document should be found");
            TestDocument retrievedDoc = getResponse.source();
            assertNotNull(retrievedDoc, "Retrieved document should not be null");
            assertEquals("John Doe", retrievedDoc.getName(), "Name should match");
            assertEquals(30, retrievedDoc.getAge(), "Age should match");
            log.info("Retrieved document: {}", retrievedDoc);

            // Delete the document
            DeleteResponse deleteResponse =
                    client.delete(d -> d.index(indexName).id(docId));
            assertEquals(docId, deleteResponse.id(), "Deleted document ID should match");
            log.info("Deleted document with ID: {}", docId);

            // Delete the index
            client.indices().delete(d -> d.index(indexName));

            transport.close();
        }
    }

    @Test
    void testSearchDocument() throws Exception {
        String indexName = "test-index-" + System.currentTimeMillis();
        String httpHostAddress = SharedElasticsearchContainer.getHttpHostAddress();
        String[] parts = httpHostAddress.split(":");
        String host = parts[0];
        int port = Integer.parseInt(parts[1]);

        try (RestClient restClient =
                RestClient.builder(new HttpHost(host, port, "http")).build()) {
            RestClientTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
            ElasticsearchClient client = new ElasticsearchClient(transport);

            // Create an index
            client.indices().create(c -> c.index(indexName));

            // Index multiple documents
            client.index(i -> i.index(indexName).id("1").document(new TestDocument("Alice", 25)));
            client.index(i -> i.index(indexName).id("2").document(new TestDocument("Bob", 30)));
            client.index(i -> i.index(indexName).id("3").document(new TestDocument("Charlie", 35)));

            // Refresh to make documents searchable
            client.indices().refresh(r -> r.index(indexName));

            // Search for documents
            SearchResponse<TestDocument> searchResponse = client.search(
                    s -> s.index(indexName)
                            .query(q -> q.match(m -> m.field("name").query("Bob"))),
                    TestDocument.class);

            assertEquals(1, searchResponse.hits().total().value(), "Should find 1 document");
            TestDocument foundDoc = searchResponse.hits().hits().get(0).source();
            assertNotNull(foundDoc, "Found document should not be null");
            assertEquals("Bob", foundDoc.getName(), "Name should be Bob");
            assertEquals(30, foundDoc.getAge(), "Age should be 30");
            log.info("Found document: {}", foundDoc);

            // Delete the index
            client.indices().delete(d -> d.index(indexName));

            transport.close();
        }
    }

    @Test
    void testSameContainerAcrossTests() {
        ElasticsearchContainer container1 = SharedElasticsearchContainer.getInstance();
        ElasticsearchContainer container2 = SharedElasticsearchContainer.getInstance();

        assertSame(container1, container2, "Should return the same container instance");
    }

    /** Test document class for Elasticsearch operations */
    public static class TestDocument {
        private String name;
        private int age;

        public TestDocument() {}

        public TestDocument(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        @Override
        public String toString() {
            return "TestDocument{name='" + name + "', age=" + age + "}";
        }
    }
}
