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
