package io.github.loadup.components.globalunique.service.impl;

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

import io.github.loadup.components.globalunique.entity.GlobalUniqueEntity;
import io.github.loadup.components.globalunique.mapper.GlobalUniqueMapper;
import io.github.loadup.components.globalunique.service.GlobalUniqueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;

/**
 * Default {@link GlobalUniqueService} backed by {@link GlobalUniqueMapper}.
 */
public class GlobalUniqueServiceImpl implements GlobalUniqueService {
    private static final Logger log = LoggerFactory.getLogger(GlobalUniqueServiceImpl.class);

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
        if (uniqueKey == null || uniqueKey.isBlank()) {
            throw new IllegalArgumentException("uniqueKey must not be blank");
        }
        try {
            GlobalUniqueEntity entity = GlobalUniqueEntity.builder()
                    .uniqueKey(uniqueKey)
                    .bizType(bizType)
                    .bizId(bizId)
                    .requestData(requestData)
                    .build();

            int inserted = globalUniqueMapper.insert(entity);

            if (inserted > 0) {
                log.debug("Unique key claimed: uniqueKey={}, bizType={}, bizId={}", uniqueKey, bizType, bizId);
                return true;
            }

            return false;

        } catch (DuplicateKeyException e) {
            log.debug("Idempotent replay rejected: uniqueKey={}, bizType={}, bizId={}", uniqueKey, bizType, bizId);
            return false;
        }
    }

    public GlobalUniqueServiceImpl(GlobalUniqueMapper globalUniqueMapper) {
        this.globalUniqueMapper = globalUniqueMapper;
    }
}
