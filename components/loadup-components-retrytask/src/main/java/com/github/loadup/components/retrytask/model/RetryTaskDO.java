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

import com.github.loadup.commons.dto.DTO;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * the domain model of retry task
 */
@Getter
@Setter
@Table("retry_task")
public class RetryTaskDO extends DTO {

    @Serial
    private static final long serialVersionUID = -1;

    /**
     * task serial id
     */
    @Id
    private String id;

    /**
     * business id, used as sharding index
     */
    private String businessId;

    /**
     * business type, user can define themselves
     */
    private String businessType;

    /**
     * the executed times
     */
    private Integer retryCount;

    /**
     * the time of next execution
     */
    private LocalDateTime nextRetryTime;

    /**
     * the maximum of execute times
     */
    private Integer maxRetries;

    /**
     * the flag of processing
     */
    private Boolean processing;

    /**
     * reach to max count or not
     */
    private Boolean reachedMaxRetries;

    /**
     * business context
     */
    private String payload;

    /**
     * create time
     */
    private LocalDateTime createdTime;

    /**
     * last modified time
     */
    private LocalDateTime updatedTime;

    /**
     * priority
     */
    private String priority;

    private String traceId;
    private String source;

    private String failureCallbackUrl;
}
