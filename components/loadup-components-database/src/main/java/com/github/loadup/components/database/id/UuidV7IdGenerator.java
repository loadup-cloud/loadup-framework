package com.github.loadup.components.database.id;

/*-
 * #%L
 * loadup-components-database
 * %%
 * Copyright (C) 2022 - 2025 loadup_cloud
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

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Random;

/**
 * UUID v7 ID 生成器
 * <p>UUID v7 是基于时间戳的 UUID，提供了更好的排序性和数据库索引性能</p>
 * <p>格式：时间戳(48位) + 版本(4位) + 随机数(12位) + 变体(2位) + 随机数(62位)</p>
 * <p>优点：</p>
 * <ul>
 *   <li>时间有序，适合数据库索引</li>
 *   <li>保持 UUID 的唯一性</li>
 *   <li>比 UUID v4 有更好的数据库性能</li>
 * </ul>
 */
public class UuidV7IdGenerator implements IdGenerator {

    private final boolean withHyphens;
    private final Random  random;

    /**
     * 创建 UUID v7 生成器
     *
     * @param withHyphens 是否保留连字符
     */
    public UuidV7IdGenerator(boolean withHyphens) {
        this.withHyphens = withHyphens;
        this.random = new SecureRandom();
    }

    /**
     * 创建 UUID v7 生成器（默认不保留连字符）
     */
    public UuidV7IdGenerator() {
        this(false);
    }

    @Override
    public String generate() {
        // 获取当前时间戳（毫秒）
        long timestamp = Instant.now().toEpochMilli();

        // 生成随机字节
        byte[] randomBytes = new byte[10];
        random.nextBytes(randomBytes);

        // 构建 UUID v7
        // 时间戳的高 32 位
        long mostSigBits = (timestamp & 0xFFFF_FFFF_0000L) << 16;
        // 时间戳的低 16 位
        mostSigBits |= (timestamp & 0xFFFF) << 48;
        // 版本号 (0111b = 7)
        mostSigBits |= 0x7000L;
        // 随机数的高 12 位
        mostSigBits |= (randomBytes[0] & 0x0F) << 8;
        mostSigBits |= (randomBytes[1] & 0xFF);

        // 构建低 64 位
        // 变体 (10b)
        long leastSigBits = 0x8000_0000_0000_0000L;
        // 随机数的低 62 位
        for (int i = 2; i < 10; i++) {
            leastSigBits |= ((long) (randomBytes[i] & 0xFF)) << ((9 - i) * 8);
        }

        return formatUuid(mostSigBits, leastSigBits);
    }

    private String formatUuid(long mostSigBits, long leastSigBits) {
        String uuid = String.format("%08x-%04x-%04x-%04x-%012x",
            (mostSigBits >> 32) & 0xFFFFFFFF,
            (mostSigBits >> 16) & 0xFFFF,
            mostSigBits & 0xFFFF,
            (leastSigBits >> 48) & 0xFFFF,
            leastSigBits & 0xFFFF_FFFF_FFFFL);

        return withHyphens ? uuid : uuid.replace("-", "");
    }

    @Override
    public String getName() {
        return withHyphens ? "uuid-v7" : "uuid-v7-no-hyphens";
    }
}

