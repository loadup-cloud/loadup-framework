package io.github.loadup.components.globalunique.service.impl;

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

