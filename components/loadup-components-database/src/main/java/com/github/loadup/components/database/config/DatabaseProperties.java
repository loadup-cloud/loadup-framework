/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.database.config;

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

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for database component.
 */
@Data
@ConfigurationProperties(prefix = "loadup.database")
public class DatabaseProperties {

    /**
     * ID generation configuration
     */
    private IdGenerator idGenerator = new IdGenerator();

    /**
     * Sequence configuration
     */
    private Sequence sequence = new Sequence();

    @Data
    public static class IdGenerator {
        /**
         * Enable automatic ID generation for entities extending BaseDO
         */
        private boolean enabled = true;

        /**
         * Length of generated ID string (only for random strategy)
         */
        private int length = 20;

        /**
         * ID generation strategy: random, uuid-v4, uuid-v7, snowflake
         * <ul>
         *   <li>random: 随机字符串，长度可配置</li>
         *   <li>uuid-v4: 标准 UUID v4（随机）</li>
         *   <li>uuid-v7: UUID v7（基于时间戳，有序）</li>
         *   <li>snowflake: 雪花算法（分布式唯一ID，数字型）</li>
         * </ul>
         */
        private String strategy = "random";

        /**
         * Whether to keep hyphens in UUID (for uuid-v4 and uuid-v7)
         */
        private boolean uuidWithHyphens = false;

        /**
         * Snowflake worker ID (0-31, for snowflake strategy)
         */
        private long snowflakeWorkerId = 0L;

        /**
         * Snowflake datacenter ID (0-31, for snowflake strategy)
         */
        private long snowflakeDatacenterId = 0L;
    }

    @Data
    public static class Sequence {
        /**
         * Default step for sequence range allocation
         */
        private Long step = 1000L;

        /**
         * Minimum value for sequences
         */
        private Long minValue = 0L;

        /**
         * Maximum value for sequences
         */
        private Long maxValue = Long.MAX_VALUE;
    }
}

