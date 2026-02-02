package io.github.loadup.components.gotone.job;

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

import io.github.loadup.components.gotone.converter.GotoneConverter;
import io.github.loadup.components.gotone.dataobject.NotificationRecordDO;
import io.github.loadup.components.gotone.domain.NotificationRecord;
import io.github.loadup.components.gotone.repository.NotificationRecordRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** 通知重试任务（使用 Spring Scheduling） 可集成 LoadUp Scheduler 组件实现更复杂的调度策略 */
@Slf4j
@Component
public class NotificationRetryJob {

  @Autowired private NotificationRecordRepository recordRepository;

  @Autowired private GotoneConverter gotoneConverter;

  /** 每30分钟扫描并重试失败的通知 */
  @Scheduled(cron = "${loadup.gotone.retry.cron:0 */30 * * * ?}")
  public void retryFailedNotifications() {
    log.info("Starting retry failed notifications job");

    try {
      // 查询24小时内失败且未达到最大重试次数的记录
      LocalDateTime afterTime = LocalDateTime.now().minusHours(24);
      List<NotificationRecordDO> recordDOs = recordRepository.findRetryableRecords(afterTime);

      log.info("Found {} records to retry", recordDOs.size());

      for (NotificationRecordDO recordDO : recordDOs) {
        try {
          // 转换为 Domain 对象
          NotificationRecord record = gotoneConverter.toNotificationRecord(recordDO);
          retryRecord(record);
        } catch (Exception e) {
          log.error("Failed to retry record {}: {}", recordDO.getId(), e.getMessage());
        }
      }

      log.info("Completed retry job, processed {} records", recordDOs.size());
    } catch (Exception e) {
      log.error("Retry job failed: {}", e.getMessage(), e);
    }
  }

  /** 重试单条记录 */
  private void retryRecord(NotificationRecord record) {
    log.info(
        "Retrying notification: bizId={}, retryCount={}",
        record.getBizId(),
        record.getRetryCount());

    // TODO: 重新构建请求并发送
    // 1. 根据记录信息重建 NotificationRequest
    // 2. 调用 GotoneNotificationService 重新发送
    // 3. 更新记录状态和重试次数

    record.setRetryCount(record.getRetryCount() + 1);
    record.setUpdatedAt(LocalDateTime.now());

    // 转换为 DO 并保存
    recordRepository.save(gotoneConverter.toNotificationRecordDO(record));
  }
}
