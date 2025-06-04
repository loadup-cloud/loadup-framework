/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.commons.core;

/*-
 * #%L
 * loadup-commons-lang
 * %%
 * Copyright (C) 2022 - 2024 loadup_cloud
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class Snowflake {

    // Snowflake 算法相关配置
    private static final long START_TIMESTAMP = 1627635600000L; // 2021-07-30 00:00:00 UTC
    private static final long SEQUENCE_BIT = 12;
    private static final long MACHINE_BIT = 5;
    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BIT);
    private static final long MAX_MACHINE_NUM = ~(-1L << MACHINE_BIT);
    private static final long MACHINE_LEFT = SEQUENCE_BIT;
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
