package io.github.loadup.retrytask.test.core;

import io.github.loadup.retrytask.core.RetryTaskExecutor;
import io.github.loadup.retrytask.core.RetryTaskProcessor;
import io.github.loadup.retrytask.core.RetryTaskProcessorRegistry;
import io.github.loadup.retrytask.core.RetryTaskService;
import io.github.loadup.retrytask.facade.model.RetryTask;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.Executor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RetryTaskExecutorTest {

    @Mock
    private RetryTaskProcessorRegistry processorRegistry;

    @Mock
    private RetryTaskService retryTaskService;

    @Mock
    private Executor executor;

    @InjectMocks
    private RetryTaskExecutor retryTaskExecutor;

    @Test
    void executeSyncShouldProcessTaskIfLocked() {
        RetryTask task = new RetryTask();
        task.setId(1L);
        task.setBizType("TEST");

        when(retryTaskService.tryLock(1L)).thenReturn(true);

        RetryTaskProcessor processor = mock(RetryTaskProcessor.class);
        when(processor.process(task)).thenReturn(true);
        when(processorRegistry.getProcessor("TEST")).thenReturn(processor);

        retryTaskExecutor.executeSync(task);

        verify(processor).process(task);
        verify(retryTaskService).markSuccess(1L);
    }

    @Test
    void executeSyncShouldSkipIfLockFailed() {
        RetryTask task = new RetryTask();
        task.setId(1L);

        when(retryTaskService.tryLock(1L)).thenReturn(false);

        retryTaskExecutor.executeSync(task);

        verifyNoInteractions(processorRegistry);
    }

    @Test
    void executeSyncShouldMarkFailureIfProcessorFails() {
        RetryTask task = new RetryTask();
        task.setId(1L);
        task.setBizType("TEST");

        when(retryTaskService.tryLock(1L)).thenReturn(true);

        RetryTaskProcessor processor = mock(RetryTaskProcessor.class);
        when(processor.process(task)).thenReturn(false);
        when(processorRegistry.getProcessor("TEST")).thenReturn(processor);

        retryTaskExecutor.executeSync(task);

        verify(retryTaskService).markFailure(eq(1L), anyString());
    }

    @Test
    void executeSyncShouldMarkFailureIfProcessorThrows() {
        RetryTask task = new RetryTask();
        task.setId(1L);
        task.setBizType("TEST");

        when(retryTaskService.tryLock(1L)).thenReturn(true);

        RetryTaskProcessor processor = mock(RetryTaskProcessor.class);
        when(processor.process(task)).thenThrow(new RuntimeException("Boom"));
        when(processorRegistry.getProcessor("TEST")).thenReturn(processor);

        retryTaskExecutor.executeSync(task);

        verify(retryTaskService).markFailure(eq(1L), contains("Boom"));
    }

    @Test
    void executeAsyncShouldSubmitToExecutor() {
        RetryTask task = new RetryTask();
        retryTaskExecutor.executeAsync(task);
        verify(executor).execute(any(Runnable.class));
    }
}

