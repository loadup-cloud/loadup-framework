package com.github.loadup.components.retrytask.model;

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

import com.github.loadup.commons.base.DTO;
import com.github.loadup.commons.util.ToStringUtils;
import com.github.loadup.components.retrytask.enums.ScheduleExecuteType;
import lombok.Getter;
import lombok.Setter;

/**
 * requet of retry task
 */
@Getter
@Setter
public class RetryTaskRequest extends DTO {

    /**
     * business id，used as sharding index
     */
    private String bizId;

    /**
     * business type, user can define themselves
     */
    private String bizType;

    /**
     * the execute strategy of retry task
     */
    private ScheduleExecuteType scheduleExecuteType = ScheduleExecuteType.ASYNC;

    /**
     * the interval between The first time to execute and now, the unit is seconds
     */
    private int startExecuteInterval = 0;

    /**
     * business context, which is stored with json format
     */
    private String bizContext;

    /**
     * priority
     */
    private String priority;

    @Override
    public String toString() {
        return ToStringUtils.reflectionToString(this);
    }
}
