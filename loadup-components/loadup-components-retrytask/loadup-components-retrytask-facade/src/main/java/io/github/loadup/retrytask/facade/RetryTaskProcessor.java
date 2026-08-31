/*-
 * #%L
 * Loadup Components Retrytask Facade
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package io.github.loadup.retrytask.facade;

import io.github.loadup.retrytask.facade.model.RetryTaskContext;

/**
 * SPI implemented by business code to process retry tasks of one business type.
 *
 * <p>Any exception thrown from {@link #process(RetryTaskContext)} marks the attempt as failed and
 * triggers the underlying retry engine. A successful return completes the task.
 */
public interface RetryTaskProcessor {

    /**
     * Returns the business type handled by this processor.
     *
     * @return the business type
     */
    String bizType();

    /**
     * Processes one retry task.
     *
     * @param context the task payload
     * @throws Exception when the attempt fails and should be retried
     */
    void process(RetryTaskContext context) throws Exception;
}
