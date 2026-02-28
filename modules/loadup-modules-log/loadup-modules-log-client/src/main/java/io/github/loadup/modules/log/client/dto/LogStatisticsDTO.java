package io.github.loadup.modules.log.client.dto;

/*-
 * #%L
 * Loadup Modules Log Client
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

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Operation log statistics DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogStatisticsDTO {

    /** Total operations in the queried period. */
    private long total;

    /** Number of successful operations. */
    private long successCount;

    /** Number of failed operations. */
    private long failureCount;

    /** Success rate (0-100). */
    private double successRate;

    /** Average duration in milliseconds. */
    private Double avgDuration;

    /** Max duration in milliseconds. */
    private Long maxDuration;

    /** Operations count grouped by module. */
    private List<StatItem> byModule;

    /** Operations count grouped by operation type. */
    private List<StatItem> byOperationType;

    /** Operations count grouped by date (yyyy-MM-dd). */
    private List<StatItem> byDate;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StatItem {
        private String name;
        private long count;
    }
}
