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

import io.github.loadup.components.testcontainers.messaging.AbstractKafkaContainerTest;
import io.github.loadup.components.testcontainers.messaging.SharedKafkaContainer;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.kafka.KafkaContainer;

/**
 * Integration test class for SharedKafkaContainer.
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Slf4j
@SpringBootTest(classes = TestApplication.class)
@TestPropertySource(properties = {"loadup.testcontainers.enabled=true", "loadup.testcontainers.kafka.enabled=true"})
class SharedKafkaContainerIT extends AbstractKafkaContainerTest {

    @Test
    void testContainerIsRunning() {
        KafkaContainer container = SharedKafkaContainer.getInstance();
        assertNotNull(container, "Container should not be null");
        assertTrue(container.isRunning(), "Container should be running");
    }

    @Test
    void testContainerProperties() {
        assertNotNull(SharedKafkaContainer.getBootstrapServers(), "Bootstrap servers should not be null");

        log.info("Bootstrap Servers: {}", SharedKafkaContainer.getBootstrapServers());
    }

    @Test
    void testKafkaProducerConsumer() throws Exception {
        String bootstrapServers = SharedKafkaContainer.getBootstrapServers();
        String topicName = "test-topic-" + UUID.randomUUID();
        String testMessage = "test-message";

        // Producer configuration
        Properties producerProps = new Properties();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // Send a message
        try (KafkaProducer<String, String> producer = new KafkaProducer<>(producerProps)) {
            ProducerRecord<String, String> record = new ProducerRecord<>(topicName, "key", testMessage);
            producer.send(record).get();
            log.info("Message sent to topic: {}", topicName);
        }

        // Consumer configuration
        Properties consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group-" + UUID.randomUUID());
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        // Consume the message
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps)) {
            consumer.subscribe(Collections.singletonList(topicName));

            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(10));
            assertFalse(records.isEmpty(), "Should receive at least one message");

            ConsumerRecord<String, String> record = records.iterator().next();
            assertEquals(testMessage, record.value(), "Message value should match");
            log.info("Message received: {}", record.value());
        }
    }

    @Test
    void testSameContainerAcrossTests() {
        KafkaContainer container1 = SharedKafkaContainer.getInstance();
        KafkaContainer container2 = SharedKafkaContainer.getInstance();

        assertSame(container1, container2, "Should return the same container instance");
    }
}
