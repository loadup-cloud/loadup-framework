/*
 * #%L
 * LoadUp Async Result
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
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
 * #L%
 */
package io.github.loadup.components.asyncresult.redis;

import io.github.loadup.commons.log.LogUtil;
import io.github.loadup.components.asyncresult.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import tools.jackson.databind.json.JsonMapper;

/** Uses string-only protocol data and a single-key CAS; caller owns the Redisson client. */
public final class RedisAsyncResultProvider implements AsyncResultProvider {
    private static final String REGISTER = """
        if redis.call('EXISTS', KEYS[1]) == 1 then return 0 end
        redis.call('HSET', KEYS[1], 'owner', ARGV[1])
        redis.call('PEXPIRE', KEYS[1], ARGV[2])
        return 1
        """;
    private static final String COMPLETE = """
        if redis.call('EXISTS', KEYS[1]) == 0 then return 3 end
        local previous = redis.call('HGET', KEYS[1], 'result')
        if previous then
            if previous == ARGV[1] then return 1 else return 2 end
        end
        redis.call('HSET', KEYS[1], 'result', ARGV[1])
        local owner = redis.call('HGET', KEYS[1], 'owner')
        redis.call('PUBLISH', ARGV[2] .. owner, ARGV[3])
        return 0
        """;
    private final RedissonClient client;
    private final String prefix;
    // This fixed internal protocol has no polymorphic types, time values or application mapper customizations.
    private final JsonMapper protocol = JsonMapper.builder().build();

    public RedisAsyncResultProvider(RedissonClient client, String namespace) {
        AsyncReplyKey.required(namespace, 128);
        this.client = java.util.Objects.requireNonNull(client);
        prefix = "loadup:async-result:" + encode(namespace) + ":";
    }

    private static String encode(String value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private String key(AsyncReplyKey key) {
        return prefix + "result:" + encode(key.tenantId()) + ":" + encode(key.operation()) + ":"
                + encode(key.businessId()) + ":" + key.correlationId();
    }

    @Override
    public void register(AsyncReplyKey key, String owner, Duration retention) {
        AsyncReplyKey.required(owner, 128);
        if (retention.toMillis() < 1 || retention.compareTo(Duration.ofDays(7)) > 0)
            throw new IllegalArgumentException("Invalid result retention");
        Number result = client.getScript(StringCodec.INSTANCE)
                .eval(
                        RScript.Mode.READ_WRITE,
                        REGISTER,
                        RScript.ReturnType.LONG,
                        List.of(key(key)),
                        owner,
                        retention.toMillis());
        if (result.intValue() != 1) throw new IllegalArgumentException("Reply key is already registered");
    }

    @Override
    public Optional<AsyncResultValue> find(AsyncReplyKey key) {
        String value =
                client.<String, String>getMap(key(key), StringCodec.INSTANCE).get("result");
        return value == null ? Optional.empty() : Optional.of(protocol.readValue(value, AsyncResultValue.class));
    }

    @Override
    public AsyncCompletionStatus complete(AsyncReplyKey key, AsyncResultValue value) {
        Number result = client.getScript(StringCodec.INSTANCE)
                .eval(
                        RScript.Mode.READ_WRITE,
                        COMPLETE,
                        RScript.ReturnType.LONG,
                        List.of(key(key)),
                        protocol.writeValueAsString(value),
                        prefix + "notify:",
                        protocol.writeValueAsString(key));
        return switch (result.intValue()) {
            case 0 -> AsyncCompletionStatus.ACCEPTED;
            case 1 -> AsyncCompletionStatus.DUPLICATE;
            case 2 -> AsyncCompletionStatus.CONFLICT;
            default -> AsyncCompletionStatus.UNKNOWN;
        };
    }

    @Override
    public AutoCloseable subscribe(String owner, Consumer<AsyncReplyKey> listener) {
        AsyncReplyKey.required(owner, 128);
        var topic = client.getTopic(prefix + "notify:" + owner, StringCodec.INSTANCE);
        int id = topic.addListener(String.class, (channel, message) -> {
            try {
                listener.accept(protocol.readValue(message, AsyncReplyKey.class));
            } catch (RuntimeException failure) {
                LogUtil.warn(
                        RedisAsyncResultProvider.class,
                        "Async result notification rejected; type={}",
                        failure.getClass().getSimpleName());
            }
        });
        return () -> topic.removeListener(id);
    }
}
