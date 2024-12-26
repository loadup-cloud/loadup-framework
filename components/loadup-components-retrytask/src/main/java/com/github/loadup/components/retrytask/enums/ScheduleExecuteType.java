package com.github.loadup.components.retrytask.enums;

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

import com.github.loadup.commons.enums.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * The schedule executation strategy
 */
@Getter
@AllArgsConstructor
public enum ScheduleExecuteType implements IEnum {

    /**
     * It does not execute when register until the schedule call
     */
    DEFAULT("DEFAULT", "It does not execute when register until the schedule call"),

    /**
     * It execute immediatelly when the transaction is commited
     */
    SYNC("SYNC", "It execute immediatelly synchronized when the transaction is commited"),

    /**
     * It execute immediatelly asynchronized when the transaction is commited
     */
    ASYNC("ASYNC", "It execute immediatelly asynchronized when the transaction is commited"),
    ;

    /**
     * code
     */
    private String code;

    /**
     * description
     */
    private String description;
}
