package com.github.loadup.commons.core;

/*-
 * #%L
 * loadup-commons-lang
 * %%
 * Copyright (C) 2022 - 2024 loadup_cloud
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

import java.time.*;

public class Snowflake {

    // Snowflake 算法相关配置
    private static final long START_TIMESTAMP = 1627635600000L; // 2021-07-30 00:00:00 UTC
    private static final long SEQUENCE_BIT   = 12;
    private static final long MACHINE_BIT    = 5;
    private static final long MAX_SEQUENCE   = ~(-1L << SEQUENCE_BIT);
    private static final long MAX_MACHINE_NUM = ~(-1L << MACHINE_BIT);
    private static final long MACHINE_LEFT   = SEQUENCE_BIT;
    private static final long TIMESTAMP_LEFT = SEQUENCE_BIT + MACHINE_BIT;

    private long machineId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;

    public Snowflake(long machineId) {
        if (machineId > MAX_MACHINE_NUM || machineId < 0) {
            throw new IllegalArgumentException(
                "Machine ID can't be greater than " + MAX_MACHINE_NUM + " or less than 0");
        }
        this.machineId = machineId;
    }

    public static void main(String[] args) {
        Snowflake idGenerator = new Snowflake(1);

        // 示例生成ID
        String generatedId = idGenerator.generateId(123, 456, "ABCD", 7);
        System.out.println("Generated ID: " + generatedId);
    }

    public synchronized String generateId(int tenantNumber, int systemNumber, String businessCode, int tableNumber) {
        long currentTimestamp = System.currentTimeMillis();

        if (currentTimestamp < lastTimestamp) {
            throw new RuntimeException("Clock moved backwards. Refusing to generate id");
        }

        if (currentTimestamp == lastTimestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            if (sequence == 0) {
                currentTimestamp = getNextTimestamp();
            }
        } else {
            sequence = 0L;
        }

        lastTimestamp = currentTimestamp;

        long id = (currentTimestamp - START_TIMESTAMP) << TIMESTAMP_LEFT | machineId << MACHINE_LEFT | sequence;

        // 格式化输出
        return formatDate(currentTimestamp)
            + formatNumber(tenantNumber, 3)
            + formatNumber(systemNumber, 3)
            + businessCode
            + formatNumber(tableNumber, 3)
            + formatNumber(id, 3);
    }

    private long getNextTimestamp() {
        long currentTimestamp = System.currentTimeMillis();
        while (currentTimestamp <= lastTimestamp) {
            currentTimestamp = System.currentTimeMillis();
        }
        return currentTimestamp;
    }

    private String formatDate(long timestamp) {
        Instant instant = Instant.ofEpochMilli(timestamp);
        LocalDate localDate =
            LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).toLocalDate();
        return String.format("%04d%02d%02d", localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth());
    }

    private String formatNumber(long number, int length) {
        String format = "%0" + length + "d";
        return String.format(format, number);
    }
}
