package io.github.loadup.components.gotone.core.repository;

/*-
 * #%L
 * loadup-components-gotone-core
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

import com.mybatisflex.core.BaseMapper;
import io.github.loadup.components.gotone.core.dataobject.NotificationRecordDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 通知记录 Repository
 */
@Mapper
public interface NotificationRecordRepository extends BaseMapper<NotificationRecordDO> {

    /**
     * 根据追踪ID查询记录
     */
    @Select("""
        SELECT * FROM gotone_notification_record
        WHERE trace_id = #{traceId}
        ORDER BY created_at DESC
        """)
    List<NotificationRecordDO> findByTraceId(@Param("traceId") String traceId);

    /**
     * 根据请求ID查询记录（幂等性）
     */
    @Select("""
        SELECT * FROM gotone_notification_record
        WHERE request_id = #{requestId}
        LIMIT 1
        """)
    NotificationRecordDO findByRequestId(@Param("requestId") String requestId);

    /**
     * 查询需要重试的失败记录
     */
    @Select("""
        SELECT * FROM gotone_notification_record
        WHERE status IN ('FAILED', 'RETRY')
        AND retry_count < max_retries
        AND (next_retry_time IS NULL OR next_retry_time <= #{currentTime})
        ORDER BY next_retry_time ASC
        LIMIT #{limit}
        """)
    List<NotificationRecordDO> findRetryRecords(
            @Param("currentTime") LocalDateTime currentTime,
            @Param("limit") int limit);

    /**
     * 根据服务代码和状态统计
     */
    @Select("""
        SELECT COUNT(*) FROM gotone_notification_record
        WHERE service_code = #{serviceCode}
        AND status = #{status}
        """)
    long countByServiceCodeAndStatus(
            @Param("serviceCode") String serviceCode,
            @Param("status") String status);

    /**
     * 检查请求ID是否存在（幂等性）
     */
    @Select("""
        SELECT COUNT(*) > 0 FROM gotone_notification_record
        WHERE request_id = #{requestId}
        """)
    boolean existsByRequestId(@Param("requestId") String requestId);

    @Select("SELECT * FROM gotone_notification_record WHERE status = 'FAILED' AND retry_count < 3 AND created_at >= #{afterTime}")
    List<NotificationRecordDO> findRetryableRecords(@Param("afterTime") LocalDateTime afterTime);
}
