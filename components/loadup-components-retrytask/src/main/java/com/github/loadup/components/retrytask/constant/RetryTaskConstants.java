package com.github.loadup.components.retrytask.constant;

/*-
 * #%L
 * loadup-components-retrytask
 * %%
 * Copyright (C) 2022 - 2023 loadup_cloud
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
 * Constants of retry task
 */
public class RetryTaskConstants {

    /**
     * the column name of sharding
     */
    public static final String SHARDING_COLUMN_NAME = "virtual_sharding_id";

    /**
     * the postfix of retry task
     */
    public static final String SUFFIX_TABLE_NAME = "retry_task";

    /**
     * the length of sharding index
     */
    public static final int SHARDING_INDEX_LENTH = 2;

    /**
     * the default business type
     */
    public static final String DEFAULT_BIZ_TYPE = "DEFAULT";

    /**
     * the default postfix of id
     */
    public static final String ID_DEFAULT_SUFFIX = "0000000000";

    /**
     * interval character
     */
    public static final String INTERVAL_CHAR = "-";

    /**
     * COMMA
     */
    public static final char COMMA = ',';
}
