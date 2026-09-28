package io.github.loadup.retrytask.test;

import io.github.loadup.retrytask.facade.RetryTaskProcessor;
import io.github.loadup.retrytask.facade.model.RetryTaskContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * One isolated processor per test scenario. Each processor owns its counters and failure switch, so
 * jobs left running by one test never affect another.
 */
@Configuration
public class TestRetryTaskProcessors {

    public static final String IMMEDIATE = "immediate";
    public static final String SCHEDULED = "scheduled";
    public static final String CANCELLED = "cancelled";
    public static final String FAILING = "failing";
    public static final String RETRY_AGAIN = "retry-again";
    public static final String RESET = "reset";
    public static final String BLOCKING = "blocking";

    private final Map<String, ControlledProcessor> processors = new HashMap<>();

    @Bean
    public RetryTaskProcessor immediateProcessor() {
        return register(new ControlledProcessor(IMMEDIATE));
    }

    @Bean
    public RetryTaskProcessor scheduledProcessor() {
        return register(new ControlledProcessor(SCHEDULED));
    }

    @Bean
    public RetryTaskProcessor cancelledProcessor() {
        return register(new ControlledProcessor(CANCELLED));
    }

    @Bean
    public RetryTaskProcessor failingProcessor() {
        return register(new ControlledProcessor(FAILING, true));
    }

    @Bean
    public RetryTaskProcessor retryAgainProcessor() {
        return register(new ControlledProcessor(RETRY_AGAIN, true));
    }

    @Bean
    public RetryTaskProcessor resetProcessor() {
        return register(new ControlledProcessor(RESET, true));
    }

    @Bean
    public RetryTaskProcessor blockingProcessor() {
        return register(new ControlledProcessor(BLOCKING).withGate(new CountDownLatch(1)));
    }

    public ControlledProcessor processor(String bizType) {
        return processors.get(bizType);
    }

    private ControlledProcessor register(ControlledProcessor processor) {
        processors.put(processor.bizType(), processor);
        return processor;
    }

    /** Recorded, controllable {@link RetryTaskProcessor} used only by tests. */
    public static class ControlledProcessor implements RetryTaskProcessor {

        private final String bizType;
        private final AtomicInteger calls = new AtomicInteger();
        private final AtomicBoolean failOnCall;
        private final CopyOnWriteArrayList<RetryTaskContext> contexts = new CopyOnWriteArrayList<>();
        private CountDownLatch gate;

        private ControlledProcessor(String bizType) {
            this(bizType, false);
        }

        private ControlledProcessor(String bizType, boolean failOnCall) {
            this.bizType = bizType;
            this.failOnCall = new AtomicBoolean(failOnCall);
        }

        private ControlledProcessor withGate(CountDownLatch gate) {
            this.gate = gate;
            return this;
        }

        @Override
        public String bizType() {
            return bizType;
        }

        @Override
        public void process(RetryTaskContext context) throws Exception {
            calls.incrementAndGet();
            contexts.add(context);
            if (gate != null) {
                gate.await();
            }
            if (failOnCall.get()) {
                throw new IllegalStateException("forced failure for " + bizType);
            }
        }

        public int calls() {
            return calls.get();
        }

        public List<RetryTaskContext> contexts() {
            return List.copyOf(contexts);
        }

        public void setFailOnCall(boolean fail) {
            failOnCall.set(fail);
        }

        public void releaseGate() {
            if (gate != null) {
                gate.countDown();
            }
        }
    }
}
