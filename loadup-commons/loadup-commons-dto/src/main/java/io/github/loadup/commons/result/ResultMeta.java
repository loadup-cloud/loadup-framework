package io.github.loadup.commons.result;

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
