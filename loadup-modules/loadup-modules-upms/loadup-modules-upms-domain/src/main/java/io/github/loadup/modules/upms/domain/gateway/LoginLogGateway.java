package io.github.loadup.modules.upms.domain.gateway;

/*-
 * #%L
 * Loadup Modules UPMS Domain Layer
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
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

import io.github.loadup.modules.upms.domain.entity.LoginLog;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Login Log Repository Interface
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
public interface LoginLogGateway {

  /** Save login log */
  LoginLog save(LoginLog log);

  /** Find log by ID */
  Optional<LoginLog> findById(String id);

  /** Find logs by user ID */
  Page<LoginLog> findByUserId(String userId, Pageable pageable);

  /** Find logs by username */
  Page<LoginLog> findByUsername(String username, Pageable pageable);

  /** Find logs by date range */
  Page<LoginLog> findByDateRange(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

  /** Find failed login attempts */
  Page<LoginLog> findFailedLogins(
      LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

  /** Delete logs before specified date */
  void deleteBeforeDate(LocalDateTime date);

  List<LoginLog> findByLoginTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

  Page<LoginLog> findAll(Pageable pageable);

  /** Count login attempts by user and time range */
  long countLoginAttempts(String userId, LocalDateTime startTime, LocalDateTime endTime);

  /** Count failed login attempts by user and time range */
  long countFailedLoginAttempts(String userId, LocalDateTime startTime, LocalDateTime endTime);

  List<LoginLog> findByUserId(String userId);

  /** Get last successful login */
  Optional<LoginLog> findLastSuccessfulLogin(String userId);
}
