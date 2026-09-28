/*-
 * #%L
 * Loadup Modules UPMS Domain Layer
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
package io.github.loadup.modules.upms.domain.valueobject;

import java.util.Arrays;
import java.util.Optional;

public enum DataScope {
    ALL((short) 1),
    CUSTOM_DEPARTMENTS((short) 2),
    DEPARTMENT((short) 3),
    DEPARTMENT_TREE((short) 4),
    OWNER((short) 5);

    private final short code;

    DataScope(short code) {
        this.code = code;
    }

    public short code() {
        return code;
    }

    public static Optional<DataScope> fromCode(Short code) {
        return Arrays.stream(values())
                .filter(scope -> code != null && scope.code == code)
                .findFirst();
    }
}
