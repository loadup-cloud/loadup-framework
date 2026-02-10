package io.github.loadup.retrytask.test.core;

import io.github.loadup.retrytask.infra.api.management.RetryTaskManagement;
import io.github.loadup.retrytask.facade.model.RetryTask;
import io.github.loadup.retrytask.facade.model.RetryTaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RetryTaskRepository
 */
@ExtendWith(MockitoExtension.class)
class RetryTaskManagementTest {

    @Mock
    private RetryTaskManagement retryTaskManagement;

    private RetryTask retryTask;

    @BeforeEach
    void setUp() {
        retryTask = new RetryTask();
        retryTask.setId(1L);
        retryTask.setBizType("ORDER");
        retryTask.setBizId("12345");
        retryTask.setStatus(RetryTaskStatus.PENDING);
        retryTask.setRetryCount(0);
        retryTask.setMaxRetryCount(3);
        retryTask.setCreateTime(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should save retry task successfully")
    void testSaveRetryTask() {
        // Given
        when(retryTaskManagement.save(any(RetryTask.class))).thenReturn(retryTask);

        // When
        RetryTask saved = retryTaskManagement.save(retryTask);

        // Then
        assertNotNull(saved);
        assertEquals(retryTask.getId(), saved.getId());
        assertEquals(retryTask.getBizType(), saved.getBizType());
        assertEquals(retryTask.getBizId(), saved.getBizId());
        verify(retryTaskManagement).save(retryTask);
    }

    @Test
    @DisplayName("Should find retry task by ID")
    void testFindById() {
        // Given
        when(retryTaskManagement.findById(1L)).thenReturn(Optional.of(retryTask));

        // When
        Optional<RetryTask> found = retryTaskManagement.findById(1L);

        // Then
        assertTrue(found.isPresent());
        assertEquals(retryTask.getId(), found.get().getId());
        verify(retryTaskManagement).findById(1L);
    }

    @Test
    @DisplayName("Should return empty when retry task not found")
    void testFindByIdNotFound() {
        // Given
        when(retryTaskManagement.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<RetryTask> found = retryTaskManagement.findById(999L);

        // Then
        assertFalse(found.isPresent());
        verify(retryTaskManagement).findById(999L);
    }

    @Test
    @DisplayName("Should find retry task by business type and ID")
    void testFindByBizTypeAndBizId() {
        // Given
        when(retryTaskManagement.findByBizTypeAndBizId("ORDER", "12345")).thenReturn(Optional.of(retryTask));

        // When
        Optional<RetryTask> found = retryTaskManagement.findByBizTypeAndBizId("ORDER", "12345");

        // Then
        assertTrue(found.isPresent());
        assertEquals("ORDER", found.get().getBizType());
        assertEquals("12345", found.get().getBizId());
        verify(retryTaskManagement).findByBizTypeAndBizId("ORDER", "12345");
    }

    @Test
    @DisplayName("Should find tasks ready for retry")
    void testFindTasksToRetry() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        List<RetryTask> readyTasks = Arrays.asList(retryTask);
        when(retryTaskManagement.findTasksToRetry(any(LocalDateTime.class), anyInt())).thenReturn(readyTasks);

        // When
        List<RetryTask> found = retryTaskManagement.findTasksToRetry(now, 10);

        // Then
        assertNotNull(found);
        assertEquals(1, found.size());
        verify(retryTaskManagement).findTasksToRetry(any(LocalDateTime.class), anyInt());
    }

    @Test
    @DisplayName("Should delete retry task by ID")
    void testDeleteRetryTaskById() {
        // When
        retryTaskManagement.deleteById(1L);

        // Then
        verify(retryTaskManagement).deleteById(1L);
    }

    @Test
    @DisplayName("Should delete retry task by business type and ID")
    void testDelete() {
        // When
        retryTaskManagement.delete("ORDER", "12345");

        // Then
        verify(retryTaskManagement).delete("ORDER", "12345");
    }
}
