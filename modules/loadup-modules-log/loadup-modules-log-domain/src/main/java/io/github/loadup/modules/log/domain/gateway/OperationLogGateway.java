package io.github.loadup.modules.log.domain.gateway;

/*-
 * #%L
 * Loadup Modules Log Domain
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

import io.github.loadup.modules.log.domain.model.OperationLog;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface OperationLogGateway {

    void save(OperationLog log);

    List<OperationLog> findByCondition(
            String userId,
            String module,
            String operationType,
            Boolean success,
            LocalDateTime startTime,
            LocalDateTime endTime,
            int pageNum,
            int pageSize);

    long countByCondition(
            String userId,
            String module,
            String operationType,
            Boolean success,
            LocalDateTime startTime,
            LocalDateTime endTime);

    /** Returns all matching logs (no pagination) for export. Max 10000 rows. */
    List<OperationLog> findAllForExport(
            String userId,
            String module,
            String operationType,
            Boolean success,
            LocalDateTime startTime,
            LocalDateTime endTime);

    /** Count grouped by a field name ("module" | "operation_type" | "date"). */
    Map<String, Long> countGroupBy(
            String groupField,
            String userId,
            String module,
            String operationType,
            Boolean success,
            LocalDateTime startTime,
            LocalDateTime endTime);

    /** Average and max duration stats. Returns map keys: avgDuration, maxDuration. */
    Map<String, Object> durationStats(
            String userId, String module, String operationType, LocalDateTime startTime, LocalDateTime endTime);
}
