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

import io.github.loadup.components.gotone.model.NotificationRequest;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** 通知队列 支持 RabbitMQ 或内存队列 */
@Slf4j
@Component
public class NotificationQueue {

  private static final String NOTIFICATION_QUEUE = "gotone.notification.queue";
  private static final String NOTIFICATION_EXCHANGE = "gotone.notification.exchange";

  @Autowired(required = false)
  private RabbitTemplate rabbitTemplate;

  // 内存队列作为备选
  private final BlockingQueue<NotificationRequest> memoryQueue =
      new PriorityBlockingQueue<>(
          1000, (r1, r2) -> Integer.compare(r2.getPriority(), r1.getPriority()));

  /** 入队 */
  public void enqueue(NotificationRequest request) {
    if (rabbitTemplate != null) {
      try {
        rabbitTemplate.convertAndSend(NOTIFICATION_EXCHANGE, "", request);
        log.debug("Enqueued notification to RabbitMQ: {}", request.getBizId());
        return;
      } catch (Exception e) {
        log.error("Failed to enqueue to RabbitMQ, fallback to memory queue: {}", e.getMessage());
      }
    }

    // 降级到内存队列
    boolean success = memoryQueue.offer(request);
    if (!success) {
      log.error("Memory queue is full, notification dropped: {}", request.getBizId());
      throw new RuntimeException("Notification queue is full");
    }
    log.debug("Enqueued notification to memory queue: {}", request.getBizId());
  }

  /** 出队 */
  public NotificationRequest dequeue() throws InterruptedException {
    return memoryQueue.take();
  }

  /** 获取队列大小 */
  public int size() {
    return memoryQueue.size();
  }
}
