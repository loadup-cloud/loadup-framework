/*-
 * #%L
 * Loadup Components Retrytask Test
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

package io.github.loadup.retrytask.test;

import io.github.loadup.retrytask.facade.RetryTaskProcessor;
import io.github.loadup.retrytask.facade.model.RetryTaskContext;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Test processors that record invocations and can be switched between success and failure. */
@Configuration
public class TestRetryTaskProcessors {

    public static final String RECORDING = "recording";
    public static final String FAILING = "failing";
    public static final String BLOCKING = "blocking";

    private final AtomicInteger recordingCalls = new AtomicInteger();
    private final CopyOnWriteArrayList<RetryTaskContext> recordingContexts = new CopyOnWriteArrayList<>();
    private final AtomicBoolean failRecording = new AtomicBoolean();

    private final AtomicInteger failingCalls = new AtomicInteger();
    private final AtomicBoolean failFailing = new AtomicBoolean(true);
    private final CopyOnWriteArrayList<RetryTaskContext> failingContexts = new CopyOnWriteArrayList<>();

    private final AtomicInteger blockingCalls = new AtomicInteger();
    private final CountDownLatch blockingGate = new CountDownLatch(1);

    @Bean
    public RetryTaskProcessor recordingRetryTaskProcessor() {
        return new RetryTaskProcessor() {
            @Override
            public String bizType() {
                return RECORDING;
            }

            @Override
            public void process(RetryTaskContext context) throws Exception {
                recordingCalls.incrementAndGet();
                recordingContexts.add(context);
                if (failRecording.get()) {
                    throw new IllegalStateException("forced recording failure");
                }
            }
        };
    }

    @Bean
    public RetryTaskProcessor failingRetryTaskProcessor() {
        return new RetryTaskProcessor() {
            @Override
            public String bizType() {
                return FAILING;
            }

            @Override
            public void process(RetryTaskContext context) throws Exception {
                failingCalls.incrementAndGet();
                failingContexts.add(context);
                if (failFailing.get()) {
                    throw new IllegalStateException("forced failing failure");
                }
            }
        };
    }

    @Bean
    public RetryTaskProcessor blockingRetryTaskProcessor() {
        return new RetryTaskProcessor() {
            @Override
            public String bizType() {
                return BLOCKING;
            }

            @Override
            public void process(RetryTaskContext context) throws Exception {
                blockingCalls.incrementAndGet();
                blockingGate.await();
            }
        };
    }

    public int recordingCalls() {
        return recordingCalls.get();
    }

    public List<RetryTaskContext> recordingContexts() {
        return List.copyOf(recordingContexts);
    }

    public void setFailRecording(boolean fail) {
        failRecording.set(fail);
    }

    public int failingCalls() {
        return failingCalls.get();
    }

    public List<RetryTaskContext> failingContexts() {
        return List.copyOf(failingContexts);
    }

    public void setFailFailing(boolean fail) {
        failFailing.set(fail);
    }

    public int blockingCalls() {
        return blockingCalls.get();
    }

    public void releaseBlocking() {
        blockingGate.countDown();
    }
}
