package com.github.loadup.modules.upms.infrastructure.repository.impl;

import com.github.loadup.modules.upms.domain.entity.User;
import com.github.loadup.modules.upms.domain.repository.UserRepository;
import com.github.loadup.modules.upms.infrastructure.dataobject.UserDO;
import com.github.loadup.modules.upms.infrastructure.mapper.UserMapper;
import com.github.loadup.modules.upms.infrastructure.repository.jdbc.UserJdbcRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

/**
 * User Repository Implementation
 *
 * <p>Uses UserDO for database operations and maps to/from User entity
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

  private final UserJdbcRepository jdbcRepository;
  private final UserMapper userMapper;

  @Override
  public User save(User user) {
    UserDO userDO = userMapper.toDO(user);
    UserDO saved = jdbcRepository.save(userDO);
    return userMapper.toEntity(saved);
  }

  @Override
  public User update(User user) {
    UserDO userDO = userMapper.toDO(user);
    UserDO updated = jdbcRepository.save(userDO);
    return userMapper.toEntity(updated);
  }

  @Override
  public void deleteById(String id) {
    jdbcRepository
        .findById(id)
        .ifPresent(
            userDO -> {
              userDO.setDeleted(true);
              jdbcRepository.save(userDO);
            });
  }

  @Override
  public Optional<User> findById(String id) {
    return jdbcRepository.findById(id).map(userMapper::toEntity);
  }

  @Override
  public Optional<User> findByUsername(String username) {
    return jdbcRepository.findByUsername(username).map(userMapper::toEntity);
  }

  @Override
  public Optional<User> findByEmail(String email) {
    return jdbcRepository.findByEmail(email).map(userMapper::toEntity);
  }

  @Override
  public Optional<User> findByPhone(String phone) {
    return jdbcRepository.findByPhone(phone).map(userMapper::toEntity);
  }

  @Override
  public List<User> findByDeptId(String deptId) {
    List<UserDO> userDOList = jdbcRepository.findByDeptId(deptId);
    return userMapper.toEntityList(userDOList);
  }

  @Override
  public List<User> findByRoleId(String roleId) {
    List<UserDO> userDOList = jdbcRepository.findByRoleId(roleId);
    return userMapper.toEntityList(userDOList);
  }

  @Override
  public boolean existsByUsername(String username) {
    return jdbcRepository.existsByUsername(username);
  }

  @Override
  public boolean existsByEmail(String email) {
    return jdbcRepository.existsByEmail(email);
  }

  @Override
  public boolean existsByPhone(String phone) {
    return jdbcRepository.existsByPhone(phone);
  }

  @Override
  public Page<User> findAll(Pageable pageable) {
    Page<UserDO> userDOPage = jdbcRepository.findAll(pageable);
    List<User> users = userMapper.toEntityList(userDOPage.getContent());
    return new PageImpl<>(users, pageable, userDOPage.getTotalElements());
  }

  @Override
  public long countByDeptId(String deptId) {
    return jdbcRepository.countByDeptId(deptId);
  }

  @Override
  public Page<User> search(String keyword, Pageable pageable) {
    if (keyword == null || keyword.trim().isEmpty()) {
      return findAll(pageable);
    }

    Page<UserDO> userDOPage = jdbcRepository.searchByKeyword(keyword, pageable);
    List<User> users = userMapper.toEntityList(userDOPage.getContent());
    return new PageImpl<>(users, pageable, userDOPage.getTotalElements());
  }
}
