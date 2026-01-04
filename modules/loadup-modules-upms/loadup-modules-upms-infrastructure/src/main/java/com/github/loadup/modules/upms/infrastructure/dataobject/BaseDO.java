package com.github.loadup.modules.upms.infrastructure.dataobject;

import java.time.LocalDateTime;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.InsertOnlyProperty;

/**
 * Base Data Object for database mapping
 *
 * <p>All DataObjects should extend this class to get common fields
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Data
public abstract class BaseDO {

  @Id private String id;

  private String createdBy;

  @CreatedDate @InsertOnlyProperty private LocalDateTime createdTime;

  private String updatedBy;

  @LastModifiedDate private LocalDateTime updatedTime;

  private Boolean deleted = false;
}
