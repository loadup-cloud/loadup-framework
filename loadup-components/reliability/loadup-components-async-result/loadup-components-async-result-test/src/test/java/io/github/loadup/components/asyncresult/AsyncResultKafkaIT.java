/*
 * #%L
 * LoadUp Async Result Test
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
package io.github.loadup.components.asyncresult;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.loadup.components.asyncresult.redis.RedisAsyncResultProvider;
import io.github.loadup.components.testcontainers.annotation.ContainerType;
import io.github.loadup.components.testcontainers.annotation.EnableTestContainers;
import io.github.loadup.components.testcontainers.messaging.SharedKafkaContainer;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest(classes = AsyncResultKafkaIT.Application.class)
@ActiveProfiles("test")
@EnableTestContainers(
        value = {ContainerType.REDIS, ContainerType.KAFKA},
        reuse = false)
class AsyncResultKafkaIT {
    private static KafkaConsumer<String, String> consumer(String broker, String topic) {
        var consumer = new KafkaConsumer<String, String>(Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                broker,
                ConsumerConfig.GROUP_ID_CONFIG,
                UUID.randomUUID().toString(),
                ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,
                false,
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                "earliest",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class));
        consumer.assign(List.of(new TopicPartition(topic, 0)));
        return consumer;
    }

    private static ConsumerRecord<String, String> receive(KafkaConsumer<String, String> consumer) {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(15);
        while (System.nanoTime() < deadline) {
            var records = consumer.poll(Duration.ofMillis(100));
            if (!records.isEmpty()) {
                return records.iterator().next();
            }
        }
        throw new AssertionError("MQ message did not arrive");
    }

    @Test
    void kafkaRequestAndReplyCompleteTheOriginalServiceCallOnAnotherInstance() throws Exception {
        String broker = SharedKafkaContainer.getBootstrapServers();
        String request = "async-request-" + UUID.randomUUID();
        String reply = "async-reply-" + UUID.randomUUID();
        var mapper = JsonMapper.builder().build();
        var redisA = RedisAsyncResultIT.client();
        var redisB = RedisAsyncResultIT.client();
        var namespace = UUID.randomUUID().toString();
        try (var admin = AdminClient.create(Map.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, broker))) {
            admin.createTopics(List.of(new NewTopic(request, 1, (short) 1), new NewTopic(reply, 1, (short) 1)))
                    .all()
                    .get(20, TimeUnit.SECONDS);
        }
        try (var producer = new KafkaProducer<String, String>(Map.of(
                        ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                        broker,
                        ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                        StringSerializer.class,
                        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                        StringSerializer.class));
                var executor = Executors.newVirtualThreadPerTaskExecutor();
                var caller = AsyncResultTest.template(
                        new RedisAsyncResultProvider(redisA, namespace),
                        new AsyncResultProperties(
                                true,
                                "redis",
                                namespace,
                                Duration.ofSeconds(15),
                                Duration.ofSeconds(30),
                                10,
                                Duration.ofMinutes(1),
                                Duration.ofMillis(20),
                                1024,
                                100));
                var receiver = AsyncResultTest.template(
                        new RedisAsyncResultProvider(redisB, namespace),
                        new AsyncResultProperties(
                                true,
                                "redis",
                                namespace,
                                Duration.ofSeconds(15),
                                Duration.ofSeconds(30),
                                10,
                                Duration.ofMinutes(1),
                                Duration.ofMillis(20),
                                1024,
                                100))) {
            var consumerTask = executor.submit(() -> {
                try (var consumer = consumer(broker, request)) {
                    var record = receive(consumer);
                    producer.send(new ProducerRecord<>(reply, record.key(), "processed:" + record.value()))
                            .get(5, TimeUnit.SECONDS);
                    return true;
                }
            });
            var replyTask = executor.submit(() -> {
                try (var consumer = consumer(broker, reply)) {
                    var record = receive(consumer);
                    return receiver.complete(mapper.readValue(record.key(), AsyncReplyKey.class), record.value());
                }
            });
            String result = caller.execute(
                    new AsyncResultOptions("tenant", "echo.kafka.v1", "order", Duration.ofSeconds(15)),
                    String.class,
                    key -> {
                        var sent = new CompletableFuture<Void>();
                        producer.send(
                                new ProducerRecord<>(request, mapper.writeValueAsString(key), "order"),
                                (metadata, error) -> {
                                    if (error == null) {
                                        sent.complete(null);
                                    } else {
                                        sent.completeExceptionally(error);
                                    }
                                });
                        return sent;
                    });
            assertThat(result).isEqualTo("processed:order");
            assertThat(consumerTask.get(10, TimeUnit.SECONDS)).isTrue();
            assertThat(replyTask.get(10, TimeUnit.SECONDS)).isEqualTo(AsyncCompletionStatus.ACCEPTED);
        } finally {
            redisA.shutdown();
            redisB.shutdown();
        }
    }

    @SpringBootConfiguration
    static class Application {}
}
