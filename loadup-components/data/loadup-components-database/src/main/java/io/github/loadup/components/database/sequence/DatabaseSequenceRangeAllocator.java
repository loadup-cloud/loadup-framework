/*
 * #%L
 * Loadup Components Database
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
package io.github.loadup.components.database.sequence;

import static io.github.loadup.components.database.sequence.dataobject.table.Tables.SEQUENCE_ALLOCATION_DO;

import com.mybatisflex.core.logicdelete.LogicDeleteManager;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.tenant.TenantManager;
import io.github.loadup.commons.error.CommonException;
import io.github.loadup.commons.util.TenantUtil;
import io.github.loadup.components.database.sequence.dataobject.SequenceAllocationDO;
import io.github.loadup.components.database.sequence.mapper.SequenceAllocationDOMapper;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.LockSupport;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.TransactionTemplate;

/** Globally shared sequence rows, isolated from caller tenant and business rollback. */
public final class DatabaseSequenceRangeAllocator implements SequenceRangeAllocator {
    private final SequenceAllocationDOMapper mapper;
    private final TransactionTemplate transaction;

    public DatabaseSequenceRangeAllocator(SequenceAllocationDOMapper mapper, PlatformTransactionManager manager) {
        this.mapper = mapper;
        transaction = new TransactionTemplate(manager);
        transaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        transaction.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        transaction.setTimeout(3);
    }

    public SequenceRange reserve(SequenceScope scope, int step) {
        if (scope == null || step < 1) throw new CommonException(DatabaseResultCodeEnum.INVALID_INPUT);
        for (int attempt = 0; attempt < 8; attempt++) {
            try {
                return TenantUtil.callWithTenant(
                        "__sequence__",
                        () -> TenantManager.withoutTenantCondition(() -> LogicDeleteManager.execWithoutLogicDelete(
                                () -> transaction.execute(status -> allocate(scope, step)))));
            } catch (DuplicateKeyException | TransientDataAccessException conflict) {
                if (attempt == 7) throw new CommonException(DatabaseResultCodeEnum.UNAVAILABLE, conflict);
                LockSupport.parkNanos(
                        ThreadLocalRandom.current().nextLong(1_000_000L, Math.min(50_000_000L, 2_000_000L << attempt)));
                if (Thread.currentThread().isInterrupted())
                    throw new CommonException(DatabaseResultCodeEnum.UNAVAILABLE, conflict);
            } catch (DataAccessException | TransactionException failure) {
                throw new CommonException(DatabaseResultCodeEnum.UNAVAILABLE, failure);
            }
        }
        throw new CommonException(DatabaseResultCodeEnum.UNAVAILABLE);
    }

    private SequenceRange allocate(SequenceScope scope, int step) {
        SequenceAllocationDO row = mapper.selectOneByQuery(QueryWrapper.create()
                .where(SEQUENCE_ALLOCATION_DO.SCOPE_KEY.eq(scope.key()))
                .forUpdate());
        if (row == null) {
            row = new SequenceAllocationDO();
            // Allocation metadata must not recursively invoke the business sequence.
            row.setId(java.util.UUID.randomUUID().toString());
            row.setScopeKey(scope.key());
            row.setMaxAllocated(0L);
            row.setMaxValue(scope.maximum());
            mapper.insert(row);
        }
        if (row.getDeleted() != 0
                || row.getMaxAllocated() == null
                || row.getMaxAllocated() < 0
                || row.getMaxValue() == null
                || row.getMaxAllocated() > row.getMaxValue())
            throw new CommonException(DatabaseResultCodeEnum.CORRUPT_STATE);
        if (row.getMaxValue() != scope.maximum()) throw new CommonException(DatabaseResultCodeEnum.CONFIG_CONFLICT);
        long first = row.getMaxAllocated() + 1;
        if (first > scope.maximum()) throw new CommonException(DatabaseResultCodeEnum.EXHAUSTED);
        long last = row.getMaxAllocated() + Math.min((long) step, scope.maximum() - row.getMaxAllocated());
        row.setMaxAllocated(last);
        if (mapper.update(row) != 1) throw new CommonException(DatabaseResultCodeEnum.CORRUPT_STATE);
        return new SequenceRange(first, last);
    }
}
