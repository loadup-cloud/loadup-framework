package io.github.loadup.components.gotone.service;

/*-
 * #%L
 * loadup-components-gotone-api
 * %%
 * Copyright (C) 2026 LoadUp Cloud
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

import io.github.loadup.components.gotone.api.GotoneNotificationService;
import io.github.loadup.components.gotone.model.NotificationRequest;
import jakarta.annotation.PostConstruct;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 通知队列消费者
 * 支持 RabbitMQ 和内存队列消费
 *
 * Note: V2.0 API 已简化，此组件主要用于兼容性。
 * 新代码建议直接使用 GotoneNotificationService.sendAsync()
 */
@Slf4j
@Component
public class NotificationQueueConsumer {

    @Autowired
    private NotificationQueue notificationQueue;

    @Autowired(required = false)
    private GotoneNotificationService gotoneNotificationService;

    private          ExecutorService executorService;
    private volatile boolean         running = true;

    @PostConstruct
    public void init() {
        if (gotoneNotificationService == null) {
            log.warn("GotoneNotificationService not available, NotificationQueueConsumer will not start");
            return;
        }

        // 创建固定大小的线程池用于并发处理
        int threadCount = Runtime.getRuntime().availableProcessors() * 2;
        executorService = Executors.newFixedThreadPool(threadCount);

        // 启动内存队列消费线程
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(this::consumeMemoryQueue);
        }

        log.info("NotificationQueueConsumer initialized with {} threads", threadCount);
    }

    /**
     * RabbitMQ 监听器
     */
    @RabbitListener(queues = "gotone.notification.queue", containerFactory = "rabbitListenerContainerFactory")
    public void onMessage(NotificationRequest request) {
        processNotification(request);
    }

    /**
     * 消费内存队列
     */
    private void consumeMemoryQueue() {
        while (running) {
            try {
                NotificationRequest request = notificationQueue.dequeue();
                processNotification(request);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Memory queue consumer interrupted");
                break;
            } catch (Exception e) {
                log.error("Error consuming memory queue: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * 处理通知
     *
     * Note: V2.0 API 变化 - 需要从 NotificationRequest 提取业务代码和参数
     * 此实现为临时兼容方案，建议迁移到新 API
     */
    private void processNotification(NotificationRequest request) {
        try {
            log.debug("Processing notification: {}", request.getBizId());

            if (gotoneNotificationService == null) {
                log.error("GotoneNotificationService not available");
                return;
            }

            // V2.0 API: 需要业务代码、地址列表和模板参数
            // 这里使用 templateCode 作为业务代码的临时方案
            String businessCode = request.getTemplateCode();
            if (businessCode == null || businessCode.isEmpty()) {
                log.warn("No business code found in request, skipping");
                return;
            }

            // 同步发送（因为已经在异步队列中了）
            gotoneNotificationService.send(
                businessCode,
                request.getBizId(),
                request.getReceivers(),
                request.getTemplateParams() != null ? request.getTemplateParams() : new java.util.HashMap<>()
            );

        } catch (Exception e) {
            log.error("Failed to process notification {}: {}",
                request.getBizId(), e.getMessage(), e);
            // TODO: 可以实现重试机制或死信队列
        }
    }

    /**
     * 监控队列大小
     */
    @Scheduled(fixedDelay = 60000)
    public void monitorQueueSize() {
        int size = notificationQueue.size();
        if (size > 0) {
            log.info("Memory notification queue size: {}", size);
        }
        if (size > 500) {
            log.warn("Memory notification queue size is high: {}", size);
        }
    }

    /**
     * 关闭消费者
     */
    public void shutdown() {
        running = false;
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}

