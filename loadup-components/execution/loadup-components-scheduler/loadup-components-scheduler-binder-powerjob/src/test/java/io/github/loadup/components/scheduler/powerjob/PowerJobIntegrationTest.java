/*
 * #%L
 * LoadUp Components Scheduler Binder PowerJob
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
package io.github.loadup.components.scheduler.powerjob;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.github.loadup.commons.context.ContextHolder;
import io.github.loadup.commons.context.ContextKey;
import io.github.loadup.commons.context.ExecutionContext;
import io.github.loadup.commons.error.CommonException;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import io.micrometer.observation.ObservationRegistry;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import tech.powerjob.client.PowerJobClient;
import tech.powerjob.worker.core.processor.ProcessResult;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.core.processor.sdk.BasicProcessor;

class PowerJobIntegrationTest {
    @Test
    void rejectsBlankWorkerFallbackAndBlankClientPassword() {
        runner.withPropertyValues(
                        "powerjob.client.enabled=true",
                        "powerjob.client.server-address=server:7700",
                        "powerjob.worker.app-name= ",
                        "powerjob.client.password=synthetic-secret")
                .run(context -> assertThat(context.getStartupFailure()).hasRootCauseInstanceOf(CommonException.class));
        runner.withPropertyValues(
                        "powerjob.client.enabled=true",
                        "powerjob.client.server-address=server:7700",
                        "powerjob.client.app-name=test-app",
                        "powerjob.client.password= ")
                .run(context -> assertThat(context.getStartupFailure()).hasRootCauseInstanceOf(CommonException.class));
    }

    @Test
    void nativeClientUsesExplicitSettingsOrWorkerDefaultsAndCloses() throws Exception {
        for (boolean explicit : List.of(true, false)) {
            try (var construction = mockConstruction(PowerJobClient.class, (client, context) -> {
                assertThat(context.arguments())
                        .isEqualTo(List.of(List.of("server:7700", "backup:7700"), "test-app", "synthetic-secret"));
            })) {
                runner.withPropertyValues(
                                "powerjob.client.enabled=true",
                                "powerjob.client.password=synthetic-secret",
                                "powerjob.worker.server-address= server:7700, backup:7700 ",
                                "powerjob.worker.app-name=test-app",
                                "powerjob.client.server-address=" + (explicit ? " server:7700, backup:7700 " : " "),
                                "powerjob.client.app-name=" + (explicit ? "test-app" : " "))
                        .run(context -> {
                            assertThat(context).hasNotFailed().hasSingleBean(PowerJobClient.class);
                            assertThat(context.getBean(PowerJobClientProperties.class)
                                            .isEnabled())
                                    .isTrue();
                        });
                assertThat(construction.constructed()).hasSize(1);
                verify(construction.constructed().getFirst()).close();
            }
        }
    }

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(LoadUpPowerJobAutoConfiguration.class))
            .withPropertyValues("powerjob.worker.enabled=false");

    @Test
    void clientIsOptInAndCustomClientIsClosedBySpring() throws Exception {
        runner.run(context ->
                assertThat(context).doesNotHaveBean(PowerJobClient.class).hasSingleBean(PowerJobProcessorAspect.class));
        var client = mock(PowerJobClient.class);
        runner.withBean(PowerJobClient.class, () -> client, definition -> definition.setDestroyMethodName("close"))
                .withPropertyValues("powerjob.client.enabled=true")
                .run(context ->
                        assertThat(context.getBean(PowerJobClient.class)).isSameAs(client));
        verify(client).close();
    }

    @Test
    void rejectsMissingCredentialsAndSchemeAddressesWithoutLeakingSecrets() {
        for (String address : List.of("", "http://127.0.0.1:7700", "127.0.0.1:7700,")) {
            runner.withPropertyValues(
                            "powerjob.client.enabled=true",
                            "powerjob.client.app-name=test",
                            "powerjob.client.server-address=" + address,
                            "powerjob.client.password=synthetic-secret")
                    .run(context ->
                            assertThat(context.getStartupFailure()).hasRootCauseInstanceOf(CommonException.class));
        }
        runner.withPropertyValues(
                        "powerjob.client.enabled=true",
                        "powerjob.client.server-address=127.0.0.1:7700",
                        "powerjob.client.app-name=test")
                .run(context -> assertThat(context.getStartupFailure()).hasRootCauseInstanceOf(CommonException.class));
        var settings = new PowerJobClientProperties();
        settings.setPassword("synthetic-secret");
        assertThat(settings.toString()).doesNotContain("synthetic-secret");
    }

    @Test
    void scopesNativeIdentifiersAndRestoresOuterContext() throws Exception {
        var observations = ObservationRegistry.create();
        var recorder = new Recorder();
        observations.observationConfig().observationHandler(recorder);
        var outerKey = new ContextKey<>("test.outer", String.class);
        BasicProcessor processor = proxy(
                context -> {
                    assertThat(ContextHolder.get(PowerJobExecutionContext.JOB_ID))
                            .isEqualTo(12L);
                    assertThat(ContextHolder.get(PowerJobExecutionContext.INSTANCE_ID))
                            .isEqualTo(34L);
                    assertThat(ContextHolder.get(PowerJobExecutionContext.TASK_ID))
                            .isEqualTo("task-1");
                    assertThat(ContextHolder.get(PowerJobExecutionContext.RETRY_TIMES))
                            .isEqualTo(2);
                    assertThat(ContextHolder.get(outerKey)).isNull();
                    assertThat(observations.getCurrentObservation()).isNotNull();
                    return new ProcessResult(true);
                },
                observations);
        ContextHolder.callWith(ExecutionContext.empty().with(outerKey, "outer"), () -> {
            assertThat(processor.process(task()).isSuccess()).isTrue();
            assertThat(ContextHolder.get(outerKey)).isEqualTo("outer");
            return null;
        });
        assertThat(ContextHolder.isBound()).isFalse();
        assertThat(recorder.stopped).singleElement().satisfies(context -> {
            assertThat(context.getError()).isNull();
            assertThat(context.getLowCardinalityKeyValue("outcome").getValue()).isEqualTo("success");
            assertThat(context.getLowCardinalityKeyValue("jobId")).isNull();
        });
    }

    @Test
    void failedResultsAndNullResultsAreObservedAsFailures() throws Exception {
        var registry = ObservationRegistry.create();
        var recorder = new Recorder();
        registry.observationConfig().observationHandler(recorder);
        var failed = new ProcessResult(false, "synthetic-private-payload");
        assertThat(proxy(context -> failed, registry).process(task())).isSameAs(failed);
        assertThatThrownBy(() -> proxy(context -> null, registry).process(task()))
                .isInstanceOf(CommonException.class);
        assertThat(recorder.stopped).hasSize(2).allSatisfy(context -> {
            assertThat(context.getError()).isInstanceOf(CommonException.class);
            assertThat(context.getError().getMessage()).doesNotContain("synthetic-private-payload");
            assertThat(context.getLowCardinalityKeyValue("outcome").getValue()).isEqualTo("failure");
        });
        assertThat(ContextHolder.isBound()).isFalse();
    }

    @Test
    void preservesCheckedFailuresAndRestoresObservationScope() {
        var registry = ObservationRegistry.create();
        var recorder = new Recorder();
        registry.observationConfig().observationHandler(recorder);
        var failure = new Exception("synthetic-private-payload");
        assertThatThrownBy(() -> proxy(
                                context -> {
                                    throw failure;
                                },
                                registry)
                        .process(task()))
                .isSameAs(failure);
        assertThat(ContextHolder.isBound()).isFalse();
        assertThat(registry.getCurrentObservation()).isNull();
        assertThat(recorder.stopped)
                .singleElement()
                .satisfies(context -> assertThat(context.getError()).isNotSameAs(failure));
    }

    private static TaskContext task() {
        var task = new TaskContext();
        task.setJobId(12L);
        task.setInstanceId(34L);
        task.setTaskId("task-1");
        task.setCurrentRetryTimes(2);
        return task;
    }

    private static BasicProcessor proxy(BasicProcessor processor, ObservationRegistry registry) {
        var factory = new AspectJProxyFactory(processor);
        factory.addAspect(new PowerJobProcessorAspect(registry));
        return factory.getProxy();
    }

    private static class Recorder implements ObservationHandler<Observation.Context> {
        private final List<Observation.Context> stopped = new ArrayList<>();

        @Override
        public boolean supportsContext(Observation.Context context) {
            return true;
        }

        @Override
        public void onStop(Observation.Context context) {
            stopped.add(context);
        }
    }
}
