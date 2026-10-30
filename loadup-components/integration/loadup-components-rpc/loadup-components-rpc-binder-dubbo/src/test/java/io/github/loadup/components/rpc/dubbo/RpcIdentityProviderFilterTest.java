package io.github.loadup.components.rpc.dubbo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import io.github.loadup.commons.enums.CommonResultCodeEnum;
import io.github.loadup.commons.error.CommonException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

class RpcIdentityProviderFilterTest {
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    private final RpcIdentityProviderFilter filter = new RpcIdentityProviderFilter();
    private final Invoker<?> invoker = mock(Invoker.class);
    private final Invocation invocation = mock(Invocation.class);

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void missingCredentialNeverReachesTheBusinessMethod() {
        when(invocation.getAttachment(RpcTokenContext.AUTHORIZATION)).thenReturn(null);

        var failure = assertThrows(RpcException.class, () -> filter.invoke(invoker, invocation));

        assertEquals(RpcException.AUTHORIZATION_EXCEPTION, failure.getCode());
        verifyNoInteractions(invoker);
    }

    @Test
    void expiredCredentialIsRejectedAsUnauthenticated() {
        when(invocation.getAttachment(RpcTokenContext.AUTHORIZATION))
                .thenReturn("Bearer " + token(Instant.now().minusSeconds(1)));

        var failure = assertThrows(RpcException.class, () -> filter.invoke(invoker, invocation));

        assertEquals(RpcException.AUTHORIZATION_EXCEPTION, failure.getCode());
        verifyNoInteractions(invoker);
    }

    @Test
    void bindsTheCallerIdentityAndRestoresThePreviousContext() {
        when(invocation.getAttachment(RpcTokenContext.AUTHORIZATION))
                .thenReturn("Bearer " + token(Instant.now().plusSeconds(300)));
        when(invocation.getArguments()).thenReturn(new Object[0]);
        when(invoker.invoke(invocation)).thenAnswer(call -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            assertNotNull(authentication);
            Jwt jwt = assertInstanceOf(Jwt.class, authentication.getCredentials());
            assertEquals("payment", jwt.getSubject());
            assertEquals("tenant", jwt.getClaimAsString("tenant_id"));
            assertTrue(authentication.getAuthorities().stream()
                    .anyMatch(authority -> "pay:test".equals(authority.getAuthority())));
            return mock(Result.class);
        });
        SecurityContext previous = SecurityContextHolder.createEmptyContext();
        SecurityContextHolder.setContext(previous);

        filter.invoke(invoker, invocation);

        assertSame(previous, SecurityContextHolder.getContext());
    }

    @Test
    void businessRejectionIsRewrittenWithItsDeclaredCode() {
        when(invocation.getAttachment(RpcTokenContext.AUTHORIZATION))
                .thenReturn("Bearer " + token(Instant.now().plusSeconds(300)));
        when(invocation.getArguments()).thenReturn(new Object[0]);
        when(invoker.invoke(invocation)).thenThrow(new CommonException(CommonResultCodeEnum.UNAUTHENTICATED));

        var failure = assertThrows(RpcException.class, () -> filter.invoke(invoker, invocation));

        assertEquals(RpcException.BIZ_EXCEPTION, failure.getCode());
        assertEquals(CommonResultCodeEnum.UNAUTHENTICATED.getCode(), RpcExceptionCodes.resultCode(failure));
    }

    @Test
    void invalidCommandIsRejectedBeforeTheBusinessMethod() {
        var validator = mock(jakarta.validation.Validator.class);
        var violation = mock(jakarta.validation.ConstraintViolation.class);
        when(validator.validate(any())).thenReturn(java.util.Set.of(violation));
        filter.setValidator(validator);
        when(invocation.getAttachment(RpcTokenContext.AUTHORIZATION))
                .thenReturn("Bearer " + token(Instant.now().plusSeconds(300)));
        when(invocation.getArguments()).thenReturn(new Object[] {"command"});

        var failure = assertThrows(RpcException.class, () -> filter.invoke(invoker, invocation));

        assertEquals(RpcException.VALIDATION_EXCEPTION, failure.getCode());
        verifyNoInteractions(invoker);
    }

    private static String token(Instant expiresAt) {
        String header = encode("{\"alg\":\"RS256\"}");
        String claims = encode("{\"sub\":\"payment\",\"username\":\"service\",\"tenant_id\":\"tenant\","
                + "\"roles\":[\"SERVICE\"],\"permissions\":[\"pay:test\"],"
                + "\"exp\":" + expiresAt.getEpochSecond() + "}");
        return header + "." + claims + ".c2lnbmF0dXJl";
    }

    private static String encode(String value) {
        return ENCODER.encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }
}
