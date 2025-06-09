/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.testify.data.matcher;

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

import com.github.loadup.components.testify.data.MetaInitItem;
import com.github.loadup.components.testify.data.MetaItemMatcher;
import org.apache.commons.lang3.StringUtils;

/**
 * 根据字段名称匹配。
 *
 *
 *
 */
public class MetaItemNameMatcher implements MetaItemMatcher {

    /**
     * @see MetaItemMatcher#match(MetaInitItem, String)
     */
    @Override
    public int match(MetaInitItem initItem, String keyword) {
        String field = initItem.getField();

        // 名称完全匹配
        if (keyword.equalsIgnoreCase(field)) {
            return HIGH_MATCH;
        }

        // 蛇形换成驼峰后完全匹配
        field = StringUtils.replace(field, "_", "");
        if (keyword.equalsIgnoreCase(field)) {
            return HIGH_MATCH;
        }

        return 0;
    }
}
