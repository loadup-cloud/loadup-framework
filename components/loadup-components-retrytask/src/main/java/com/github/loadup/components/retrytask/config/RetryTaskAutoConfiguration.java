package com.github.loadup.components.retrytask.config;

/*-
 * #%L
 * loadup-components-retrytask
 * %%
 * Copyright (C) 2022 - 2023 loadup_cloud
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@AutoConfiguration
@EnableAsync
@EnableScheduling
public class RetryTaskAutoConfiguration {
    @Value("${retrytask.loader.thread.core_pool_size:5}")
    private int loaderCorePoolSize;
    @Value("${retrytask.loader.thread.max_pool_size:10}")
    private int loaderMaxPoolSize;
    @Value("${retrytask.loader.thread.keep_alive_seconds:60}")
    private int loaderKeepAliveSeconds;
    @Value("${retrytask.loader.thread.queue_capacity:10}")
    private int loaderQueueCapacity;

    @Value("${retrytask.executor.thread.core_pool_size:12}")
    private int executorCorePoolSize;
    @Value("${retrytask.executor.thread.max_pool_size:20}")
    private int executorMaxPoolSize;
    @Value("${retrytask.executor.thread.keep_alive_seconds:60}")
    private int executorKeepAliveSeconds;
    @Value("${retrytask.executor.thread.queue_capacity:100}")
    private int executorQueueCapacity;

    @Bean("retryTaskLoaderThreadPool")
    public ThreadPoolTaskExecutor retryTaskLoaderThreadPool() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(loaderCorePoolSize);
        threadPoolTaskExecutor.setMaxPoolSize(loaderMaxPoolSize);
        threadPoolTaskExecutor.setKeepAliveSeconds(loaderKeepAliveSeconds);
        threadPoolTaskExecutor.setQueueCapacity(loaderQueueCapacity);
        return threadPoolTaskExecutor;
    }

    @Bean("retryTaskExecutorThreadPool")
    public ThreadPoolTaskExecutor retryTaskExecutorThreadPool() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(executorCorePoolSize);
        threadPoolTaskExecutor.setMaxPoolSize(executorMaxPoolSize);
        threadPoolTaskExecutor.setKeepAliveSeconds(executorKeepAliveSeconds);
        threadPoolTaskExecutor.setQueueCapacity(executorQueueCapacity);
        return threadPoolTaskExecutor;
    }
}
