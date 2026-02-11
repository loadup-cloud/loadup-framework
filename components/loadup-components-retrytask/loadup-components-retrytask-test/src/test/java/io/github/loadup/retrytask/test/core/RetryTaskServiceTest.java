package io.github.loadup.retrytask.test.core;

import io.github.loadup.retrytask.core.RetryTaskExecutor;
import io.github.loadup.retrytask.core.RetryTaskServiceImpl;
import io.github.loadup.retrytask.core.config.RetryTaskProperties;
import io.github.loadup.retrytask.facade.model.RetryTask;
import io.github.loadup.retrytask.facade.enums.RetryTaskStatus;
import io.github.loadup.retrytask.facade.request.RetryTaskRegisterRequest;
import io.github.loadup.retrytask.infra.repository.RetryTaskRepository;
import io.github.loadup.retrytask.notify.RetryTaskNotifierRegistry;
import io.github.loadup.retrytask.strategy.RetryStrategy;
import io.github.loadup.retrytask.strategy.RetryStrategyRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RetryTaskServiceTest {

    @Mock
    private RetryTaskRepository retryTaskRepository;
    @Mock
    private RetryTaskNotifierRegistry notifierRegistry;
    @Mock
    private RetryStrategyRegistry strategyRegistry;
    @Mock
    private RetryTaskProperties properties;
    @Mock
    private RetryTaskExecutor retryTaskExecutor;
    @Mock
    private RetryStrategy retryStrategy;

    @InjectMocks
    private RetryTaskServiceImpl service;

    @BeforeEach
    void setUp() {
        lenient().when(properties.getBizTypes()).thenReturn(Collections.emptyMap());
        lenient().when(strategyRegistry.getStrategy(any())).thenReturn(retryStrategy);
        lenient().when(retryStrategy.nextRetryTime(any())).thenReturn(LocalDateTime.now().plusSeconds(60));
    }

    @Test
    void registerShouldSaveTask() {
        RetryTaskRegisterRequest request = new RetryTaskRegisterRequest();
        request.setBizType("TEST");
        request.setBizId("123");

        when(retryTaskRepository.save(any(RetryTask.class))).thenAnswer(inv -> {
            RetryTask task = inv.getArgument(0);
            task.setId(1L);
            return task;
        });

        Long taskId = service.register(request);

        assertThat(taskId).isEqualTo(1L);
        verify(retryTaskRepository).save(any(RetryTask.class));
    }

    @Test
    void markSuccessShouldDeleteTask() {
        service.markSuccess(1L);
        verify(retryTaskRepository).deleteById(1L);
    }

    @Test
    void markFailureShouldIncrementCountAndReschedule() {
        RetryTask task = new RetryTask();
        task.setId(1L);
        task.setBizType("TEST");
        task.setRetryCount(0);
        task.setMaxRetryCount(3);

        when(retryTaskRepository.findById(1L)).thenReturn(Optional.of(task));

        service.markFailure(1L, "Error");

        verify(retryTaskRepository).save(task);
        assertThat(task.getRetryCount()).isEqualTo(1);
        assertThat(task.getLastFailureReason()).isEqualTo("Error");
        assertThat(task.getStatus()).isNotEqualTo(RetryTaskStatus.FAILURE);
    }

    @Test
    void markFailureShouldMarkFailedWhenMaxRetriesReached() {
        RetryTask task = new RetryTask();
        task.setId(1L);
        task.setBizType("TEST");
        task.setRetryCount(9);
        task.setMaxRetryCount(10); // Matches default properties config (10)

        when(retryTaskRepository.findById(1L)).thenReturn(Optional.of(task));

        service.markFailure(1L, "Final Error");

        // After this call, count will be 10, which is >= maxRetryCount (10) -> FAILURE

        assertThat(task.getStatus()).isEqualTo(RetryTaskStatus.FAILURE);
        verify(retryTaskRepository).save(task);
    }
}

