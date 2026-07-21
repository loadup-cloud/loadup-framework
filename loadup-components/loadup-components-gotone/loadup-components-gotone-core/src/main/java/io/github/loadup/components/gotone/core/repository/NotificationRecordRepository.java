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

import static io.github.loadup.components.gotone.core.dataobject.table.Tables.NOTIFICATION_RECORD_DO;

import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.query.QueryWrapper;
import io.github.loadup.components.gotone.core.dataobject.NotificationRecordDO;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

/**
 * 通知记录 Repository
 */
@Mapper
public interface NotificationRecordRepository extends BaseMapper<NotificationRecordDO> {

    /**
     * 根据追踪ID查询记录
     */
    default List<NotificationRecordDO> findByTraceId(String traceId) {
        QueryWrapper query = QueryWrapper.create()
                .where(NOTIFICATION_RECORD_DO.TRACE_ID.eq(traceId))
                .orderBy(NOTIFICATION_RECORD_DO.CREATED_AT.desc());
        return selectListByQuery(query);
    }

    /**
     * 根据请求ID查询记录（幂等性）
     */
    default NotificationRecordDO findByRequestId(String requestId) {
        QueryWrapper query = QueryWrapper.create()
                .where(NOTIFICATION_RECORD_DO.REQUEST_ID.eq(requestId))
                .limit(1);
        return selectOneByQuery(query);
    }

    /**
     * 查询需要重试的失败记录
     */
    default List<NotificationRecordDO> findRetryRecords(LocalDateTime currentTime, int limit) {
        QueryWrapper query = QueryWrapper.create()
                .where(NOTIFICATION_RECORD_DO.STATUS.in("FAILED", "RETRY"))
                .and(NOTIFICATION_RECORD_DO.RETRY_COUNT.lt(NOTIFICATION_RECORD_DO.MAX_RETRIES))
                .and(NOTIFICATION_RECORD_DO
                        .NEXT_RETRY_TIME
                        .isNull()
                        .or(NOTIFICATION_RECORD_DO.NEXT_RETRY_TIME.le(currentTime)))
                .orderBy(NOTIFICATION_RECORD_DO.NEXT_RETRY_TIME.asc())
                .limit(limit);
        return selectListByQuery(query);
    }

    /**
     * 根据服务代码和状态统计
     */
    default long countByServiceCodeAndStatus(String serviceCode, String status) {
        QueryWrapper query = QueryWrapper.create()
                .where(NOTIFICATION_RECORD_DO.SERVICE_CODE.eq(serviceCode))
                .and(NOTIFICATION_RECORD_DO.STATUS.eq(status));
        return selectCountByQuery(query);
    }

    /**
     * 检查请求ID是否存在（幂等性）
     */
    default boolean existsByRequestId(String requestId) {
        QueryWrapper query = QueryWrapper.create().where(NOTIFICATION_RECORD_DO.REQUEST_ID.eq(requestId));
        return selectCountByQuery(query) > 0;
    }

    /**
     * 查询可重试的失败记录
     */
    default List<NotificationRecordDO> findRetryableRecords(LocalDateTime afterTime) {
        QueryWrapper query = QueryWrapper.create()
                .where(NOTIFICATION_RECORD_DO.STATUS.eq("FAILED"))
                .and(NOTIFICATION_RECORD_DO.RETRY_COUNT.lt(3))
                .and(NOTIFICATION_RECORD_DO.CREATED_AT.ge(afterTime));
        return selectListByQuery(query);
    }
}
