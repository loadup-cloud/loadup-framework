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
    }

    public LogStatisticsDTO(long total, long successCount, long failureCount, double successRate, Double avgDuration, Long maxDuration, List<StatItem> byModule, List<StatItem> byOperationType, List<StatItem> byDate, String name, long count) {
        this.total = total;
        this.successCount = successCount;
        this.failureCount = failureCount;
        this.successRate = successRate;
        this.avgDuration = avgDuration;
        this.maxDuration = maxDuration;
        this.byModule = byModule;
        this.byOperationType = byOperationType;
        this.byDate = byDate;
        this.name = name;
        this.count = count;
    }

    public LogStatisticsDTO() {
    }

    public long getTotal() {
        return this.total;
    }

    public long getSuccessCount() {
        return this.successCount;
    }

    public long getFailureCount() {
        return this.failureCount;
    }

    public double getSuccessRate() {
        return this.successRate;
    }

    public Double getAvgDuration() {
        return this.avgDuration;
    }

    public Long getMaxDuration() {
        return this.maxDuration;
    }

    public List<StatItem> getByModule() {
        return this.byModule;
    }

    public List<StatItem> getByOperationType() {
        return this.byOperationType;
    }

    public List<StatItem> getByDate() {
        return this.byDate;
    }

    public String getName() {
        return this.name;
    }

    public long getCount() {
        return this.count;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public void setSuccessCount(long successCount) {
        this.successCount = successCount;
    }

    public void setFailureCount(long failureCount) {
        this.failureCount = failureCount;
    }

    public void setSuccessRate(double successRate) {
        this.successRate = successRate;
    }

    public void setAvgDuration(Double avgDuration) {
        this.avgDuration = avgDuration;
    }

    public void setMaxDuration(Long maxDuration) {
        this.maxDuration = maxDuration;
    }

    public void setByModule(List<StatItem> byModule) {
        this.byModule = byModule;
    }

    public void setByOperationType(List<StatItem> byOperationType) {
        this.byOperationType = byOperationType;
    }

    public void setByDate(List<StatItem> byDate) {
        this.byDate = byDate;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCount(long count) {
        this.count = count;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(total, successCount, failureCount, successRate, avgDuration, maxDuration, byModule, byOperationType, byDate, name, count);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LogStatisticsDTO other = (LogStatisticsDTO) o;
        if (!java.util.Objects.equals(total, other.total)) return false;
        if (!java.util.Objects.equals(successCount, other.successCount)) return false;
        if (!java.util.Objects.equals(failureCount, other.failureCount)) return false;
        if (!java.util.Objects.equals(successRate, other.successRate)) return false;
        if (!java.util.Objects.equals(avgDuration, other.avgDuration)) return false;
        if (!java.util.Objects.equals(maxDuration, other.maxDuration)) return false;
        if (!java.util.Objects.equals(byModule, other.byModule)) return false;
        if (!java.util.Objects.equals(byOperationType, other.byOperationType)) return false;
        if (!java.util.Objects.equals(byDate, other.byDate)) return false;
        if (!java.util.Objects.equals(name, other.name)) return false;
        if (!java.util.Objects.equals(count, other.count)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "LogStatisticsDTO(" + "total=" + total + ", " + "successCount=" + successCount + ", " + "failureCount=" + failureCount + ", " + "successRate=" + successRate + ", " + "avgDuration=" + avgDuration + ", " + "maxDuration=" + maxDuration + ", " + "byModule=" + byModule + ", " + "byOperationType=" + byOperationType + ", " + "byDate=" + byDate + ", " + "name=" + name + ", " + "count=" + count + ")";
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private long total;
        private long successCount;
        private long failureCount;
        private double successRate;
        private Double avgDuration;
        private Long maxDuration;
        private List<StatItem> byModule;
        private List<StatItem> byOperationType;
        private List<StatItem> byDate;
        private String name;
        private long count;

        public Builder total(long total) {
            this.total = total;
            return this;
        }

        public Builder successCount(long successCount) {
            this.successCount = successCount;
            return this;
        }

        public Builder failureCount(long failureCount) {
            this.failureCount = failureCount;
            return this;
        }

        public Builder successRate(double successRate) {
            this.successRate = successRate;
            return this;
        }

        public Builder avgDuration(Double avgDuration) {
            this.avgDuration = avgDuration;
            return this;
        }

        public Builder maxDuration(Long maxDuration) {
            this.maxDuration = maxDuration;
            return this;
        }

        public Builder byModule(List<StatItem> byModule) {
            this.byModule = byModule;
            return this;
        }

        public Builder byOperationType(List<StatItem> byOperationType) {
            this.byOperationType = byOperationType;
            return this;
        }

        public Builder byDate(List<StatItem> byDate) {
            this.byDate = byDate;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder count(long count) {
            this.count = count;
            return this;
        }

        public LogStatisticsDTO build() {
            return new LogStatisticsDTO(this.total, this.successCount, this.failureCount, this.successRate, this.avgDuration, this.maxDuration, this.byModule, this.byOperationType, this.byDate, this.name, this.count);
        }
    }
}
