package io.github.loadup.modules.upms.domain.entity;

/*-
 * #%L
 * Loadup Modules UPMS Domain Layer
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

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Operation Log Entity - User operation audit log
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperationLog {

    private String id;

    private String traceId;

    private String userId;

    private String username;

    private String operationType;

    private String operationModule;

    private String operationDesc;
    private LocalDateTime operationTime;

    private String requestMethod;

    private String requestUrl;

    private String requestParams;

    private String responseResult;

    private String ipAddress;

    private String userAgent;

    private Long executionTime;

    /** Status: 1-Success, 0-Failure */
    private Short status;

    private String errorMessage;

    private LocalDateTime createdTime;
}
