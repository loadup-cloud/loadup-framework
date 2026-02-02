package io.github.loadup.commons.result;

/*-
 * #%L
 * loadup-commons-util
 * %%
 * Copyright (C) 2022 - 2024 loadup_cloud
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
