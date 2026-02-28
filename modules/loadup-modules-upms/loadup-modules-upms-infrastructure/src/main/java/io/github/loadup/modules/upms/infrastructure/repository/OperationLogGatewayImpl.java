package io.github.loadup.modules.upms.infrastructure.repository;

/*-
 * #%L
 * loadup-modules-upms-infrastructure
 * %%
 * Copyright (C) 2022 - 2026 loadup_cloud
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

import static io.github.loadup.modules.upms.infrastructure.dataobject.table.Tables.OPERATION_LOG_DO;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import io.github.loadup.modules.upms.domain.entity.OperationLog;
import io.github.loadup.modules.upms.domain.gateway.OperationLogGateway;
import io.github.loadup.modules.upms.infrastructure.converter.OperationLogConverter;
import io.github.loadup.modules.upms.infrastructure.dataobject.OperationLogDO;
import io.github.loadup.modules.upms.infrastructure.mapper.OperationLogDOMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

/**
 * OperationLog Repository Implementation using MyBatis-Flex
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Repository
@RequiredArgsConstructor
public class OperationLogGatewayImpl implements OperationLogGateway {

    private final OperationLogDOMapper operationLogDOMapper;
    private final OperationLogConverter operationLogConverter;

    @Override
    public OperationLog save(OperationLog entity) {
        OperationLogDO operationLogDO = operationLogConverter.toDataObject(entity);
        operationLogDOMapper.insert(operationLogDO);
        entity = operationLogConverter.toEntity(operationLogDO);
        return entity;
    }

    @Override
    public void batchSave(List<OperationLog> logs) {
        List<OperationLogDO> operationLogDOs =
                logs.stream().map(operationLogConverter::toDataObject).collect(Collectors.toList());
        operationLogDOMapper.insertBatch(operationLogDOs);
    }

    @Override
    public Optional<OperationLog> findById(String id) {
        OperationLogDO operationLogDO = operationLogDOMapper.selectOneById(id);
        return Optional.ofNullable(operationLogDO).map(operationLogConverter::toEntity);
    }

    @Override
    public org.springframework.data.domain.Page<OperationLog> findByUserId(String userId, Pageable pageable) {
        QueryWrapper query = QueryWrapper.create()
                .where(OPERATION_LOG_DO.USER_ID.eq(userId))
                .orderBy(OPERATION_LOG_DO.CREATED_AT.desc());

        Page<OperationLogDO> page =
                operationLogDOMapper.paginate(Page.of(pageable.getPageNumber() + 1, pageable.getPageSize()), query);

        List<OperationLog> logs =
                page.getRecords().stream().map(operationLogConverter::toEntity).collect(Collectors.toList());

        return new PageImpl<>(logs, pageable, page.getTotalRow());
    }

    @Override
    public org.springframework.data.domain.Page<OperationLog> findByOperationType(
            String operationType, Pageable pageable) {
        QueryWrapper query = QueryWrapper.create()
                .where(OPERATION_LOG_DO.OPERATION_TYPE.eq(operationType))
                .orderBy(OPERATION_LOG_DO.CREATED_AT.desc());

        Page<OperationLogDO> page =
                operationLogDOMapper.paginate(Page.of(pageable.getPageNumber() + 1, pageable.getPageSize()), query);

        List<OperationLog> logs =
                page.getRecords().stream().map(operationLogConverter::toEntity).collect(Collectors.toList());

        return new PageImpl<>(logs, pageable, page.getTotalRow());
    }

    @Override
    public org.springframework.data.domain.Page<OperationLog> findByDateRange(
            LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        QueryWrapper query = QueryWrapper.create()
                .where(OPERATION_LOG_DO.CREATED_AT.between(startTime, endTime))
                .orderBy(OPERATION_LOG_DO.CREATED_AT.desc());

        Page<OperationLogDO> page =
                operationLogDOMapper.paginate(Page.of(pageable.getPageNumber() + 1, pageable.getPageSize()), query);

        List<OperationLog> logs =
                page.getRecords().stream().map(operationLogConverter::toEntity).collect(Collectors.toList());

        return new PageImpl<>(logs, pageable, page.getTotalRow());
    }

    @Override
    public org.springframework.data.domain.Page<OperationLog> search(
            String userId,
            String operationType,
            String module,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Pageable pageable) {
        QueryWrapper query = QueryWrapper.create();

        if (userId != null) {
            query.where(OPERATION_LOG_DO.USER_ID.eq(userId));
        }
        if (operationType != null) {
            query.and(OPERATION_LOG_DO.OPERATION_TYPE.eq(operationType));
        }
        if (module != null) {
            query.and(OPERATION_LOG_DO.OPERATION_MODULE.like(module));
        }
        if (startTime != null && endTime != null) {
            query.and(OPERATION_LOG_DO.CREATED_AT.between(startTime, endTime));
        }

        query.orderBy(OPERATION_LOG_DO.CREATED_AT.desc());

        Page<OperationLogDO> page =
                operationLogDOMapper.paginate(Page.of(pageable.getPageNumber() + 1, pageable.getPageSize()), query);

        List<OperationLog> logs =
                page.getRecords().stream().map(operationLogConverter::toEntity).collect(Collectors.toList());

        return new PageImpl<>(logs, pageable, page.getTotalRow());
    }

    @Override
    public void deleteBeforeDate(LocalDateTime date) {
        operationLogDOMapper.deleteByQuery(QueryWrapper.create().where(OPERATION_LOG_DO.CREATED_AT.lt(date)));
    }

    @Override
    public List<OperationLog> findByUserId(String userId) {
        QueryWrapper query = QueryWrapper.create()
                .where(OPERATION_LOG_DO.USER_ID.eq(userId))
                .orderBy(OPERATION_LOG_DO.CREATED_AT.desc());
        List<OperationLogDO> operationLogDOs = operationLogDOMapper.selectListByQuery(query);
        return operationLogDOs.stream().map(operationLogConverter::toEntity).collect(Collectors.toList());
    }

    @Override
    public List<OperationLog> findByOperationType(String operationType) {
        QueryWrapper query = QueryWrapper.create()
                .where(OPERATION_LOG_DO.OPERATION_TYPE.eq(operationType))
                .orderBy(OPERATION_LOG_DO.CREATED_AT.desc());
        List<OperationLogDO> operationLogDOs = operationLogDOMapper.selectListByQuery(query);
        return operationLogDOs.stream().map(operationLogConverter::toEntity).collect(Collectors.toList());
    }

    @Override
    public List<OperationLog> findByCreatedTimeBetween(LocalDateTime startTime, LocalDateTime endTime) {
        QueryWrapper query = QueryWrapper.create()
                .where(OPERATION_LOG_DO.CREATED_AT.between(startTime, endTime))
                .orderBy(OPERATION_LOG_DO.CREATED_AT.desc());
        List<OperationLogDO> operationLogDOs = operationLogDOMapper.selectListByQuery(query);
        return operationLogDOs.stream().map(operationLogConverter::toEntity).collect(Collectors.toList());
    }

    @Override
    public long countByUserId(String userId) {
        QueryWrapper query = QueryWrapper.create().where(OPERATION_LOG_DO.USER_ID.eq(userId));
        return operationLogDOMapper.selectCountByQuery(query);
    }

    @Override
    public org.springframework.data.domain.Page<OperationLog> findAll(Pageable pageable) {
        QueryWrapper query = QueryWrapper.create().orderBy(OPERATION_LOG_DO.CREATED_AT.desc());

        Page<OperationLogDO> page =
                operationLogDOMapper.paginate(Page.of(pageable.getPageNumber() + 1, pageable.getPageSize()), query);

        List<OperationLog> logs =
                page.getRecords().stream().map(operationLogConverter::toEntity).collect(Collectors.toList());

        return new PageImpl<>(logs, pageable, page.getTotalRow());
    }

    @Override
    public long countFailedOperations(LocalDateTime startTime, LocalDateTime endTime) {
        QueryWrapper query = QueryWrapper.create()
                .where(OPERATION_LOG_DO.CREATED_AT.between(startTime, endTime))
                .and(OPERATION_LOG_DO.STATUS.eq(0));
        return operationLogDOMapper.selectCountByQuery(query);
    }
}
