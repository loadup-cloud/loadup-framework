/*-
 * #%L
 * Loadup Components Retrytask Facade
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
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
