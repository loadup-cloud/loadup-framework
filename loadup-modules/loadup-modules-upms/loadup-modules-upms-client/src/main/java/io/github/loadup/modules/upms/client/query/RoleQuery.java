package io.github.loadup.modules.upms.client.query;

/**
 * Role Query Parameters
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
public class RoleQuery {

    private String roleName;
    private String roleCode;
    private String parentId;
    private Short status;
    private Boolean deleted;

    // Pagination
    private Integer page = 1;
    private Integer size = 20;
    private String sortBy = "sortOrder";
    private String sortOrder = "ASC";

    public RoleQuery(
            String roleName,
            String roleCode,
            String parentId,
            Short status,
            Boolean deleted,
            Integer page,
            Integer size,
            String sortBy,
            String sortOrder) {
        this.roleName = roleName;
        this.roleCode = roleCode;
        this.parentId = parentId;
        this.status = status;
        this.deleted = deleted;
        this.page = page;
        this.size = size;
        this.sortBy = sortBy;
        this.sortOrder = sortOrder;
    }

    public RoleQuery() {}

    public String getRoleName() {
        return this.roleName;
    }

    public String getRoleCode() {
        return this.roleCode;
    }

    public String getParentId() {
        return this.parentId;
    }

    public Short getStatus() {
        return this.status;
    }

    public Boolean isDeleted() {
        return this.deleted;
    }

    public Integer getPage() {
        return this.page;
    }

    public Integer getSize() {
        return this.size;
    }

    public String getSortBy() {
        return this.sortBy;
    }

    public String getSortOrder() {
        return this.sortOrder;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public void setStatus(Short status) {
        this.status = status;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String roleName;
        private String roleCode;
        private String parentId;
        private Short status;
        private Boolean deleted;
        private Integer page = 1;
        private Integer size = 20;
        private String sortBy = "sortOrder";
        private String sortOrder = "ASC";

        public Builder roleName(String roleName) {
            this.roleName = roleName;
            return this;
        }

        public Builder roleCode(String roleCode) {
            this.roleCode = roleCode;
            return this;
        }

        public Builder parentId(String parentId) {
            this.parentId = parentId;
            return this;
        }

        public Builder status(Short status) {
            this.status = status;
            return this;
        }

        public Builder deleted(Boolean deleted) {
            this.deleted = deleted;
            return this;
        }

        public Builder page(Integer page) {
            this.page = page;
            return this;
        }

        public Builder size(Integer size) {
            this.size = size;
            return this;
        }

        public Builder sortBy(String sortBy) {
            this.sortBy = sortBy;
            return this;
        }

        public Builder sortOrder(String sortOrder) {
            this.sortOrder = sortOrder;
            return this;
        }

        public RoleQuery build() {
            return new RoleQuery(
                    this.roleName,
                    this.roleCode,
                    this.parentId,
                    this.status,
                    this.deleted,
                    this.page,
                    this.size,
                    this.sortBy,
                    this.sortOrder);
        }
    }

    @Override
    public String toString() {
        return org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString(
                this, org.apache.commons.lang3.builder.ToStringStyle.JSON_STYLE);
    }
}
