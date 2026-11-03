/*
 * #%L
 * LoadUp Components RPC Proto
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
package io.github.loadup.components.rpc.proto;

import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Timestamps;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

public final class RpcScalarMapper {
    private RpcScalarMapper() {}

    public static Timestamp timestamp(OffsetDateTime value) {
        if (value == null) {
            return null;
        }
        return Timestamps.checkValid(Timestamp.newBuilder()
                .setSeconds(value.toEpochSecond())
                .setNanos(value.getNano())
                .build());
    }

    public static OffsetDateTime offset(Timestamp value) {
        if (value == null) {
            return null;
        }
        Timestamps.checkValid(value);
        return OffsetDateTime.ofInstant(
                Instant.ofEpochSecond(value.getSeconds(), value.getNanos()), ZoneId.systemDefault());
    }

    public static Timestamp timestamp(Instant value) {
        if (value == null) {
            return null;
        }
        return Timestamps.checkValid(Timestamp.newBuilder()
                .setSeconds(value.getEpochSecond())
                .setNanos(value.getNano())
                .build());
    }

    public static Instant instant(Timestamp value) {
        if (value == null) {
            return null;
        }
        Timestamps.checkValid(value);
        return Instant.ofEpochSecond(value.getSeconds(), value.getNanos());
    }

    public static String decimal(BigDecimal value) {
        return value == null ? null : value.toPlainString();
    }

    public static BigDecimal decimal(String value) {
        return value == null ? null : new BigDecimal(value);
    }
}
