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
 * The type of sql
 */
@Getter
@AllArgsConstructor
public enum SqlType implements IEnum {

    /**
     * insert the task
     */
    SQL_TASK_INSERT("SQL_TASK_INSERT", "insert the task"),

    /**
     * update the task
     */
    SQL_TASK_UPDATE("SQL_TASK_UPDATE", "update the task"),

    /**
     * load the task
     */
    SQL_TASK_LOAD("SQL_TASK_LOAD", "load the task"),

    /**
     * load the task for the unusuals
     */
    SQL_TASK_LOAD_UNUSUAL("SQL_TASK_LOAD_UNUSUAL", "load the task"),

    /**
     * lock the task by the taskId
     */
    SQL_TASK_LOCK_BY_ID("SQL_TASK_LOCK_BY_ID", "lock the task by the taskId"),

    /**
     * load the task by the taskId
     */
    SQL_TASK_LOAD_BY_ID("SQL_TASK_LOAD_BY_ID", "load the task by the taskId"),

    /**
     * delete the task
     */
    SQL_TASK_DELETE("SQL_TASK_DELETE", "delete the task"),

    /**
     * load the task by priority
     */
    SQL_TASK_LOAD_BY_PRIORITY("SQL_TASK_LOAD_BY_PRIORITY", "load the task by priority"),
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
