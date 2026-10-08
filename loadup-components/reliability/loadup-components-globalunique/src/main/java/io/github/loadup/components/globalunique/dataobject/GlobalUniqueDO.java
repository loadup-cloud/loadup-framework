package io.github.loadup.components.globalunique.dataobject;

import com.mybatisflex.annotation.Table;
import io.github.loadup.commons.dataobject.BaseDO;

/** Persistence record for a tenant-scoped idempotency claim. */
@Table("global_unique")
public class GlobalUniqueDO extends BaseDO {
    private static final long serialVersionUID = 1L;

    private String uniqueKey;
    private String bizType;
    private String bizId;
    private String requestData;

    public String getUniqueKey() {
        return uniqueKey;
    }

    public void setUniqueKey(String uniqueKey) {
        this.uniqueKey = uniqueKey;
    }

    public String getBizType() {
        return bizType;
    }

    public void setBizType(String bizType) {
        this.bizType = bizType;
    }

    public String getBizId() {
        return bizId;
    }

    public void setBizId(String bizId) {
        this.bizId = bizId;
    }

    public String getRequestData() {
        return requestData;
    }

    public void setRequestData(String requestData) {
        this.requestData = requestData;
    }
}
