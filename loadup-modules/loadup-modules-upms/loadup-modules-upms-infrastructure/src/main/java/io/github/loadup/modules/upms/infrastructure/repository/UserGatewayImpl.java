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

import static io.github.loadup.modules.upms.infrastructure.dataobject.table.Tables.USER_DO;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import io.github.loadup.commons.dto.PageQuery;
import io.github.loadup.commons.result.PageDTO;
import io.github.loadup.modules.upms.domain.entity.User;
import io.github.loadup.modules.upms.domain.gateway.UserGateway;
import io.github.loadup.modules.upms.infrastructure.converter.UserConverter;
import io.github.loadup.modules.upms.infrastructure.dataobject.UserDO;
import io.github.loadup.modules.upms.infrastructure.mapper.UserDOMapper;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

/**
 * User Repository Implementation using MyBatis-Flex.
 *
 * <p>{@link AuthGateway} is implemented separately in {@link AuthGatewayImpl}
 * to respect the Interface Segregation Principle.
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Repository
public class UserGatewayImpl implements UserGateway {

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
        QueryWrapper query = QueryWrapper.create().where(USER_DO.USERNAME.eq(username));
        UserDO userDO = userDOMapper.selectOneByQuery(query);
        return Optional.ofNullable(userDO).map(userConverter::toEntity);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        QueryWrapper query = QueryWrapper.create().where(USER_DO.EMAIL.eq(email));
        UserDO userDO = userDOMapper.selectOneByQuery(query);
        return Optional.ofNullable(userDO).map(userConverter::toEntity);
    }

    @Override
    public Optional<User> findByMobile(String mobile) {
        QueryWrapper query = QueryWrapper.create().where(USER_DO.MOBILE.eq(mobile));
        UserDO userDO = userDOMapper.selectOneByQuery(query);
        return Optional.ofNullable(userDO).map(userConverter::toEntity);
    }

    @Override
    public List<User> findByDeptId(String deptId) {
        QueryWrapper query = QueryWrapper.create().where(USER_DO.DEPT_ID.eq(deptId));
        List<UserDO> userDOs = userDOMapper.selectListByQuery(query);
        return userDOs.stream().map(userConverter::toEntity).collect(Collectors.toList());
    }

    @Override
    @Deprecated
    public List<User> findByRoleId(String roleId) {
        // TODO: Implement role-based user lookup (requires user_role table join)
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public PageDTO<User> findAll(PageQuery query) {
        Page<UserDO> page = userDOMapper.paginate(Page.of(query.pageNum(), query.pageSize()), QueryWrapper.create());

        List<User> users =
                page.getRecords().stream().map(userConverter::toEntity).collect(Collectors.toList());

        return PageDTO.of(users, page.getTotalRow(), query.pageNum(), query.pageSize());
    }

    @Override
    public PageDTO<User> search(String keyword, PageQuery query) {
        QueryWrapper search = QueryWrapper.create()
                .where(
                        "username LIKE ? OR nickname LIKE ? OR real_name LIKE ?",
                        "%" + keyword + "%",
                        "%" + keyword + "%",
                        "%" + keyword + "%");

        Page<UserDO> page = userDOMapper.paginate(Page.of(query.pageNum(), query.pageSize()), search);

        List<User> users =
                page.getRecords().stream().map(userConverter::toEntity).collect(Collectors.toList());

        return PageDTO.of(users, page.getTotalRow(), query.pageNum(), query.pageSize());
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
    public boolean existsByMobile(String mobile) {
        QueryWrapper query = QueryWrapper.create().where(USER_DO.MOBILE.eq(mobile));
        return userDOMapper.selectCountByQuery(query) > 0;
    }

    @Override
    public long countByDeptId(String deptId) {
        QueryWrapper query = QueryWrapper.create().where(USER_DO.DEPT_ID.eq(deptId));
        return userDOMapper.selectCountByQuery(query);
    }

    public UserGatewayImpl(UserDOMapper userDOMapper, UserConverter userConverter) {
        this.userDOMapper = userDOMapper;
        this.userConverter = userConverter;
    }
}
