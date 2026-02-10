package io.github.loadup.retrytask.infra.mysql.repository;

import io.github.loadup.retrytask.facade.model.RetryTaskStatus;
import io.github.loadup.retrytask.infra.mysql.model.RetryTaskDO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JDBC repository for {@link RetryTaskDO}.
 */
@Repository
public interface RetryTaskDORepository extends CrudRepository<RetryTaskDO, Long> {

    Optional<RetryTaskDO> findByBizTypeAndBizId(String bizType, String bizId);

    @Modifying
    @Query("DELETE FROM retry_task WHERE biz_type = :bizType AND biz_id = :bizId")
    void delete(@Param("bizType") String bizType, @Param("bizId") String bizId);

    @Query("SELECT * FROM retry_task WHERE next_retry_time < :nextRetryTime AND status = :status ORDER BY priority DESC, next_retry_time ASC")
    List<RetryTaskDO> findTasksToRetry(@Param("nextRetryTime") LocalDateTime nextRetryTime, @Param("status") RetryTaskStatus status, Pageable pageable);
}
