/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.database.id;

/*-
 * #%L
 * loadup-components-database
 * %%
 * Copyright (C) 2022 - 2025 loadup_cloud
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

/**
 * 雪花算法 ID 生成器
 * <p>基于 Twitter Snowflake 算法生成 64 位的分布式唯一 ID</p>
 * <p>结构：1位符号位 + 41位时间戳 + 10位机器ID + 12位序列号</p>
 * <p>优点：</p>
 * <ul>
 *   <li>趋势递增，适合数据库索引</li>
 *   <li>高性能，每毫秒可生成 4096 个 ID</li>
 *   <li>分布式环境下唯一</li>
 * </ul>
 */
public class SnowflakeIdGenerator implements IdGenerator {

    // 起始时间戳 (2020-01-01 00:00:00)
    private static final long EPOCH = 1577836800000L;

    // 机器ID所占的位数
    private static final long WORKER_ID_BITS     = 5L;
    private static final long DATACENTER_ID_BITS = 5L;

    // 序列号占用的位数
    private static final long SEQUENCE_BITS = 12L;

    // 最大机器ID
    private static final long MAX_WORKER_ID     = ~(-1L << WORKER_ID_BITS);
    private static final long MAX_DATACENTER_ID = ~(-1L << DATACENTER_ID_BITS);

    // 时间戳左移位数
    private static final long TIMESTAMP_LEFT_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATACENTER_ID_BITS;

    // 数据中心ID左移位数
    private static final long DATACENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;

    // 机器ID左移位数
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;

    // 序列号掩码
    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);

    private final long workerId;
    private final long datacenterId;
    private       long sequence      = 0L;
    private       long lastTimestamp = -1L;

    /**
     * 创建雪花算法生成器
     *
     * @param workerId     工作机器ID (0-31)
     * @param datacenterId 数据中心ID (0-31)
     */
    public SnowflakeIdGenerator(long workerId, long datacenterId) {
        if (workerId > MAX_WORKER_ID || workerId < 0) {
            throw new IllegalArgumentException(
                String.format("Worker ID must be between 0 and %d", MAX_WORKER_ID));
        }
        if (datacenterId > MAX_DATACENTER_ID || datacenterId < 0) {
            throw new IllegalArgumentException(
                String.format("Datacenter ID must be between 0 and %d", MAX_DATACENTER_ID));
        }
        this.workerId = workerId;
        this.datacenterId = datacenterId;
    }

    /**
     * 创建雪花算法生成器（使用默认的机器ID和数据中心ID）
     */
    public SnowflakeIdGenerator() {
        this(0L, 0L);
    }

    @Override
    public synchronized String generate() {
        long timestamp = System.currentTimeMillis();

        // 时钟回拨检测
        if (timestamp < lastTimestamp) {
            throw new IllegalStateException(
                String.format("Clock moved backwards. Refusing to generate id for %d milliseconds",
                    lastTimestamp - timestamp));
        }

        // 同一毫秒内
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            // 序列号溢出
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }

        lastTimestamp = timestamp;

        // 组合ID
        long id = ((timestamp - EPOCH) << TIMESTAMP_LEFT_SHIFT)
            | (datacenterId << DATACENTER_ID_SHIFT)
            | (workerId << WORKER_ID_SHIFT)
            | sequence;

        return String.valueOf(id);
    }

    private long tilNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }

    @Override
    public String getName() {
        return String.format("snowflake-%d-%d", workerId, datacenterId);
    }
}

