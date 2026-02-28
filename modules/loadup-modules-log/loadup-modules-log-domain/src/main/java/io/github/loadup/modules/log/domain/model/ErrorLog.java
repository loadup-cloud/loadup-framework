package io.github.loadup.modules.log.domain.model;

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

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Domain model for error log. Pure POJO, no framework annotations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorLog {

    private String id;
    private String userId;
    /** BUSINESS / SYSTEM / THIRD_PARTY */
    private String errorType;

    private String errorCode;
    private String errorMessage;
    private String stackTrace;
    private String requestUrl;
    private String requestMethod;
    private String requestParams;
    private String ip;
    private LocalDateTime errorTime;
    private LocalDateTime createdAt;
}
