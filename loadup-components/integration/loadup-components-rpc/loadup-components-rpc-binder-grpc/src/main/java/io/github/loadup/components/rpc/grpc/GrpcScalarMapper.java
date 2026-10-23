/*
 * Copyright 2026 LoadUp Cloud
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.loadup.components.rpc.grpc;

import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Timestamps;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

/** Scalar conversions for generated protobuf mappings. */
public final class GrpcScalarMapper {
    private GrpcScalarMapper() {}

    public static Timestamp timestamp(OffsetDateTime value) {
        if (value == null) return null;
        return Timestamps.checkValid(Timestamp.newBuilder().setSeconds(value.toEpochSecond()).setNanos(value.getNano()).build());
    }

    public static OffsetDateTime offset(Timestamp value) {
        if (value == null) return null;
        Timestamps.checkValid(value);
        return OffsetDateTime.ofInstant(Instant.ofEpochSecond(value.getSeconds(), value.getNanos()), ZoneId.systemDefault());
    }

    public static String decimal(BigDecimal value) { return value == null ? null : value.toPlainString(); }
    public static BigDecimal decimal(String value) { return value == null ? null : new BigDecimal(value); }
}
