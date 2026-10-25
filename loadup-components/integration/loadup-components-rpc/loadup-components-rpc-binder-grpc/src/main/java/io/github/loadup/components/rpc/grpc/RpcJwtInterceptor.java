/*
 * Copyright 2026 LoadUp Cloud
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.loadup.components.rpc.grpc;

import io.github.loadup.components.resourceserver.LoadUpJwtAuthenticationConverter;
import io.github.loadup.components.resourceserver.ResourceServerProperties;
import io.grpc.ForwardingServerCallListener;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;

/** Reuses the HTTP resource-server trust policy without depending on servlet request state. */
public final class RpcJwtInterceptor implements ServerInterceptor {
    private final JwtDecoder decoder;
    private final LoadUpJwtAuthenticationConverter converter;
    private final ResourceServerProperties properties;

    public RpcJwtInterceptor(
            JwtDecoder decoder, LoadUpJwtAuthenticationConverter converter, ResourceServerProperties properties) {
        this.decoder = decoder;
        this.converter = converter;
        this.properties = properties;
    }

    @Override
    public <Q, R> ServerCall.Listener<Q> interceptCall(
            ServerCall<Q, R> call, Metadata headers, ServerCallHandler<Q, R> next) {
        final SecurityContext context;
        try {
            String authorization = headers.get(RpcServerSupport.AUTHORIZATION);
            if (authorization == null || !authorization.startsWith("Bearer ")) throw new IllegalArgumentException();
            var jwt = decoder.decode(authorization.substring(7));
            if (properties.getIssuerUri() == null
                    || properties.getIssuerUri().isBlank()
                    || properties.getAudience() == null
                    || properties.getAudience().isBlank()
                    || JwtValidators.createDefaultWithIssuer(properties.getIssuerUri())
                            .validate(jwt)
                            .hasErrors()
                    || jwt.getAudience() == null
                    || !jwt.getAudience().contains(properties.getAudience())) throw new IllegalArgumentException();
            String useClaim = properties.getAccessTokenUseClaim();
            if (useClaim != null
                    && !useClaim.isBlank()
                    && (properties.getAccessTokenUseValue() == null
                            || !properties.getAccessTokenUseValue().equals(jwt.getClaimAsString(useClaim))))
                throw new IllegalArgumentException();
            context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(converter.convert(jwt));
        } catch (RuntimeException invalid) {
            call.close(Status.UNAUTHENTICATED.withDescription("Valid access token required"), new Metadata());
            return new ServerCall.Listener<>() {};
        }
        SecurityContext previous = SecurityContextHolder.getContext();
        final ServerCall.Listener<Q> delegate;
        try {
            SecurityContextHolder.setContext(context);
            delegate = next.startCall(call, headers);
        } finally {
            SecurityContextHolder.setContext(previous);
        }
        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<>(delegate) {
            private void authenticated(Runnable work) {
                SecurityContext prior = SecurityContextHolder.getContext();
                try {
                    SecurityContextHolder.setContext(context);
                    work.run();
                } finally {
                    SecurityContextHolder.setContext(prior);
                }
            }

            @Override
            public void onMessage(Q message) {
                authenticated(() -> super.onMessage(message));
            }

            @Override
            public void onHalfClose() {
                authenticated(super::onHalfClose);
            }

            @Override
            public void onCancel() {
                authenticated(super::onCancel);
            }

            @Override
            public void onComplete() {
                authenticated(super::onComplete);
            }

            @Override
            public void onReady() {
                authenticated(super::onReady);
            }
        };
    }
}
