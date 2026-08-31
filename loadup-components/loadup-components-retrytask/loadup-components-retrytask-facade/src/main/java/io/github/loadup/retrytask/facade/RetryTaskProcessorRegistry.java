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

/**
 * Resolves a {@link RetryTaskProcessor} for a business type.
 */
public interface RetryTaskProcessorRegistry {

    /**
     * Returns the processor registered for the given business type.
     *
     * @param bizType the business type
     * @return the processor
     * @throws IllegalArgumentException when no processor is registered for the business type
     */
    RetryTaskProcessor getProcessor(String bizType);
}
