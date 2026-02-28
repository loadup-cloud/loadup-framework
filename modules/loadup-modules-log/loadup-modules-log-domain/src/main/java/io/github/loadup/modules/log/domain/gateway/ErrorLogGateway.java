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

import io.github.loadup.modules.log.domain.model.ErrorLog;
import java.time.LocalDateTime;
import java.util.List;

public interface ErrorLogGateway {

    void save(ErrorLog log);

    List<ErrorLog> findByCondition(
            String userId,
            String errorType,
            String errorCode,
            LocalDateTime startTime,
            LocalDateTime endTime,
            int pageNum,
            int pageSize);

    long countByCondition(
            String userId, String errorType, String errorCode, LocalDateTime startTime, LocalDateTime endTime);
}
