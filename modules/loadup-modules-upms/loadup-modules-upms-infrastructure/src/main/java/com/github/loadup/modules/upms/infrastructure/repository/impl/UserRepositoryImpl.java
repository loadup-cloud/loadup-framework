package com.github.loadup.modules.upms.infrastructure.repository.impl;

import com.github.loadup.modules.upms.domain.entity.User;
import com.github.loadup.modules.upms.domain.repository.UserRepository;
import com.github.loadup.modules.upms.infrastructure.repository.jdbc.JdbcUserRepository;
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
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

  private final JdbcUserRepository jdbcRepository;

  @Override
  public User save(User user) {
    return jdbcRepository.save(user);
  }

  @Override
  public User update(User user) {
    return jdbcRepository.save(user);
  }

  @Override
  public void deleteById(Long id) {
    // Use soft delete
    jdbcRepository.softDelete(id, 1L); // TODO: get actual operator ID
  }

  @Override
  public Optional<User> findById(Long id) {
    return jdbcRepository.findById(id);
  }

  @Override
  public Optional<User> findByUsername(String username) {
    return jdbcRepository.findByUsername(username);
  }

  @Override
  public Optional<User> findByEmail(String email) {
    return jdbcRepository.findByEmail(email);
  }

  @Override
  public Optional<User> findByPhone(String phone) {
    return jdbcRepository.findByPhone(phone);
  }

  @Override
  public List<User> findByDeptId(Long deptId) {
    return jdbcRepository.findByDeptId(deptId);
  }

  @Override
  public List<User> findByRoleId(Long roleId) {
    return jdbcRepository.findByRoleId(roleId);
  }

  @Override
  public Page<User> findAll(Pageable pageable) {
    return jdbcRepository.findAll(pageable);
  }

  @Override
  public Page<User> search(String keyword, Pageable pageable) {
    List<User> users = jdbcRepository.searchByKeyword(keyword);
    int start = (int) pageable.getOffset();
    int end = Math.min((start + pageable.getPageSize()), users.size());
    return new PageImpl<>(users.subList(start, end), pageable, users.size());
  }

  @Override
  public boolean existsByUsername(String username) {
    return jdbcRepository.countByUsername(username) > 0;
  }

  @Override
  public boolean existsByEmail(String email) {
    return jdbcRepository.countByEmail(email) > 0;
  }

  @Override
  public boolean existsByPhone(String phone) {
    return jdbcRepository.countByPhone(phone) > 0;
  }

  @Override
  public long countByDeptId(Long deptId) {
    return jdbcRepository.countByDeptId(deptId);
  }
}
