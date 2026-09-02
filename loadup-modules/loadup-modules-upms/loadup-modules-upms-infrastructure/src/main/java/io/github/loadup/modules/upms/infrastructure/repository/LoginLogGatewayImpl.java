package io.github.loadup.modules.upms.infrastructure.repository;

/*-
 * #%L
 * loadup-modules-upms-infrastructure
 * %%
 * Copyright (C) 2022 - 2026 loadup_cloud
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

import static io.github.loadup.modules.upms.infrastructure.dataobject.table.Tables.LOGIN_LOG_DO;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import io.github.loadup.commons.dto.PageQuery;
import io.github.loadup.commons.domain.PageResult;
import io.github.loadup.modules.upms.domain.entity.LoginLog;
import io.github.loadup.modules.upms.domain.gateway.LoginLogGateway;
import io.github.loadup.modules.upms.infrastructure.converter.LoginLogConverter;
import io.github.loadup.modules.upms.infrastructure.dataobject.LoginLogDO;
import io.github.loadup.modules.upms.infrastructure.mapper.LoginLogDOMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

/**
 * LoginLog Repository Implementation using MyBatis-Flex
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Repository
public class LoginLogGatewayImpl implements LoginLogGateway {

    private final LoginLogDOMapper loginLogDOMapper;
    private final LoginLogConverter loginLogConverter;

    @Override
    public LoginLog save(LoginLog entity) {
        LoginLogDO loginLogDO = loginLogConverter.toDataObject(entity);
        loginLogDOMapper.insert(loginLogDO);
        entity = loginLogConverter.toEntity(loginLogDO);
        return entity;
    }

    @Override
    public Optional<LoginLog> findById(String id) {
        LoginLogDO loginLogDO = loginLogDOMapper.selectOneById(id);
        return Optional.ofNullable(loginLogDO).map(loginLogConverter::toEntity);
    }

    @Override
    public PageResult<LoginLog> findByUserId(String userId, PageQuery query) {
        QueryWrapper wrapper =
                QueryWrapper.create().where(LOGIN_LOG_DO.USER_ID.eq(userId)).orderBy(LOGIN_LOG_DO.LOGIN_TIME.desc());

        Page<LoginLogDO> page = loginLogDOMapper.paginate(Page.of(query.pageNum(), query.pageSize()), wrapper);

        List<LoginLog> logs =
                page.getRecords().stream().map(loginLogConverter::toEntity).collect(Collectors.toList());

        return PageResult.of(logs, page.getTotalRow(), query.pageNum(), query.pageSize());
    }

    @Override
    public PageResult<LoginLog> findByUsername(String username, PageQuery query) {
        QueryWrapper wrapper =
                QueryWrapper.create().where(LOGIN_LOG_DO.USERNAME.eq(username)).orderBy(LOGIN_LOG_DO.LOGIN_TIME.desc());

        Page<LoginLogDO> page = loginLogDOMapper.paginate(Page.of(query.pageNum(), query.pageSize()), wrapper);

        List<LoginLog> logs =
                page.getRecords().stream().map(loginLogConverter::toEntity).collect(Collectors.toList());

        return PageResult.of(logs, page.getTotalRow(), query.pageNum(), query.pageSize());
    }

    @Override
    public PageResult<LoginLog> findByDateRange(LocalDateTime startTime, LocalDateTime endTime, PageQuery query) {
        QueryWrapper wrapper = QueryWrapper.create()
                .where(LOGIN_LOG_DO.LOGIN_TIME.between(startTime, endTime))
                .orderBy(LOGIN_LOG_DO.LOGIN_TIME.desc());

        Page<LoginLogDO> page = loginLogDOMapper.paginate(Page.of(query.pageNum(), query.pageSize()), wrapper);

        List<LoginLog> logs =
                page.getRecords().stream().map(loginLogConverter::toEntity).collect(Collectors.toList());

        return PageResult.of(logs, page.getTotalRow(), query.pageNum(), query.pageSize());
    }

    @Override
    public PageResult<LoginLog> findFailedLogins(LocalDateTime startTime, LocalDateTime endTime, PageQuery query) {
        QueryWrapper wrapper = QueryWrapper.create()
                .where(LOGIN_LOG_DO.LOGIN_STATUS.eq((short) 0))
                .and(LOGIN_LOG_DO.LOGIN_TIME.between(startTime, endTime))
                .orderBy(LOGIN_LOG_DO.LOGIN_TIME.desc());

        Page<LoginLogDO> page = loginLogDOMapper.paginate(Page.of(query.pageNum(), query.pageSize()), wrapper);

        List<LoginLog> logs =
                page.getRecords().stream().map(loginLogConverter::toEntity).collect(Collectors.toList());

        return PageResult.of(logs, page.getTotalRow(), query.pageNum(), query.pageSize());
    }

    @Override
    public void deleteBeforeDate(LocalDateTime date) {
        loginLogDOMapper.deleteByQuery(QueryWrapper.create().where(LOGIN_LOG_DO.LOGIN_TIME.lt(date)));
    }

    @Override
    public List<LoginLog> findByLoginTimeBetween(LocalDateTime startTime, LocalDateTime endTime) {
        QueryWrapper query = QueryWrapper.create()
                .where(LOGIN_LOG_DO.LOGIN_TIME.between(startTime, endTime))
                .orderBy(LOGIN_LOG_DO.LOGIN_TIME.desc());
        List<LoginLogDO> loginLogDOs = loginLogDOMapper.selectListByQuery(query);
        return loginLogDOs.stream().map(loginLogConverter::toEntity).collect(Collectors.toList());
    }

    @Override
    public PageResult<LoginLog> findAll(PageQuery query) {
        QueryWrapper wrapper = QueryWrapper.create().orderBy(LOGIN_LOG_DO.LOGIN_TIME.desc());

        Page<LoginLogDO> page = loginLogDOMapper.paginate(Page.of(query.pageNum(), query.pageSize()), wrapper);

        List<LoginLog> logs =
                page.getRecords().stream().map(loginLogConverter::toEntity).collect(Collectors.toList());

        return PageResult.of(logs, page.getTotalRow(), query.pageNum(), query.pageSize());
    }

    @Override
    public long countLoginAttempts(String userId, LocalDateTime startTime, LocalDateTime endTime) {
        QueryWrapper query = QueryWrapper.create()
                .where(LOGIN_LOG_DO.USER_ID.eq(userId))
                .and(LOGIN_LOG_DO.LOGIN_TIME.between(startTime, endTime));
        return loginLogDOMapper.selectCountByQuery(query);
    }

    @Override
    public long countFailedLoginAttempts(String userId, LocalDateTime startTime, LocalDateTime endTime) {
        QueryWrapper query = QueryWrapper.create()
                .where(LOGIN_LOG_DO.USER_ID.eq(userId))
                .and(LOGIN_LOG_DO.LOGIN_STATUS.eq((short) 0))
                .and(LOGIN_LOG_DO.LOGIN_TIME.between(startTime, endTime));
        return loginLogDOMapper.selectCountByQuery(query);
    }

    @Override
    public List<LoginLog> findByUserId(String userId) {
        QueryWrapper query =
                QueryWrapper.create().where(LOGIN_LOG_DO.USER_ID.eq(userId)).orderBy(LOGIN_LOG_DO.LOGIN_TIME.desc());
        List<LoginLogDO> loginLogDOs = loginLogDOMapper.selectListByQuery(query);
        return loginLogDOs.stream().map(loginLogConverter::toEntity).collect(Collectors.toList());
    }

    @Override
    public Optional<LoginLog> findLastSuccessfulLogin(String userId) {
        QueryWrapper query = QueryWrapper.create()
                .where(LOGIN_LOG_DO.USER_ID.eq(userId))
                .and(LOGIN_LOG_DO.LOGIN_STATUS.eq((short) 1))
                .orderBy(LOGIN_LOG_DO.LOGIN_TIME.desc())
                .limit(1);
        LoginLogDO loginLogDO = loginLogDOMapper.selectOneByQuery(query);
        return Optional.ofNullable(loginLogDO).map(loginLogConverter::toEntity);
    }

    public LoginLogGatewayImpl(LoginLogDOMapper loginLogDOMapper, LoginLogConverter loginLogConverter) {
        this.loginLogDOMapper = loginLogDOMapper;
        this.loginLogConverter = loginLogConverter;
    }
}
