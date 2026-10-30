package io.github.loadup.modules.upms.infrastructure.repository;

import io.github.loadup.components.database.tenant.TenantContext;
import io.github.loadup.modules.upms.infrastructure.dataobject.LoginLogDO;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.ListCrudRepository;

/** Spring Data JDBC reads for login history. Tenant identity always comes from the execution context. */
public interface LoginLogRepository extends ListCrudRepository<LoginLogDO, String> {

    @Override
    default Optional<LoginLogDO> findById(String id) {
        return findByTenantIdAndIdAndDeletedFalse(TenantContext.tenantId(), id);
    }

    default Page<LoginLogDO> findAll(Pageable pageable) {
        return findByTenantIdAndDeletedFalse(TenantContext.tenantId(), pageable);
    }

    default Page<LoginLogDO> findByUserId(String userId, Pageable pageable) {
        return findByTenantIdAndUserIdAndDeletedFalse(TenantContext.tenantId(), userId, pageable);
    }

    default Page<LoginLogDO> findByUsername(String username, Pageable pageable) {
        return findByTenantIdAndUsernameAndDeletedFalse(TenantContext.tenantId(), username, pageable);
    }

    default Page<LoginLogDO> findByDateRange(LocalDateTime start, LocalDateTime end, Pageable pageable) {
        return findByTenantIdAndLoginTimeBetweenAndDeletedFalse(TenantContext.tenantId(), start, end, pageable);
    }

    default Page<LoginLogDO> findFailedLogins(LocalDateTime start, LocalDateTime end, Pageable pageable) {
        return findByTenantIdAndLoginStatusAndLoginTimeBetweenAndDeletedFalse(
                TenantContext.tenantId(), (short) 0, start, end, pageable);
    }

    default List<LoginLogDO> findByLoginTimeBetween(LocalDateTime start, LocalDateTime end) {
        return findByTenantIdAndLoginTimeBetweenAndDeletedFalse(TenantContext.tenantId(), start, end);
    }

    default List<LoginLogDO> findByUserId(String userId) {
        return findByTenantIdAndUserIdAndDeletedFalse(TenantContext.tenantId(), userId);
    }

    default Optional<LoginLogDO> findLastSuccessfulLogin(String userId) {
        return findFirstByTenantIdAndUserIdAndLoginStatusAndDeletedFalseOrderByLoginTimeDesc(
                TenantContext.tenantId(), userId, (short) 1);
    }

    default long countLoginAttempts(String userId, LocalDateTime start, LocalDateTime end) {
        return countByTenantIdAndUserIdAndLoginTimeBetweenAndDeletedFalse(TenantContext.tenantId(), userId, start, end);
    }

    default long countFailedLoginAttempts(String userId, LocalDateTime start, LocalDateTime end) {
        return countByTenantIdAndUserIdAndLoginStatusAndLoginTimeBetweenAndDeletedFalse(
                TenantContext.tenantId(), userId, (short) 0, start, end);
    }

    Optional<LoginLogDO> findByTenantIdAndIdAndDeletedFalse(String tenantId, String id);

    Page<LoginLogDO> findByTenantIdAndDeletedFalse(String tenantId, Pageable pageable);

    Page<LoginLogDO> findByTenantIdAndUserIdAndDeletedFalse(String tenantId, String userId, Pageable pageable);

    Page<LoginLogDO> findByTenantIdAndUsernameAndDeletedFalse(String tenantId, String username, Pageable pageable);

    Page<LoginLogDO> findByTenantIdAndLoginTimeBetweenAndDeletedFalse(
            String tenantId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    Page<LoginLogDO> findByTenantIdAndLoginStatusAndLoginTimeBetweenAndDeletedFalse(
            String tenantId, Short loginStatus, LocalDateTime start, LocalDateTime end, Pageable pageable);

    List<LoginLogDO> findByTenantIdAndLoginTimeBetweenAndDeletedFalse(
            String tenantId, LocalDateTime start, LocalDateTime end);

    List<LoginLogDO> findByTenantIdAndUserIdAndDeletedFalse(String tenantId, String userId);

    Optional<LoginLogDO> findFirstByTenantIdAndUserIdAndLoginStatusAndDeletedFalseOrderByLoginTimeDesc(
            String tenantId, String userId, Short loginStatus);

    long countByTenantIdAndUserIdAndLoginTimeBetweenAndDeletedFalse(
            String tenantId, String userId, LocalDateTime start, LocalDateTime end);

    long countByTenantIdAndUserIdAndLoginStatusAndLoginTimeBetweenAndDeletedFalse(
            String tenantId, String userId, Short loginStatus, LocalDateTime start, LocalDateTime end);
}
