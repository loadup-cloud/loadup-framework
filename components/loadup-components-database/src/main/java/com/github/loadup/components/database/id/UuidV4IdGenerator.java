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

import java.util.UUID;

/**
 * UUID v4 ID 生成器
 * <p>使用标准的 UUID v4（随机 UUID）生成 ID</p>
 * <p>生成的 ID 格式：xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx</p>
 */
public class UuidV4IdGenerator implements IdGenerator {

    private final boolean withHyphens;

    /**
     * 创建 UUID v4 生成器
     *
     * @param withHyphens 是否保留连字符
     */
    public UuidV4IdGenerator(boolean withHyphens) {
        this.withHyphens = withHyphens;
    }

    /**
     * 创建 UUID v4 生成器（默认不保留连字符）
     */
    public UuidV4IdGenerator() {
        this(false);
    }

    @Override
    public String generate() {
        String uuid = UUID.randomUUID().toString();
        return withHyphens ? uuid : uuid.replace("-", "");
    }

    @Override
    public String getName() {
        return withHyphens ? "uuid-v4" : "uuid-v4-no-hyphens";
    }
}

