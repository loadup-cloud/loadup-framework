package io.github.loadup.components.rpc.proto;

import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Timestamps;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

/**
 * Scalar conversions shared by generated Protobuf IDL mappers.
 *
 * <p>The methods are static so MapStruct can reference this class from {@code uses} without
 * creating a bean. Absolute instants are preserved; the offset is rendered in the application's
 * system zone, matching the rest of the platform's time semantics.
 */
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
