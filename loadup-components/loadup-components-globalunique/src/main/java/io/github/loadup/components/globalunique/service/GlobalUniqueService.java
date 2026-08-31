package io.github.loadup.components.globalunique.service;

/*-
 * #%L
 * LoadUp Components :: Global Unique
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
