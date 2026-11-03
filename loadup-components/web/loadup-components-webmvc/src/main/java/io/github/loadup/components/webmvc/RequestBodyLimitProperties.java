package io.github.loadup.components.webmvc;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.util.unit.DataSize;

/** Explicitly enabled byte limit for a mapped MVC path prefix. */
@ConfigurationProperties("loadup.webmvc.request-body")
public record RequestBodyLimitProperties(
        @DefaultValue("/api/internal/") String pathPrefix, DataSize maxSize) {
    public RequestBodyLimitProperties {
        if (pathPrefix == null
                || !pathPrefix.startsWith("/")
                || !pathPrefix.endsWith("/")
                || pathPrefix.contains("//")
                || pathPrefix.contains("..")
                || pathPrefix.contains("%")) {
            throw new IllegalArgumentException("Request body path prefix must be an absolute directory path");
        }
        if (maxSize == null || maxSize.toBytes() < 1 || maxSize.toBytes() >= Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Request body max size must be between 1 and 2147483646 bytes");
        }
    }
}
