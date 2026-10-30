/*
 * #%L
 * Loadup Components Database
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
package io.github.loadup.components.database.id;

import io.github.loadup.commons.error.CommonException;
import io.github.loadup.commons.json.ToStringAsJson;
import io.github.loadup.components.database.sequence.DatabaseResultCodeEnum;

/**
 * Business metadata; shard is supplied by the event's registered routing policy.
 */
public record BusinessIdCommand(
        int dataVersion, int systemVersion, String eventCode, String extensionCode, int siteId, int shard) {
    public BusinessIdCommand {
        extensionCode = extensionCode == null ? "" : extensionCode;
        if (dataVersion < 0
                || dataVersion > 9
                || systemVersion < 0
                || systemVersion > 9
                || eventCode == null
                || !eventCode.matches("[0-9]{8}")
                || !extensionCode.matches("[A-Za-z0-9]{0,32}")
                || siteId < 0
                || siteId > 999
                || shard < 0
                || shard > 999) {
            throw new CommonException(DatabaseResultCodeEnum.INVALID_INPUT);
        }
    }

    @Override
    public String toString() {
        return ToStringAsJson.reflectionToString(this);
    }
}
