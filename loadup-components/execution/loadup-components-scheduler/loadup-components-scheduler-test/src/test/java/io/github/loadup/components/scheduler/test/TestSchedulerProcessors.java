/*
 * #%L
 * Loadup Components Scheduler Test
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
