package io.github.loadup.components.configcenter.model;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Wrapper that holds a single configuration entry retrieved from the config center.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfigEntry {

    /** Config item ID (key). */
    private String dataId;

    /** Config group. */
    private String group;

    /** Namespace / tenant. */
    private String namespace;

    /** Config content (plain text). */
    private String content;

    /** Content type, e.g. yaml / properties / json / text. */
    private String contentType;

    /** Version returned by the config center; null if not supported. */
    private String version;

    /** Last-modified timestamp returned by the config center; null if not supported. */
    private Instant lastModified;
}
