/*
 * #%L
 * LoadUp Components DelayedTask
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
package io.github.loadup.components.delayedtask;

import static io.github.loadup.components.delayedtask.dataobject.table.Tables.*;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.tenant.TenantManager;
import com.mybatisflex.core.util.UpdateEntity;
import io.github.loadup.components.delayedtask.dataobject.DelayedTaskDO;
import io.github.loadup.components.delayedtask.mapper.DelayedTaskDOMapper;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.transaction.annotation.*;

/** Short claim/ack transactions with token and lease fencing. */
public class DelayedTaskStore {
    private final DelayedTaskDOMapper mapper;

    public DelayedTaskStore(DelayedTaskDOMapper mapper) {
        this.mapper = mapper;
    }

    DelayedTaskDOMapper mapper() {
        return mapper;
    }

    static QueryWrapper key(String tenant, String type, String biz) {
        return QueryWrapper.create()
                .from(DELAYED_TASK_DO)
                .where(DELAYED_TASK_DO.TENANT_ID.eq(tenant))
                .and(DELAYED_TASK_DO.TASK_TYPE.eq(type))
                .and(DELAYED_TASK_DO.BIZ_ID.eq(biz));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recoverExhausted(LocalDateTime now) {
        TenantManager.withoutTenantCondition(() -> {
            var patch = UpdateEntity.of(DelayedTaskDO.class);
            patch.setStatus(DelayedTaskStatus.FAILED);
            patch.setClaimToken(null);
            patch.setLeaseUntil(null);
            patch.setLastError("Lease expired after final attempt");
            mapper.updateByQuery(
                    patch,
                    QueryWrapper.create()
                            .from(DELAYED_TASK_DO)
                            .where(DELAYED_TASK_DO.STATUS.eq("RUNNING"))
                            .and(DELAYED_TASK_DO.LEASE_UNTIL.le(now))
                            .and(DELAYED_TASK_DO.ATTEMPTS.ge(DELAYED_TASK_DO.MAX_ATTEMPTS)));
            return null;
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
    public Optional<DelayedTaskDO> claim(LocalDateTime now, DelayedTaskProperties props) {
        return TenantManager.withoutTenantCondition(() -> {
            var eligible = DELAYED_TASK_DO
                    .STATUS
                    .eq("PENDING")
                    .or(DELAYED_TASK_DO.STATUS.eq("RUNNING").and(DELAYED_TASK_DO.LEASE_UNTIL.le(now)));
            var candidates = mapper.selectListByQuery(QueryWrapper.create()
                    .from(DELAYED_TASK_DO)
                    .where(DELAYED_TASK_DO.NEXT_EXECUTE_AT.le(now))
                    .and(eligible)
                    .and(DELAYED_TASK_DO.ATTEMPTS.lt(DELAYED_TASK_DO.MAX_ATTEMPTS))
                    .orderBy(DELAYED_TASK_DO.NEXT_EXECUTE_AT.asc(), DELAYED_TASK_DO.ID.asc())
                    .limit(props.getBatchSize()));
            if (candidates == null) return Optional.empty();
            for (var row : candidates) {
                var patch = UpdateEntity.of(DelayedTaskDO.class);
                patch.setStatus(DelayedTaskStatus.RUNNING);
                patch.setAttempts(row.getAttempts() + 1);
                patch.setClaimToken(UUID.randomUUID().toString());
                patch.setLeaseUntil(now.plus(props.getLease()));
                var expected = QueryWrapper.create()
                        .from(DELAYED_TASK_DO)
                        .where(DELAYED_TASK_DO.ID.eq(row.getId()))
                        .and(DELAYED_TASK_DO.STATUS.eq(row.getStatus().name()))
                        .and(DELAYED_TASK_DO.ATTEMPTS.eq(row.getAttempts()))
                        .and(DELAYED_TASK_DO.NEXT_EXECUTE_AT.eq(row.getNextExecuteAt()))
                        .and(DELAYED_TASK_DO.EXECUTE_AT.eq(row.getExecuteAt()));
                if (row.getClaimToken() == null) expected.and(DELAYED_TASK_DO.CLAIM_TOKEN.isNull());
                else
                    expected.and(DELAYED_TASK_DO.CLAIM_TOKEN.eq(row.getClaimToken()))
                            .and(DELAYED_TASK_DO.LEASE_UNTIL.le(now));
                if (mapper.updateByQuery(patch, expected) == 1) {
                    row.setStatus(DelayedTaskStatus.RUNNING);
                    row.setAttempts(patch.getAttempts());
                    row.setClaimToken(patch.getClaimToken());
                    row.setLeaseUntil(patch.getLeaseUntil());
                    return Optional.of(row);
                }
            }
            return Optional.empty();
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean finish(DelayedTaskDO claimed, LocalDateTime now, boolean success, DelayedTaskProperties props) {
        return TenantManager.withoutTenantCondition(() -> {
            var patch = UpdateEntity.of(DelayedTaskDO.class);
            patch.setStatus(
                    success
                            ? DelayedTaskStatus.SUCCEEDED
                            : claimed.getAttempts() >= claimed.getMaxAttempts()
                                    ? DelayedTaskStatus.FAILED
                                    : DelayedTaskStatus.PENDING);
            patch.setClaimToken(null);
            patch.setLeaseUntil(null);
            patch.setLastError(success ? null : "Handler failed");
            if (!success) patch.setNextExecuteAt(now.plus(props.getRetryDelay()));
            return mapper.updateByQuery(
                            patch,
                            QueryWrapper.create()
                                    .from(DELAYED_TASK_DO)
                                    .where(DELAYED_TASK_DO.ID.eq(claimed.getId()))
                                    .and(DELAYED_TASK_DO.STATUS.eq("RUNNING"))
                                    .and(DELAYED_TASK_DO.CLAIM_TOKEN.eq(claimed.getClaimToken()))
                                    .and(DELAYED_TASK_DO.LEASE_UNTIL.gt(now)))
                    == 1;
        });
    }
}
