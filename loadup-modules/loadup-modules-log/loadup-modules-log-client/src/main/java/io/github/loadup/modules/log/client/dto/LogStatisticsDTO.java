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

/**
 * Operation log statistics DTO.
 */
public class LogStatisticsDTO {

    /**
     * Total operations in the queried period.
     */
    private long total;

    /**
     * Number of successful operations.
     */
    private long successCount;

    /**
     * Number of failed operations.
     */
    private long failureCount;

    /**
     * Success rate (0-100).
     */
    private double successRate;

    /**
     * Average duration in milliseconds.
     */
    private Double avgDuration;

    /**
     * Max duration in milliseconds.
     */
    private Long maxDuration;

    /**
     * Operations count grouped by module.
     */
    private List<StatItem> byModule;

    /**
     * Operations count grouped by operation type.
     */
    private List<StatItem> byOperationType;

    /**
     * Operations count grouped by date (yyyy-MM-dd).
     */
    private List<StatItem> byDate;

    public static class StatItem {
        private String name;
        private long count;

        public StatItem(String name, long count) {
            this.name = name;
            this.count = count;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public long getCount() {
            return count;
        }

        public void setCount(long count) {
            this.count = count;
        }
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(long successCount) {
        this.successCount = successCount;
    }

    public long getFailureCount() {
        return failureCount;
    }

    public void setFailureCount(long failureCount) {
        this.failureCount = failureCount;
    }

    public double getSuccessRate() {
        return successRate;
    }

    public void setSuccessRate(double successRate) {
        this.successRate = successRate;
    }

    public Double getAvgDuration() {
        return avgDuration;
    }

    public void setAvgDuration(Double avgDuration) {
        this.avgDuration = avgDuration;
    }

    public Long getMaxDuration() {
        return maxDuration;
    }

    public void setMaxDuration(Long maxDuration) {
        this.maxDuration = maxDuration;
    }

    public List<StatItem> getByModule() {
        return byModule;
    }

    public void setByModule(List<StatItem> byModule) {
        this.byModule = byModule;
    }

    public List<StatItem> getByOperationType() {
        return byOperationType;
    }

    public void setByOperationType(List<StatItem> byOperationType) {
        this.byOperationType = byOperationType;
    }

    public List<StatItem> getByDate() {
        return byDate;
    }

    public void setByDate(List<StatItem> byDate) {
        this.byDate = byDate;
    }
}
