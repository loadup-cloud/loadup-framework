package io.github.loadup.components.gotone.repository;

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

import io.github.loadup.components.gotone.dataobject.NotificationRecordDO;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** 通知记录仓储 */
@Repository
public interface NotificationRecordRepository extends CrudRepository<NotificationRecordDO, String> {

  @Query("SELECT * FROM gotone_notification_record WHERE biz_id = :bizId")
  Optional<NotificationRecordDO> findByBizId(@Param("bizId") String bizId);

  @Query("SELECT * FROM gotone_notification_record WHERE trace_id = :traceId")
  List<NotificationRecordDO> findByTraceId(@Param("traceId") String traceId);

  @Query(
      "SELECT * FROM gotone_notification_record WHERE business_code = :businessCode AND status = :status")
  List<NotificationRecordDO> findByBusinessCodeAndStatus(
      @Param("businessCode") String businessCode, @Param("status") String status);

  @Query(
      "SELECT * FROM gotone_notification_record WHERE status = 'FAILED' AND retry_count < 3 AND created_at >= :afterTime")
  List<NotificationRecordDO> findRetryableRecords(@Param("afterTime") LocalDateTime afterTime);
}
