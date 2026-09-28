package io.github.loadup.commons.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import java.io.Serializable;
import java.time.LocalDateTime;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/** Common persistent fields for MyBatis-Flex data objects. */
public abstract class BaseDO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.None)
    private String id;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String tenantId;

    private Integer deleted;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }

    public String getId() {
        return this.id;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    public String getTenantId() {
        return this.tenantId;
    }

    public Integer getDeleted() {
        return this.deleted;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }
}
