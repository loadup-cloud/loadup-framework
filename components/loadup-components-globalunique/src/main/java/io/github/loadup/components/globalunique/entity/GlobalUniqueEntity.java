package io.github.loadup.components.globalunique.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 全局唯一性记录实体
 *
 * @author loadup
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GlobalUniqueEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private String id;

    /**
     * 唯一键（业务方自定义）
     */
    private String uniqueKey;

    /**
     * 业务类型
     */
    private String bizType;

    /**
     * 业务ID（可选）
     */
    private String bizId;

    /**
     * 请求数据快照（可选，JSON格式）
     */
    private String requestData;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}

