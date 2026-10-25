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

import com.google.protobuf.Message;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientInterceptors;
import io.grpc.Metadata;
import io.grpc.stub.ClientCalls;
import io.grpc.stub.MetadataUtils;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.springframework.grpc.client.GrpcChannelFactory;

/** Native unary RPCs over starter-owned channels; credentials are attached per call, never cached. */
public final class GrpcTransport {
    private final GrpcChannelFactory factory;
    private final RpcClientProperties properties;
    private final ConcurrentHashMap<String, Channel> channels = new ConcurrentHashMap<>();

    public GrpcTransport(GrpcChannelFactory factory, RpcClientProperties properties) {
        this.factory = factory;
        this.properties = properties;
    }

    public <Q, R, P extends Message, S extends Message> R call(
            RpcOperation<Q, R, P, S> operation, Q request, String token) {
        var peer = properties.clients() == null ? null : properties.clients().get(operation.peer());
        if (peer == null
                || peer.channel() == null
                || peer.channel().isBlank()
                || peer.deadline() == null
                || peer.deadline().isZero()
                || peer.deadline().isNegative())
            throw new IllegalStateException("RPC channel and positive deadline are required");
        if (token == null || !token.matches("[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+"))
            throw new IllegalArgumentException("RPC bearer credential is required");
        var metadata = new Metadata();
        metadata.put(RpcServerSupport.AUTHORIZATION, "Bearer " + token);
        var channel = ClientInterceptors.intercept(
                channels.computeIfAbsent(peer.channel(), factory::createChannel),
                MetadataUtils.newAttachHeadersInterceptor(metadata));
        var reply = ClientCalls.blockingUnaryCall(
                channel,
                operation.descriptor(),
                CallOptions.DEFAULT.withDeadlineAfter(peer.deadline().toNanos(), TimeUnit.NANOSECONDS),
                operation.encode().apply(request));
        return operation.decode().apply(reply);
    }
}
