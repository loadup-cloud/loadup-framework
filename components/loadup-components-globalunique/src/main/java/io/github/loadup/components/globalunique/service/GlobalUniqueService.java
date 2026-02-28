package io.github.loadup.components.globalunique.service;

/*-
 * #%L
 * LoadUp Components :: Global Unique
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

/**
 * 全局唯一性服务接口
 *
 * @author loadup
 */
public interface GlobalUniqueService {

    /**
     * 插入并检查唯一性（核心方法）
     *
     * @param uniqueKey 唯一键（业务方自行拼接，如: "ORDER_CREATE:userId:orderId"）
     * @param bizType   业务类型（用于分类统计，如: "ORDER", "PAYMENT"）
     * @return true=首次插入成功(可执行业务), false=已存在(幂等拦截)
     */
    boolean insertAndCheck(String uniqueKey, String bizType);

    /**
     * 插入并检查（带业务ID）
     *
     * @param uniqueKey 唯一键
     * @param bizType   业务类型
     * @param bizId     业务ID（可选，方便后续查询）
     * @return true=首次插入成功(可执行业务), false=已存在(幂等拦截)
     */
    boolean insertAndCheck(String uniqueKey, String bizType, String bizId);

    /**
     * 插入并检查（带请求数据快照）
     *
     * @param uniqueKey   唯一键
     * @param bizType     业务类型
     * @param bizId       业务ID（可选）
     * @param requestData 请求数据JSON（可选，用于问题排查）
     * @return true=首次插入成功(可执行业务), false=已存在(幂等拦截)
     */
    boolean insertAndCheck(String uniqueKey, String bizType, String bizId, String requestData);
}
