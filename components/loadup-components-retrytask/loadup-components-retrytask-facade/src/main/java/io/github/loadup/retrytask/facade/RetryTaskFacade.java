package io.github.loadup.retrytask.facade;

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

import io.github.loadup.retrytask.facade.request.RetryTaskRegisterRequest;

/**
 * The facade for the retry task module.
 */
public interface RetryTaskFacade {

    /**
     * Registers a new retry task.
     *
     * @param request The request to register a new retry task.
     * @return The unique identifier of the registered task.
     */
    Long register(RetryTaskRegisterRequest request);

    /**
     * Deletes a retry task.
     *
     * @param bizType The business type of the task.
     * @param bizId The business identifier of the task.
     */
    void delete(String bizType, String bizId);

    /**
     * Resets a retry task.
     *
     * @param bizType The business type of the task.
     * @param bizId The business identifier of the task.
     */
    void reset(String bizType, String bizId);
}
