package io.github.loadup.retrytask.test.integration;

import io.github.loadup.retrytask.facade.enums.Priority;
import io.github.loadup.retrytask.facade.enums.RetryTaskStatus;
import io.github.loadup.retrytask.facade.model.RetryTask;
import io.github.loadup.retrytask.infra.repository.RetryTaskRepository;
import io.github.loadup.retrytask.test.BaseRetryTaskTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class JdbcRetryTaskRepositoryIntegrationTest extends BaseRetryTaskTest {

    @Autowired
    private RetryTaskRepository repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanUp() {
        // Since we are @Transactional, rollback happens automatically, but good to be cleaner if needed.
    }

    @Test
    @DisplayName("Should save and find task")
    void testSaveAndFind() {
        RetryTask task = createTask("TEST_TYPE", "TEST_ID");
        RetryTask saved = repository.save(task);

        assertThat(saved.getId()).isNotNull();

        // Find by ID
        assertThat(repository.findById(saved.getId())).isPresent();

        // Find by BizType/BizId
        assertThat(repository.findByBizTypeAndBizId("TEST_TYPE", "TEST_ID")).isPresent();
    }

    @Test
    @DisplayName("Should find pending tasks due for execution")
    void testFindTasksToRetry() {
        // Task 1: Pending, due
        RetryTask t1 = createTask("TYPE_A", "ID_1");
        t1.setNextRetryTime(LocalDateTime.now().minusMinutes(1));
        repository.save(t1);

        // Task 2: Pending, future (not due)
        RetryTask t2 = createTask("TYPE_A", "ID_2");
        t2.setNextRetryTime(LocalDateTime.now().plusMinutes(10));
        repository.save(t2);

        // Task 3: Running (not pending)
        RetryTask t3 = createTask("TYPE_A", "ID_3");
        t3.setStatus(RetryTaskStatus.RUNNING);
        t3.setNextRetryTime(LocalDateTime.now().minusMinutes(1));
        repository.save(t3);

        List<RetryTask> tasks = repository.findTasksToRetry(LocalDateTime.now(), 10);

        assertThat(tasks).hasSize(1);
        assertThat(tasks.get(0).getBizId()).isEqualTo("ID_1");
    }

    @Test
    @DisplayName("Should tryLock successfully only once")
    void testTryLock() {
        RetryTask task = createTask("LOCK_TEST", "LOCK_ID");
        task = repository.save(task);

        // First lock should succeed
        boolean lock1 = repository.tryLock(task.getId());
        assertThat(lock1).isTrue();

        // Second lock should fail (status is now RUNNING)
        boolean lock2 = repository.tryLock(task.getId());
        assertThat(lock2).isFalse();
    }

    @Test
    @DisplayName("Should reset stuck tasks")
    void testResetStuckTasks() {
        // Stuck task: Running, updated long ago
        RetryTask stuck = createTask("STUCK", "ID_STUCK");
        stuck.setStatus(RetryTaskStatus.RUNNING);
        stuck = repository.save(stuck);

        // Force update_time in DB because save() updates it to NOW()
        jdbcTemplate.update("UPDATE retry_task SET update_time = ? WHERE id = ?", LocalDateTime.now().minusMinutes(10), stuck.getId());

        // Active task: Running, updated recently
        RetryTask active = createTask("ACTIVE", "ID_ACTIVE");
        active.setStatus(RetryTaskStatus.RUNNING);
        repository.save(active);

        int count = repository.resetStuckTasks(LocalDateTime.now().minusMinutes(5));

        assertThat(count).isEqualTo(1);

        // Verify stuck task is now PENDING
        RetryTask updatedStuck = repository.findById(stuck.getId()).get();
        assertThat(updatedStuck.getStatus()).isEqualTo(RetryTaskStatus.PENDING);
    }

    @Test
    @DisplayName("Should find tasks by biz type")
    void testFindTasksByBizType() {
        RetryTask t1 = createTask("TYPE_A", "ID_1");
        t1.setNextRetryTime(LocalDateTime.now().minusMinutes(1));
        repository.save(t1);

        RetryTask t2 = createTask("TYPE_B", "ID_2");
        t2.setNextRetryTime(LocalDateTime.now().minusMinutes(1));
        repository.save(t2);

        List<RetryTask> typeATasks = repository.findTasksToRetryByBizType("TYPE_A", LocalDateTime.now(), 10);
        assertThat(typeATasks).hasSize(1);
        assertThat(typeATasks.get(0).getBizType()).isEqualTo("TYPE_A");
    }

    private RetryTask createTask(String bizType, String bizId) {
        RetryTask task = new RetryTask();
        task.setBizType(bizType);
        task.setBizId(bizId);
        task.setStatus(RetryTaskStatus.PENDING);
        task.setPriority(Priority.LOW);
        task.setNextRetryTime(LocalDateTime.now());
        task.setRetryCount(0);
        task.setMaxRetryCount(3);
        task.setCreateTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());
        return task;
    }
}

