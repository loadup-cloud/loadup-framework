/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.testify.data;

/*-
 * #%L
 * loadup-components-test
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
 * 原子项匹配器。
 * <p>
 * 根据某种规则进行匹配度计算。
 *
 *
 *
 */
public interface MetaItemMatcher {

    /**
     * 完全匹配
     */
    int FULL_MATCH = Integer.MAX_VALUE;

    /**
     * 高度匹配
     */
    int HIGH_MATCH = 100;

    /**
     * 中度匹配
     */
    int MEDIUM_MATCH = 50;

    /**
     * 低度匹配
     */
    int LOW_MATCH = 10;

    /**
     * 与某个关键词的匹配度。
     *
     * @param initItem
     * @param keyword
     * @return
     */
    int match(MetaInitItem initItem, String keyword);
}
