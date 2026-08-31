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

package io.github.loadup.retrytask.facade.model;

import java.util.Map;

/**
 * Payload handed to a {@link io.github.loadup.retrytask.facade.RetryTaskProcessor} when a task is
 * executed.
 *
 * @param bizType the business type
 * @param bizId the business identifier
 * @param args the string payload registered with the task
 */
public record RetryTaskContext(String bizType, String bizId, Map<String, String> args) {}
