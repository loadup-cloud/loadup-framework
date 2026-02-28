package io.github.loadup.modules.config.domain.model;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Domain model for a data dictionary item.
 *
 * <p>Pure POJO — no persistence framework annotations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DictItem {

    private String id;
    private String dictCode;
    private String itemLabel;
    private String itemValue;
    /** Null for top-level items; set for cascaded children. */
    private String parentValue;
    private String cssClass;
    private Integer sortOrder;
    private Boolean enabled;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
}

