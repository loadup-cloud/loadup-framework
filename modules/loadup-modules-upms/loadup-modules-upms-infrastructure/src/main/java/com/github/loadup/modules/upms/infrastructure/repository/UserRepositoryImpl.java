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

import static com.github.loadup.modules.upms.infrastructure.dataobject.table.Tables.USER_DO;

import com.github.loadup.modules.upms.domain.entity.User;
import com.github.loadup.modules.upms.domain.repository.UserRepository;
import com.github.loadup.modules.upms.infrastructure.converter.UserConverter;
import com.github.loadup.modules.upms.infrastructure.dataobject.UserDO;
import com.github.loadup.modules.upms.infrastructure.mapper.UserDOMapper;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

/**
 * User Repository Implementation using MyBatis-Flex
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

  private final UserDOMapper userDOMapper;
  private final UserConverter userConverter;

  @Override
  public User save(User user) {
    UserDO userDO = userConverter.toDataObject(user);
    userDOMapper.insert(userDO);
    user = userConverter.toEntity(userDO);
    return user;
  }

  @Override
  public User update(User user) {
    UserDO userDO = userConverter.toDataObject(user);
    userDOMapper.update(userDO);
    user = userConverter.toEntity(userDO);
    return user;
  }

  @Override
  public void deleteById(String id) {
    userDOMapper.deleteById(id);
  }

  @Override
  public Optional<User> findById(String id) {
    UserDO userDO = userDOMapper.selectOneById(id);
    return Optional.ofNullable(userDO).map(userConverter::toEntity);
  }

  @Override
  public Optional<User> findByUsername(String username) {
    QueryWrapper query = QueryWrapper.create().eq("username", username);
    UserDO userDO = userDOMapper.selectOneByQuery(query);
    return Optional.ofNullable(userDO).map(userConverter::toEntity);
  }

  @Override
  public Optional<User> findByEmail(String email) {
    QueryWrapper query = QueryWrapper.create().eq("email", email);
    UserDO userDO = userDOMapper.selectOneByQuery(query);
    return Optional.ofNullable(userDO).map(userConverter::toEntity);
  }

  @Override
  public Optional<User> findByPhone(String phone) {
    QueryWrapper query = QueryWrapper.create().eq("phone", phone);
    UserDO userDO = userDOMapper.selectOneByQuery(query);
    return Optional.ofNullable(userDO).map(userConverter::toEntity);
  }

  @Override
  public List<User> findByDeptId(String deptId) {
    QueryWrapper query = QueryWrapper.create().eq("dept_id", deptId);
    List<UserDO> userDOs = userDOMapper.selectListByQuery(query);
    return userDOs.stream().map(userConverter::toEntity).collect(Collectors.toList());
  }

  @Override
  public List<User> findByRoleId(String roleId) {
    // TODO: 实现根据角色ID查询用户（需要关联 user_role 表）
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public org.springframework.data.domain.Page<User> findAll(Pageable pageable) {
    Page<UserDO> page =
        userDOMapper.paginate(
            Page.of(pageable.getPageNumber() + 1, pageable.getPageSize()), QueryWrapper.create());

    List<User> users =
        page.getRecords().stream().map(userConverter::toEntity).collect(Collectors.toList());

    return new PageImpl<>(users, pageable, page.getTotalRow());
  }

  @Override
  public org.springframework.data.domain.Page<User> search(String keyword, Pageable pageable) {
    QueryWrapper query =
        QueryWrapper.create()
            .where(
                "username LIKE ? OR nickname LIKE ? OR real_name LIKE ?",
                "%" + keyword + "%",
                "%" + keyword + "%",
                "%" + keyword + "%");

    Page<UserDO> page =
        userDOMapper.paginate(Page.of(pageable.getPageNumber() + 1, pageable.getPageSize()), query);

    List<User> users =
        page.getRecords().stream().map(userConverter::toEntity).collect(Collectors.toList());

    return new PageImpl<>(users, pageable, page.getTotalRow());
  }

  @Override
  public boolean existsByUsername(String username) {
    QueryWrapper query = QueryWrapper.create().where(USER_DO.USERNAME.eq(username));
    return userDOMapper.selectCountByQuery(query) > 0;
  }

  @Override
  public boolean existsByEmail(String email) {
    QueryWrapper query = QueryWrapper.create().where(USER_DO.EMAIL.eq(email));
    return userDOMapper.selectCountByQuery(query) > 0;
  }

  @Override
  public boolean existsByPhone(String phone) {
    QueryWrapper query = QueryWrapper.create().where(USER_DO.PHONE.eq(phone));
    return userDOMapper.selectCountByQuery(query) > 0;
  }

  @Override
  public long countByDeptId(String deptId) {
    QueryWrapper query = QueryWrapper.create().where(USER_DO.DEPT_ID.eq(deptId));
    return userDOMapper.selectCountByQuery(query);
  }
}
