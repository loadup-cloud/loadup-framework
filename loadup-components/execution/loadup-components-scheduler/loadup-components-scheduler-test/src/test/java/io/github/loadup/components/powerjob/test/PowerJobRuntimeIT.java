/*
 * #%L
 * LoadUp Components PowerJob Test
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
package io.github.loadup.components.powerjob.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import tech.powerjob.client.PowerJobClient;
import tech.powerjob.common.enums.ExecuteType;
import tech.powerjob.common.enums.InstanceStatus;
import tech.powerjob.common.enums.ProcessorType;
import tech.powerjob.common.enums.SwitchableStatus;
import tech.powerjob.common.enums.TimeExpressionType;
import tech.powerjob.common.request.http.SaveJobInfoRequest;
import tech.powerjob.common.response.ResultDTO;
import tech.powerjob.worker.core.processor.ProcessResult;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.core.processor.sdk.BasicProcessor;

/** Exercises the vendor SDK and real worker with production LoadUp instrumentation. */
class PowerJobRuntimeIT {
    private static final PowerJobServer SERVER = new PowerJobServer();
    private static final Map<Long, AtomicInteger> ATTEMPTS = new ConcurrentHashMap<>();
    private static final Map<Long, String> PAYLOADS = new ConcurrentHashMap<>();
    private static final AtomicInteger SCANS = new AtomicInteger();
    private static PowerJobClient client;
    private static ConfigurableApplicationContext worker;

    @BeforeAll
    static void start() throws Exception {
        try {
            SERVER.start();
            worker = new SpringApplicationBuilder(Application.class)
                    .properties(Map.of("powerjob.client.enabled", true, "powerjob.client.password", SERVER.appPassword))
                    .web(WebApplicationType.NONE)
                    .run(
                            "--loadup.scheduler.binder-type=powerjob",
                            "--powerjob.worker.app-name=" + SERVER.appName,
                            "--powerjob.worker.server-address=" + SERVER.address(),
                            "--powerjob.worker.port=" + PowerJobServer.freePort(),
                            "--powerjob.worker.protocol=HTTP",
                            "--powerjob.worker.store-strategy=MEMORY",
                            "--powerjob.worker.health-report-interval=1",
                            "--spring.main.banner-mode=off");
            SERVER.awaitWorker();
            client = worker.getBean(PowerJobClient.class);
            assertThat(org.springframework.aop.support.AopUtils.isAopProxy(worker.getBean("pocProcessor")))
                    .isTrue();
        } catch (Exception | Error e) {
            close();
            throw e;
        }
    }

    @AfterAll
    static void close() throws Exception {
        try {
            // The Spring context owns the native client lifecycle.
        } finally {
            try {
                if (worker != null) worker.close();
            } finally {
                SERVER.close();
            }
        }
    }

    @Test
    void schedulerBinderInvokesRegisteredScanner() {
        var request = request("scanner", TimeExpressionType.API, "");
        request.setProcessorInfo("loadupSchedulerProcessor");
        request.setJobParams("poc.scan");
        int before = SCANS.get();
        long job = data(client.saveJob(request));
        try {
            awaitStatus(data(client.runJob(job)), InstanceStatus.SUCCEED);
            assertThat(SCANS.get()).isEqualTo(before + 1);
        } finally {
            deleteJob(job);
        }
    }

    @Test
    void manualExecutionPreservesPayload() {
        long job = data(client.saveJob(request("manual", TimeExpressionType.API, "")));
        try {
            long instance = data(client.runJob(job, "synthetic-payload", 0));
            awaitStatus(instance, InstanceStatus.SUCCEED);
            assertThat(PAYLOADS.get(instance)).isEqualTo("synthetic-payload");
            assertThat(ATTEMPTS.get(instance).get()).isEqualTo(1);
        } finally {
            deleteJob(job);
        }
    }

    @Test
    void cronUpdateUsesSameIdentityAndExecutes() {
        var request = request("dynamic-cron", TimeExpressionType.CRON, "0 0 0 1 1 ? 2099");
        long job = data(client.saveJob(request));
        try {
            data(client.disableJob(job));
            assertThat(data(client.fetchJob(job)).getId()).isEqualTo(job);
            assertThat(data(client.fetchJob(job)).getStatus()).isEqualTo(SwitchableStatus.DISABLE.getV());
            request.setId(job);
            request.setTimeExpression("*/5 * * * * ?");
            data(client.saveJob(request));
            data(client.enableJob(job));
            await().atMost(Duration.ofSeconds(60))
                    .untilAsserted(() -> assertThat(ATTEMPTS.keySet())
                            .anySatisfy(instance -> assertThat(data(client.fetchInstanceInfo(instance))
                                            .getJobId())
                                    .isEqualTo(job)));
            data(client.disableJob(job));
            assertThat(data(client.fetchJob(job)).getTimeExpression()).isEqualTo("*/5 * * * * ?");
        } finally {
            deleteJob(job);
        }
    }

    @Test
    void nativeRetryAndTerminalInstanceRecovery() {
        var request = request("retry-once", TimeExpressionType.API, "");
        request.setInstanceRetryNum(1);
        long job = data(client.saveJob(request));
        try {
            long instance = data(client.runJob(job, "fail-once", 0));
            awaitStatus(instance, InstanceStatus.SUCCEED);
            assertThat(ATTEMPTS.get(instance).get()).isEqualTo(2);
            request.setId(job);
            request.setInstanceRetryNum(0);
            data(client.saveJob(request));
            long failed = data(client.runJob(job, "fail-once", 0));
            awaitStatus(failed, InstanceStatus.FAILED);
            data(client.retryInstance(failed));
            awaitStatus(failed, InstanceStatus.SUCCEED);
            assertThat(ATTEMPTS.get(failed).get()).isEqualTo(2);
        } finally {
            deleteJob(job);
        }
    }

    @Test
    void serverRestartRetainsJobDefinition() throws Exception {
        long job = data(client.saveJob(request("durable-definition", TimeExpressionType.API, "")));
        try {
            SERVER.restart();
            SERVER.awaitWorker();
            assertThat(data(client.fetchJob(job)).getId()).isEqualTo(job);
            long instance = data(client.runJob(job));
            awaitStatus(instance, InstanceStatus.SUCCEED);
        } finally {
            deleteJob(job);
        }
    }

    private static SaveJobInfoRequest request(String name, TimeExpressionType type, String expression) {
        var request = new SaveJobInfoRequest();
        request.setJobName(name);
        request.setTimeExpressionType(type);
        request.setTimeExpression(expression);
        request.setExecuteType(ExecuteType.STANDALONE);
        request.setProcessorType(ProcessorType.BUILT_IN);
        request.setProcessorInfo("pocProcessor");
        request.setConcurrency(1);
        request.setMaxInstanceNum(1);
        request.setInstanceTimeLimit(30_000L);
        return request;
    }

    private static void deleteJob(long job) {
        data(client.deleteJob(job));
        assertThat(data(client.fetchJob(job)).getStatus()).isEqualTo(SwitchableStatus.DELETED.getV());
    }

    private static <T> T data(ResultDTO<T> result) {
        assertThat(result.isSuccess())
                .as("PowerJob SDK response: %s", result.getMessage())
                .isTrue();
        return result.getData();
    }

    private static void awaitStatus(long instance, InstanceStatus status) {
        await().atMost(Duration.ofSeconds(90))
                .pollInterval(Duration.ofSeconds(1))
                .untilAsserted(() ->
                        assertThat(data(client.fetchInstanceStatus(instance))).isEqualTo(status.getV()));
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class Application {
        @Bean
        io.github.loadup.components.scheduler.ScheduledTask scanner() {
            return new io.github.loadup.components.scheduler.ScheduledTask() {
                public String name() {
                    return "poc.scan";
                }

                public void execute() {
                    SCANS.incrementAndGet();
                }
            };
        }

        @Bean
        io.micrometer.observation.ObservationRegistry observationRegistry() {
            var registry = io.micrometer.observation.ObservationRegistry.create();
            registry.observationConfig()
                    .observationHandler(
                            new io.micrometer.observation.ObservationHandler<
                                    io.micrometer.observation.Observation.Context>() {
                                public boolean supportsContext(io.micrometer.observation.Observation.Context context) {
                                    return true;
                                }
                            });
            return registry;
        }

        @Bean
        BasicProcessor pocProcessor() {
            return new PocProcessor();
        }
    }

    static class PocProcessor implements BasicProcessor {
        @Override
        public ProcessResult process(TaskContext context) {
            assertThat(io.github.loadup.commons.context.ContextHolder.get(
                            io.github.loadup.components.powerjob.PowerJobExecutionContext.INSTANCE_ID))
                    .isEqualTo(context.getInstanceId());
            int attempt = ATTEMPTS.computeIfAbsent(context.getInstanceId(), ignored -> new AtomicInteger())
                    .incrementAndGet();
            if (context.getInstanceParams() != null) PAYLOADS.put(context.getInstanceId(), context.getInstanceParams());
            boolean success = !"fail-once".equals(context.getInstanceParams()) || attempt > 1;
            return new ProcessResult(success, success ? "synthetic-success" : "synthetic-failure");
        }
    }
}
