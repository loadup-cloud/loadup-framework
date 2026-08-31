package io.github.loadup.modules.log.domain.gateway;

/*-
 * #%L
 * Loadup Modules Log Domain
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
