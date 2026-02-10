package io.github.loadup.retrytask.test.facade;

import io.github.loadup.retrytask.facade.RetryTaskFacade;
import io.github.loadup.retrytask.facade.request.RetryTaskRegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RetryTaskFacade
 */
@ExtendWith(MockitoExtension.class)
class RetryTaskFacadeTest {

    @Mock
    private RetryTaskFacade retryTaskFacade;

    private RetryTaskRegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new RetryTaskRegisterRequest();
        registerRequest.setBizType("ORDER");
        registerRequest.setBizId("12345");
    }

    @Test
    @DisplayName("Should register retry task successfully")
    void testRegisterRetryTask() {
        // Given
        Long expectedTaskId = 1L;
        when(retryTaskFacade.register(any(RetryTaskRegisterRequest.class))).thenReturn(expectedTaskId);

        // When
        Long taskId = retryTaskFacade.register(registerRequest);

        // Then
        assertNotNull(taskId);
        assertEquals(expectedTaskId, taskId);
        verify(retryTaskFacade, times(1)).register(registerRequest);
    }

    @Test
    @DisplayName("Should delete retry task successfully")
    void testDeleteRetryTask() {
        // Given
        String bizType = "ORDER";
        String bizId = "12345";

        // When
        retryTaskFacade.delete(bizType, bizId);

        // Then
        verify(retryTaskFacade, times(1)).delete(bizType, bizId);
    }

    @Test
    @DisplayName("Should reset retry task successfully")
    void testResetRetryTask() {
        // Given
        String bizType = "ORDER";
        String bizId = "12345";

        // When
        retryTaskFacade.reset(bizType, bizId);

        // Then
        verify(retryTaskFacade, times(1)).reset(bizType, bizId);
    }

    @Test
    @DisplayName("Should throw exception when registering with null request")
    void testRegisterWithNullRequest() {
        // Given
        when(retryTaskFacade.register(null)).thenThrow(new IllegalArgumentException("Request cannot be null"));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> retryTaskFacade.register(null));
    }
}
