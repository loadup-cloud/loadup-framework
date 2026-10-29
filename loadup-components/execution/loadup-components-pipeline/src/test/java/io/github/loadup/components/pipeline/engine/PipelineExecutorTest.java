/*
 * #%L
 * Loadup Components Pipeline
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
package io.github.loadup.components.pipeline.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.loadup.components.pipeline.api.IBizProcessStage;
import io.github.loadup.components.pipeline.api.IControlStage;
import io.github.loadup.components.pipeline.api.IDataPrepareStage;
import io.github.loadup.components.pipeline.api.IDataProcessStage;
import io.github.loadup.components.pipeline.api.IFinallyStage;
import io.github.loadup.components.pipeline.api.IParamVerifyStage;
import io.github.loadup.components.pipeline.api.IPipelineDefinition;
import io.github.loadup.components.pipeline.api.IResultAssembleStage;
import io.github.loadup.components.pipeline.api.IStage;
import io.github.loadup.components.pipeline.builder.PipelineBuilder;
import io.github.loadup.components.pipeline.context.PipelineContext;
import io.github.loadup.components.pipeline.exception.ExceptionHandlerBus;
import io.github.loadup.components.pipeline.exception.IExceptionClassHandler;
import io.github.loadup.components.pipeline.spec.PipelineSpec;
import io.github.loadup.components.pipeline.tx.DefaultSpringTxInitializer;
import io.github.loadup.components.pipeline.tx.EndTxMarker;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

class PipelineExecutorTest {
    private final List<String> events = new ArrayList<>();
    private GenericApplicationContext application;
    private PipelineExecutor executor;

    private static IPipelineDefinition definition(PipelineSpec spec, ExceptionHandlerBus bus) {
        return new IPipelineDefinition() {
            @Override
            public PipelineSpec definePipeline() {
                return spec;
            }

            @Override
            public ExceptionHandlerBus exceptions() {
                return bus;
            }
        };
    }

    @SafeVarargs
    private static PipelineSpec stages(Class<? extends IStage>... classes) {
        return new PipelineSpec(List.of(classes), Map.of(), Map.of(), Map.of());
    }

    private static PipelineSpec failing() {
        return PipelineBuilder.builder()
                .process(ctx -> {
                    throw new IllegalArgumentException("failure");
                })
                .build();
    }

    @BeforeEach
    void setUp() {
        application = new GenericApplicationContext();
        application.refresh();
        executor = new PipelineExecutor(application);
    }

    @AfterEach
    void closeContext() {
        application.close();
    }

    @Test
    void executesInlineStagesInOrderWithSharedModelAndIsolatedInvocations() {
        PipelineSpec spec = PipelineBuilder.builder()
                .verify((String request) -> events.add("verify:" + request))
                .prepare(ctx -> new StringBuilder(ctx.getRequest(String.class)))
                .process((StringBuilder model, PipelineContext ctx) -> {
                    events.add("data:" + model);
                    model.append("-processed");
                    ctx.putProperty("marker", "present");
                    ctx.putProperty("marker", null);
                    assertThat((Object) ctx.getProperty("marker")).isNull();
                })
                .process(ctx -> {
                    events.add("business");
                    assertThat(ctx.getModelClass()).isEqualTo(StringBuilder.class);
                })
                .prepare(ctx -> null)
                .assemble(ctx -> ctx.getModel(StringBuilder.class).toString())
                .build();
        assertThat(execute(spec, "first")).isEqualTo("first-processed");
        assertThat(execute(spec, "second")).isEqualTo("second-processed");
        assertThat(events)
                .containsExactly("verify:first", "data:first", "business", "verify:second", "data:second", "business");
    }

    @Test
    void resolvesSpringStagesThroughTheSamePublicBoundary() {
        register("verify", new Verify());
        register("prepare", new Prepare());
        register("data", new DataProcess());
        register("business", new Business());
        register("assemble", new Assemble());
        PipelineSpec spec = PipelineBuilder.builder()
                .verify(Verify.class)
                .prepare(Prepare.class)
                .process(DataProcess.class)
                .process(Business.class)
                .prepare(Prepare.class)
                .assemble(Assemble.class)
                .build();
        assertThat(execute(spec, "input")).isEqualTo("input");
        assertThat(events).containsExactly("verify", "prepare", "data", "business", "prepare", "assemble");
    }

    @Test
    void stopsAtControlStageButStillRunsPreviouslyRegisteredCleanup() {
        register("cleanup", new Cleanup());
        register("control", new Stop());
        register("business", new Business());
        PipelineSpec spec = stages(Cleanup.class, Stop.class, Business.class);
        assertThat(execute(spec, "request")).isNull();
        assertThat(events).containsExactly("cleanup");
    }

    @Test
    void continuesWhenControlStageAllowsIt() {
        register("control", new Continue());
        register("assemble", new Assemble());
        assertThat(execute(stages(Continue.class, Assemble.class), "request")).isEqualTo("request");
        assertThat(events).containsExactly("assemble");
    }

    @Test
    void cleanupFailureDoesNotReplaceSuccessfulResultOrSkipLaterCleanup() {
        register("brokenCleanup", new BrokenCleanup());
        register("cleanup", new Cleanup());
        register("assemble", new Assemble());
        assertThat(execute(stages(BrokenCleanup.class, Cleanup.class, Assemble.class), "result"))
                .isEqualTo("result");
        assertThat(events).containsExactly("assemble", "broken-cleanup", "cleanup");
    }

    @Test
    void choosesExactExceptionHandlerBeforeSuperclassHandler() {
        register("exact", new ExactHandler());
        register("fallback", new FallbackHandler());
        ExceptionHandlerBus bus = ExceptionHandlerBus.builder()
                .register(RuntimeException.class, FallbackHandler.class)
                .register(IllegalArgumentException.class, ExactHandler.class);
        assertThat(execute(failing(), bus)).isEqualTo("exact");
        assertThat(events).containsExactly("handle-exact", "assemble-exact");
    }

    @Test
    void usesSuperclassHandlerWhenExactHandlerIsMissing() {
        register("fallback", new FallbackHandler());
        ExceptionHandlerBus bus = ExceptionHandlerBus.builder().register(RuntimeException.class, FallbackHandler.class);
        assertThat(execute(failing(), bus)).isEqualTo("fallback");
    }

    @Test
    void usesBottomHandlerForUnmatchedException() {
        register("fallback", new FallbackHandler());
        ExceptionHandlerBus bus = ExceptionHandlerBus.builder()
                .register(UnsupportedOperationException.class, ExactHandler.class)
                .bottom(FallbackHandler.class);
        assertThat(execute(failing(), bus)).isEqualTo("fallback");
    }

    @Test
    void overflowBypassesHandlersAndPreservesOriginalException() {
        IllegalArgumentException failure = new IllegalArgumentException("failure");
        PipelineSpec spec = PipelineBuilder.builder()
                .process(ctx -> {
                    throw failure;
                })
                .build();
        ExceptionHandlerBus bus =
                ExceptionHandlerBus.builder().overflow(RuntimeException.class).bottom(FallbackHandler.class);
        assertThatThrownBy(() -> execute(spec, bus)).isSameAs(failure);
        assertThat(events).isEmpty();
    }

    @Test
    void unhandledExceptionPropagatesAndStillRunsCleanup() {
        register("cleanup", new Cleanup());
        register("failure", new Failure());
        assertThatThrownBy(() -> execute(stages(Cleanup.class, Failure.class), "request"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(events).containsExactly("cleanup");
    }

    @Test
    void commitsTransactionBeforeExecutingPostTransactionStages() {
        RecordingTransactions transactions = new RecordingTransactions();
        register("transactions", transactions);
        register("initializer", new DefaultSpringTxInitializer());
        register("cleanup", new Cleanup());
        register("business", new Business());
        PipelineSpec spec = PipelineBuilder.builder()
                .verify((String request) -> assertThat(request).isEqualTo("request"))
                .startTx(DefaultSpringTxInitializer.class)
                .prepare(ctx -> "model")
                .process(
                        (String model, PipelineContext ctx) -> assertThat(model).isEqualTo("model"))
                .process(Business.class)
                .endTx()
                .process(ctx -> events.add("after"))
                .assemble(ctx -> ctx.getModel(String.class))
                .build();
        assertThat(execute(spec, "request")).isEqualTo("model");
        assertThat(events).containsExactly("begin", "business", "commit", "after");
        assertThat(transactions.definition.getPropagationBehavior())
                .isEqualTo(TransactionDefinition.PROPAGATION_REQUIRED);
    }

    @Test
    void rollsBackBeforeAssemblingHandledFailureAndRunsTransactionCleanup() {
        register("transactions", new RecordingTransactions());
        register("initializer", new DefaultSpringTxInitializer());
        register("cleanup", new Cleanup());
        register("failure", new Failure());
        register("exact", new ExactHandler());
        PipelineSpec spec = stages(DefaultSpringTxInitializer.class, Cleanup.class, Failure.class, EndTxMarker.class);
        assertThat(execute(spec, ExceptionHandlerBus.builder().bottom(ExactHandler.class)))
                .isEqualTo("exact");
        assertThat(events).containsExactly("begin", "rollback", "handle-exact", "assemble-exact", "cleanup");
    }

    @Test
    void implicitTransactionEndCommitsAndHonorsStopInsideBlock() {
        register("transactions", new RecordingTransactions());
        register("initializer", new DefaultSpringTxInitializer());
        register("control", new Stop());
        register("business", new Business());
        register("cleanup", new Cleanup());
        assertThat(execute(
                        stages(DefaultSpringTxInitializer.class, Cleanup.class, Stop.class, Business.class), "request"))
                .isNull();
        assertThat(events).containsExactly("begin", "commit", "cleanup");
    }

    @Test
    void builtSpecificationRemainsImmutableAfterBuilderChanges() {
        var builder = PipelineBuilder.builder().process(ctx -> ctx.putProperty("value", "original"));
        PipelineSpec first = builder.build();
        builder.process(ctx -> ctx.putProperty("value", "later"));
        assertThat(first.getStageList()).hasSize(1);
        assertThatThrownBy(() -> first.getStageList().clear()).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> first.getConsumerIndexMap().clear()).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> first.getFunctionIndexMap().clear()).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> first.getBiConsumerIndexMap().clear())
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void skippedPreparationExposesNullModelAndNoResponse() {
        PipelineSpec spec = PipelineBuilder.builder()
                .process(ctx -> {
                    assertThat(ctx.getModel(String.class)).isNull();
                    assertThat(ctx.getModel()).isNull();
                    assertThat(ctx.getModelClass()).isNull();
                    assertThat(ctx.getResponse(String.class)).isNull();
                })
                .build();
        assertThat(execute(spec, "request")).isNull();
    }

    @Test
    void optionalStagesSupportBeanAndLambdaEntryPoints() {
        register("verify", new Verify());
        register("prepare", new Prepare());
        register("business", new Business());
        register("assemble", new Assemble());
        List<PipelineSpec> definitions = List.of(
                PipelineBuilder.builder()
                        .prepare(Prepare.class)
                        .assemble(Assemble.class)
                        .build(),
                PipelineBuilder.builder()
                        .prepare(ctx -> ctx.getRequest())
                        .assemble(ctx -> ctx.getModel())
                        .build(),
                PipelineBuilder.builder()
                        .process(Business.class)
                        .assemble(Assemble.class)
                        .build(),
                PipelineBuilder.builder()
                        .process((Object model, PipelineContext ctx) ->
                                assertThat(model).isNull())
                        .assemble(ctx -> ctx.getRequest())
                        .build(),
                PipelineBuilder.builder()
                        .verify(Verify.class)
                        .process(Business.class)
                        .assemble(Assemble.class)
                        .build(),
                PipelineBuilder.builder()
                        .verify(Verify.class)
                        .process(ctx -> ctx.putProperty("checked", true))
                        .assemble(Assemble.class)
                        .build(),
                PipelineBuilder.builder()
                        .verify(Verify.class)
                        .process((Object model, PipelineContext ctx) ->
                                assertThat(model).isNull())
                        .assemble(Assemble.class)
                        .build(),
                PipelineBuilder.builder()
                        .verify(Verify.class)
                        .assemble(Assemble.class)
                        .build(),
                PipelineBuilder.builder()
                        .verify(Verify.class)
                        .assemble(ctx -> ctx.getRequest())
                        .build());
        for (PipelineSpec spec : definitions) {
            assertThat(execute(spec, "request")).isEqualTo("request");
        }
    }

    @Test
    void transactionBuilderSupportsBothEntryPointsAndBeanPreparation() {
        register("transactions", new RecordingTransactions());
        register("initializer", new DefaultSpringTxInitializer());
        register("prepare", new Prepare());
        register("business", new Business());
        PipelineSpec direct = PipelineBuilder.builder()
                .startTx(DefaultSpringTxInitializer.class)
                .prepare(Prepare.class)
                .process(ctx -> events.add("inline"))
                .endTx()
                .assemble(ctx -> ctx.getModel(String.class))
                .build();
        assertThat(execute(direct, "request")).isEqualTo("request");
        assertThat(events).containsExactly("begin", "prepare", "inline", "commit");
        events.clear();
        PipelineSpec afterProcess = PipelineBuilder.builder()
                .prepare(ctx -> ctx.getRequest())
                .startTx(DefaultSpringTxInitializer.class)
                .process(Business.class)
                .endTx()
                .assemble(ctx -> ctx.getModel(String.class))
                .build();
        assertThat(execute(afterProcess, "request")).isEqualTo("request");
        assertThat(events).containsExactly("begin", "business", "commit");
    }

    private Object execute(PipelineSpec spec, Object request) {
        return executor.execute(definition(spec, ExceptionHandlerBus.builder()), request);
    }

    private Object execute(PipelineSpec spec, ExceptionHandlerBus bus) {
        return executor.execute(definition(spec, bus), "request");
    }

    private void register(String name, Object bean) {
        application.getBeanFactory().registerSingleton(name, bean);
    }

    static class Stop implements IControlStage {
        @Override
        public boolean shouldStop(PipelineContext ctx) {
            return true;
        }
    }

    static class Continue implements IControlStage {
        @Override
        public boolean shouldStop(PipelineContext ctx) {
            return false;
        }
    }

    static class Failure implements IBizProcessStage {
        @Override
        public void process(PipelineContext ctx) {
            throw new IllegalArgumentException("failure");
        }
    }

    static class FallbackHandler implements IExceptionClassHandler<String> {
        @Override
        public String assembleResultOnException(Throwable failure, PipelineContext ctx) {
            return "fallback";
        }
    }

    class Verify implements IParamVerifyStage<String> {
        @Override
        public void verify(String request) {
            events.add("verify");
        }
    }

    class Prepare implements IDataPrepareStage<String> {
        @Override
        public String prepare(PipelineContext ctx) {
            events.add("prepare");
            return ctx.getRequest(String.class);
        }
    }

    class DataProcess implements IDataProcessStage<String> {
        @Override
        public void process(String model, PipelineContext ctx) {
            events.add("data");
        }
    }

    class Business implements IBizProcessStage {
        @Override
        public void process(PipelineContext ctx) {
            events.add("business");
        }
    }

    class Assemble implements IResultAssembleStage<String> {
        @Override
        public String assemble(PipelineContext ctx) {
            events.add("assemble");
            return ctx.getRequest(String.class);
        }
    }

    class Cleanup implements IFinallyStage {
        @Override
        public void doFinally(PipelineContext ctx) {
            events.add("cleanup");
        }
    }

    class BrokenCleanup implements IFinallyStage {
        @Override
        public void doFinally(PipelineContext ctx) {
            events.add("broken-cleanup");
            throw new IllegalStateException("cleanup failure");
        }
    }

    class ExactHandler implements IExceptionClassHandler<String> {
        @Override
        public void handleException(Throwable failure, PipelineContext ctx) {
            events.add("handle-exact");
        }

        @Override
        public String assembleResultOnException(Throwable failure, PipelineContext ctx) {
            events.add("assemble-exact");
            return "exact";
        }
    }

    class RecordingTransactions extends AbstractPlatformTransactionManager {
        TransactionDefinition definition;

        @Override
        protected Object doGetTransaction() {
            return new Object();
        }

        @Override
        protected void doBegin(Object tx, TransactionDefinition definition) {
            this.definition = definition;
            events.add("begin");
        }

        @Override
        protected void doCommit(DefaultTransactionStatus status) {
            events.add("commit");
        }

        @Override
        protected void doRollback(DefaultTransactionStatus status) {
            events.add("rollback");
        }
    }
}
