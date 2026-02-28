package io.github.loadup.components.globalunique.service.impl;

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

import io.github.loadup.components.globalunique.entity.GlobalUniqueEntity;
import io.github.loadup.components.globalunique.mapper.GlobalUniqueMapper;
import io.github.loadup.components.globalunique.service.GlobalUniqueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

/**
 * 全局唯一性服务实现
 *
 * @author loadup
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GlobalUniqueServiceImpl implements GlobalUniqueService {

    private final GlobalUniqueMapper globalUniqueMapper;

    @Override
    public boolean insertAndCheck(String uniqueKey, String bizType) {
        return insertAndCheck(uniqueKey, bizType, null, null);
    }

    @Override
    public boolean insertAndCheck(String uniqueKey, String bizType, String bizId) {
        return insertAndCheck(uniqueKey, bizType, bizId, null);
    }

    @Override
    public boolean insertAndCheck(String uniqueKey, String bizType, String bizId, String requestData) {
        try {
            GlobalUniqueEntity entity = GlobalUniqueEntity.builder()
                    .uniqueKey(uniqueKey)
                    .bizType(bizType)
                    .bizId(bizId)
                    .requestData(requestData)
                    .build();

            int inserted = globalUniqueMapper.insert(entity);

            if (inserted == 1) {
                log.debug("全局唯一性检查通过: uniqueKey={}, bizType={}, bizId={}", uniqueKey, bizType, bizId);
                return true;
            }

            return false;

        } catch (DuplicateKeyException e) {
            // 唯一键冲突 = 幂等拦截
            log.debug("幂等拦截: uniqueKey={}, bizType={}, bizId={}", uniqueKey, bizType, bizId);
            return false;
        }
    }
}
