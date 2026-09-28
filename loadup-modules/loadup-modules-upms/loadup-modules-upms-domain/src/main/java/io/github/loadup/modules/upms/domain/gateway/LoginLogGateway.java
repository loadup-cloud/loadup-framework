package io.github.loadup.modules.upms.domain.gateway;

import io.github.loadup.commons.domain.PageResult;
import io.github.loadup.commons.dto.PageQuery;
import io.github.loadup.modules.upms.domain.entity.LoginLog;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Login Log Repository Interface
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
public interface LoginLogGateway {

    /**
     * Save login log
     */
    LoginLog save(LoginLog log);

    /**
     * Find log by ID
     */
    Optional<LoginLog> findById(String id);

    /**
     * Find logs by user ID
     */
    PageResult<LoginLog> findByUserId(String userId, PageQuery query);

    /**
     * Find logs by username
     */
    PageResult<LoginLog> findByUsername(String username, PageQuery query);

    /**
     * Find logs by date range
     */
    PageResult<LoginLog> findByDateRange(LocalDateTime startTime, LocalDateTime endTime, PageQuery query);

    /**
     * Find failed login attempts
     */
    PageResult<LoginLog> findFailedLogins(LocalDateTime startTime, LocalDateTime endTime, PageQuery query);

    /**
     * Delete logs before specified date
     */
    void deleteBeforeDate(LocalDateTime date);

    List<LoginLog> findByLoginTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

    PageResult<LoginLog> findAll(PageQuery query);

    /**
     * Count login attempts by user and time range
     */
    long countLoginAttempts(String userId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * Count failed login attempts by user and time range
     */
    long countFailedLoginAttempts(String userId, LocalDateTime startTime, LocalDateTime endTime);

    List<LoginLog> findByUserId(String userId);

    /**
     * Get last successful login
     */
    Optional<LoginLog> findLastSuccessfulLogin(String userId);
}
