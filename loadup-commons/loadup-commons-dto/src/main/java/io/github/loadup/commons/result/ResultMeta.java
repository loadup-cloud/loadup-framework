package io.github.loadup.commons.result;

/*-
 * #%L
 * loadup-commons-util
 * %%
 * Copyright (C) 2022 - 2024 loadup_cloud
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

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;

public record ResultMeta(String requestId, String timestamp) {

    // 主构造函数
    public ResultMeta {}

    // 静态工厂方法
    public static ResultMeta of(String requestId) {
        return new ResultMeta(requestId, LocalDateTime.now().toString());
    }

    public static ResultMeta of(String requestId, LocalDateTime timestamp) {
        return new ResultMeta(
                requestId,
                Objects.requireNonNullElseGet(timestamp, LocalDateTime::now).toString());
    }

    public static ResultMeta of(String requestId, Date timestamp) {
        if (timestamp == null) {
            return new ResultMeta(requestId, LocalDateTime.now().toString());
        }
        LocalDateTime localDateTime =
                timestamp.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

        return new ResultMeta(requestId, localDateTime.toString());
    }
}
