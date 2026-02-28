package io.github.loadup.modules.log.client.dto;

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

