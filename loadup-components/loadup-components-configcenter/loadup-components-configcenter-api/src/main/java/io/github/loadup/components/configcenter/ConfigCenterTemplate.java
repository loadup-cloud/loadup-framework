package io.github.loadup.components.configcenter;

/*-
 * #%L
 * LoadUp ConfigCenter Components API
 * %%
 * Copyright (C) 2025 - 2026 loadup_cloud
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

import java.util.List;
import java.util.function.Consumer;

public interface ConfigCenterTemplate {
    String getConfig(String key);

    String getConfig(String key, String defaultValue);

    boolean setConfig(String key, String value);

    boolean removeConfig(String key);

    List<String> listKeys(String prefix);

    void addListener(String key, Consumer<String> listener);

    void removeListener(String key);
}
