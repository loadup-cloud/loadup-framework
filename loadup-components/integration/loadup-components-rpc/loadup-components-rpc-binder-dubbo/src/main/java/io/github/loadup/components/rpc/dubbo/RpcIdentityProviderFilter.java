/*
 * Copyright (C) 2025-2026 LoadUp Cloud
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.loadup.components.rpc.dubbo;

import io.github.loadup.commons.error.CommonException;
import io.github.loadup.components.resourceserver.LoadUpJwtAuthenticationConverter;
import jakarta.validation.Validator;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.Filter;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.RpcException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

@Activate(group = CommonConstants.PROVIDER, order = RpcIdentityProviderFilter.ORDER)
public final class RpcIdentityProviderFilter implements Filter {
    /**
     * Runs after the framework context filters but before the framework exception filter, so the
     * rewritten failure is the one serialized back to the caller.
     */
    public static final int ORDER = -20000;

    private final LoadUpJwtAuthenticationConverter converter = new LoadUpJwtAuthenticationConverter();
    private Validator validator;

    private static Jwt parse(String token) {
        try {
            return RpcJwtClaims.parse(token);
        } catch (RuntimeException invalid) {
            throw new RpcException(RpcException.AUTHORIZATION_EXCEPTION, "Valid access token required");
        }
    }

    /**
     * Injected by Dubbo from the Spring context when a {@link Validator} bean is available.
     */
    public void setValidator(Validator validator) {
        this.validator = validator;
    }

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        SecurityContext previous = SecurityContextHolder.getContext();
        try {
            SecurityContextHolder.setContext(authenticate(invocation));
            validate(invocation);
            Result result;
            try {
                result = invoker.invoke(invocation);
            } catch (CommonException failure) {
                throw RpcExceptionCodes.business(failure);
            }
            if (result.hasException() && result.getException() instanceof CommonException failure) {
                result.setException(RpcExceptionCodes.business(failure));
            }
            return result;
        } finally {
            SecurityContextHolder.setContext(previous);
        }
    }

    private SecurityContext authenticate(Invocation invocation) {
        String authorization = invocation.getAttachment(RpcTokenContext.AUTHORIZATION);
        if (authorization == null) {
            authorization = RpcContext.getServerAttachment().getAttachment(RpcTokenContext.AUTHORIZATION);
        }
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new RpcException(RpcException.AUTHORIZATION_EXCEPTION, "Valid access token required");
        }
        var jwt = parse(authorization.substring(7));
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(converter.convert(jwt));
        return context;
    }

    private void validate(Invocation invocation) {
        if (validator == null) {
            return;
        }
        for (Object argument : invocation.getArguments()) {
            if (argument != null && !validator.validate(argument).isEmpty()) {
                throw new RpcException(RpcException.VALIDATION_EXCEPTION, "Invalid RPC command");
            }
        }
    }
}
