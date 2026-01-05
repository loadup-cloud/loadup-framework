package com.github.loadup.modules.upms.infrastructure.repository;

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

import static com.github.loadup.modules.upms.infrastructure.dataobject.table.Tables.LOGIN_LOG_DO;

import com.github.loadup.modules.upms.domain.entity.LoginLog;
import com.github.loadup.modules.upms.domain.repository.LoginLogRepository;
import com.github.loadup.modules.upms.infrastructure.converter.LoginLogConverter;
import com.github.loadup.modules.upms.infrastructure.dataobject.LoginLogDO;
import com.github.loadup.modules.upms.infrastructure.mapper.LoginLogDOMapper;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

/**
 * LoginLog Repository Implementation using MyBatis-Flex
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Repository
@RequiredArgsConstructor
public class LoginLogRepositoryImpl implements LoginLogRepository {

  private final LoginLogDOMapper loginLogDOMapper;
  private final LoginLogConverter loginLogConverter;

  @Override
  public LoginLog save(LoginLog log) {
    LoginLogDO loginLogDO = loginLogConverter.toDataObject(log);
    loginLogDOMapper.insert(loginLogDO);
    return loginLogConverter.toEntity(loginLogDO);
  }

  @Override
  public Optional<LoginLog> findById(String id) {
    LoginLogDO loginLogDO = loginLogDOMapper.selectOneById(id);
    return Optional.ofNullable(loginLogDO).map(loginLogConverter::toEntity);
  }

  @Override
  public org.springframework.data.domain.Page<LoginLog> findByUserId(
      String userId, Pageable pageable) {
    QueryWrapper query =
        QueryWrapper.create()
            .where(LOGIN_LOG_DO.USER_ID.eq(userId))
            .orderBy(LOGIN_LOG_DO.LOGIN_TIME.desc());

    Page<LoginLogDO> page =
        loginLogDOMapper.paginate(
            Page.of(pageable.getPageNumber() + 1, pageable.getPageSize()), query);

    List<LoginLog> logs =
        page.getRecords().stream().map(loginLogConverter::toEntity).collect(Collectors.toList());

    return new PageImpl<>(logs, pageable, page.getTotalRow());
  }

  @Override
  public org.springframework.data.domain.Page<LoginLog> findByUsername(
      String username, Pageable pageable) {
    QueryWrapper query =
        QueryWrapper.create()
            .where(LOGIN_LOG_DO.USERNAME.eq(username))
            .orderBy(LOGIN_LOG_DO.LOGIN_TIME.desc());

    Page<LoginLogDO> page =
        loginLogDOMapper.paginate(
            Page.of(pageable.getPageNumber() + 1, pageable.getPageSize()), query);

    List<LoginLog> logs =
        page.getRecords().stream().map(loginLogConverter::toEntity).collect(Collectors.toList());

    return new PageImpl<>(logs, pageable, page.getTotalRow());
  }

  @Override
  public org.springframework.data.domain.Page<LoginLog> findByDateRange(
      LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
    QueryWrapper query =
        QueryWrapper.create()
            .where(LOGIN_LOG_DO.LOGIN_TIME.between(startTime, endTime))
            .orderBy(LOGIN_LOG_DO.LOGIN_TIME.desc());

    Page<LoginLogDO> page =
        loginLogDOMapper.paginate(
            Page.of(pageable.getPageNumber() + 1, pageable.getPageSize()), query);

    List<LoginLog> logs =
        page.getRecords().stream().map(loginLogConverter::toEntity).collect(Collectors.toList());

    return new PageImpl<>(logs, pageable, page.getTotalRow());
  }

  @Override
  public org.springframework.data.domain.Page<LoginLog> findFailedLogins(
      LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
    QueryWrapper query =
        QueryWrapper.create()
            .where(LOGIN_LOG_DO.LOGIN_STATUS.eq((short) 0))
            .and(LOGIN_LOG_DO.LOGIN_TIME.between(startTime, endTime))
            .orderBy(LOGIN_LOG_DO.LOGIN_TIME.desc());

    Page<LoginLogDO> page =
        loginLogDOMapper.paginate(
            Page.of(pageable.getPageNumber() + 1, pageable.getPageSize()), query);

    List<LoginLog> logs =
        page.getRecords().stream().map(loginLogConverter::toEntity).collect(Collectors.toList());

    return new PageImpl<>(logs, pageable, page.getTotalRow());
  }

  @Override
  public void deleteBeforeDate(LocalDateTime date) {
    loginLogDOMapper.deleteByQuery(QueryWrapper.create().where(LOGIN_LOG_DO.LOGIN_TIME.lt(date)));
  }

  @Override
  public List<LoginLog> findByLoginTimeBetween(LocalDateTime startTime, LocalDateTime endTime) {
    QueryWrapper query =
        QueryWrapper.create()
            .where(LOGIN_LOG_DO.LOGIN_TIME.between(startTime, endTime))
            .orderBy(LOGIN_LOG_DO.LOGIN_TIME.desc());
    List<LoginLogDO> loginLogDOs = loginLogDOMapper.selectListByQuery(query);
    return loginLogDOs.stream().map(loginLogConverter::toEntity).collect(Collectors.toList());
  }

  @Override
  public org.springframework.data.domain.Page<LoginLog> findAll(Pageable pageable) {
    QueryWrapper query = QueryWrapper.create().orderBy(LOGIN_LOG_DO.LOGIN_TIME.desc());

    Page<LoginLogDO> page =
        loginLogDOMapper.paginate(
            Page.of(pageable.getPageNumber() + 1, pageable.getPageSize()), query);

    List<LoginLog> logs =
        page.getRecords().stream().map(loginLogConverter::toEntity).collect(Collectors.toList());

    return new PageImpl<>(logs, pageable, page.getTotalRow());
  }

  @Override
  public long countLoginAttempts(String userId, LocalDateTime startTime, LocalDateTime endTime) {
    QueryWrapper query =
        QueryWrapper.create()
            .where(LOGIN_LOG_DO.USER_ID.eq(userId))
            .and(LOGIN_LOG_DO.LOGIN_TIME.between(startTime, endTime));
    return loginLogDOMapper.selectCountByQuery(query);
  }

  @Override
  public long countFailedLoginAttempts(
      String userId, LocalDateTime startTime, LocalDateTime endTime) {
    QueryWrapper query =
        QueryWrapper.create()
            .where(LOGIN_LOG_DO.USER_ID.eq(userId))
            .and(LOGIN_LOG_DO.LOGIN_STATUS.eq((short) 0))
            .and(LOGIN_LOG_DO.LOGIN_TIME.between(startTime, endTime));
    return loginLogDOMapper.selectCountByQuery(query);
  }

  @Override
  public List<LoginLog> findByUserId(String userId) {
    QueryWrapper query =
        QueryWrapper.create()
            .where(LOGIN_LOG_DO.USER_ID.eq(userId))
            .orderBy(LOGIN_LOG_DO.LOGIN_TIME.desc());
    List<LoginLogDO> loginLogDOs = loginLogDOMapper.selectListByQuery(query);
    return loginLogDOs.stream().map(loginLogConverter::toEntity).collect(Collectors.toList());
  }

  @Override
  public Optional<LoginLog> findLastSuccessfulLogin(String userId) {
    QueryWrapper query =
        QueryWrapper.create()
            .where(LOGIN_LOG_DO.USER_ID.eq(userId))
            .and(LOGIN_LOG_DO.LOGIN_STATUS.eq((short) 1))
            .orderBy(LOGIN_LOG_DO.LOGIN_TIME.desc())
            .limit(1);
    LoginLogDO loginLogDO = loginLogDOMapper.selectOneByQuery(query);
    return Optional.ofNullable(loginLogDO).map(loginLogConverter::toEntity);
  }
}
