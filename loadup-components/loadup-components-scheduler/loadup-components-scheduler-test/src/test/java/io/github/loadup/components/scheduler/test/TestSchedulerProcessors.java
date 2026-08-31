package io.github.loadup.components.scheduler.test;

import io.github.loadup.components.scheduler.SchedulerProcessor;
import io.github.loadup.components.scheduler.model.SchedulerContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * One isolated processor per test scenario. Each processor owns its counters and optional gate, so
 * recurring runs left behind by one test never affect another.
 */
@Configuration
public class TestSchedulerProcessors {

    public static final String IMMEDIATE = "immediate";
    public static final String IDEMPOTENT = "idempotent";
    public static final String TRIGGER = "trigger";
    public static final String CRON_UPDATE = "cron-update";
    public static final String DELETE = "delete";

    private final Map<String, ControlledProcessor> processors = new HashMap<>();

    @Bean
    public SchedulerProcessor immediateProcessor() {
        return register(new ControlledProcessor(IMMEDIATE));
    }

    @Bean
    public SchedulerProcessor idempotentProcessor() {
        return register(new ControlledProcessor(IDEMPOTENT).withGate(new CountDownLatch(1)));
    }

    @Bean
    public SchedulerProcessor triggerProcessor() {
        return register(new ControlledProcessor(TRIGGER));
    }

    @Bean
    public SchedulerProcessor cronUpdateProcessor() {
        return register(new ControlledProcessor(CRON_UPDATE));
    }

    @Bean
    public SchedulerProcessor deleteProcessor() {
        return register(new ControlledProcessor(DELETE));
    }

    public ControlledProcessor processor(String taskName) {
        return processors.get(taskName);
    }

    private ControlledProcessor register(ControlledProcessor processor) {
        processors.put(processor.taskName(), processor);
        return processor;
    }

    /** Recorded, controllable {@link SchedulerProcessor} used only by tests. */
    public static class ControlledProcessor implements SchedulerProcessor {

        private final String taskName;
        private final AtomicInteger calls = new AtomicInteger();
        private final CopyOnWriteArrayList<SchedulerContext> contexts = new CopyOnWriteArrayList<>();
        private CountDownLatch gate;

        private ControlledProcessor(String taskName) {
            this.taskName = taskName;
        }

        private ControlledProcessor withGate(CountDownLatch gate) {
            this.gate = gate;
            return this;
        }

        @Override
        public String taskName() {
            return taskName;
        }

        @Override
        public void process(SchedulerContext context) throws Exception {
            calls.incrementAndGet();
            contexts.add(context);
            if (gate != null) {
                gate.await();
            }
        }

        public int calls() {
            return calls.get();
        }

        public List<SchedulerContext> contexts() {
            return List.copyOf(contexts);
        }

        public void releaseGate() {
            if (gate != null) {
                gate.countDown();
            }
        }
    }
}
