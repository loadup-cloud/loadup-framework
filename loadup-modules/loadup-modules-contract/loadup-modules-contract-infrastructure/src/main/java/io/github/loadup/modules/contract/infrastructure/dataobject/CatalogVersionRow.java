package io.github.loadup.modules.contract.infrastructure.dataobject;

import com.mybatisflex.annotation.Table;
import io.github.loadup.commons.dataobject.BaseDO;
import java.time.LocalDateTime;

/**
 * Row model for catalog versions.
 */
@Table("contract_catalog_version")
public class CatalogVersionRow extends BaseDO {
    private String kind;
    private String code;
    private int version;
    private String status;
    private long rowVersion;
    private String content;
    private String updatedBy;

    public CatalogVersionRow() {}

    public CatalogVersionRow(
            String id,
            String kind,
            String code,
            int version,
            String status,
            long rowVersion,
            String content,
            String updatedBy,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            boolean deleted) {
        setId(id);
        this.kind = kind;
        this.code = code;
        this.version = version;
        this.status = status;
        this.rowVersion = rowVersion;
        this.content = content;
        this.updatedBy = updatedBy;
        setCreatedAt(createdAt);
        setUpdatedAt(updatedAt);
        setDeleted(deleted);
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String value) {
        this.kind = value;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String value) {
        this.code = value;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int value) {
        this.version = value;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String value) {
        this.status = value;
    }

    public long getRowVersion() {
        return rowVersion;
    }

    public void setRowVersion(long value) {
        this.rowVersion = value;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String value) {
        this.content = value;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String value) {
        this.updatedBy = value;
    }
}
