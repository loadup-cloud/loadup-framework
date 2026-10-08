/*
 * #%L
 * LoadUp Components Signature
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
package io.github.loadup.components.signature.request;

import io.github.loadup.components.kms.KmsAutoConfiguration;
import io.github.loadup.components.kms.KmsTemplate;
import java.time.Clock;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

/** Enabling request verification requires a real atomic nonce store; no local fallback. */
@AutoConfiguration(
        after = KmsAutoConfiguration.class,
        afterName = "org.springframework.boot.data.redis.autoconfigure.DataRedisAutoConfiguration")
@ConditionalOnProperty(
        prefix = "loadup.components.signature",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
@ConditionalOnProperty(prefix = "loadup.components.signature.request", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(RequestSignatureProperties.class)
public class RequestSignatureAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public RequestSigner requestSigner(
            KmsTemplate kms, ObjectProvider<Clock> clock, RequestSignatureProperties properties) {
        return new RequestSigner(kms, clock.getIfAvailable(Clock::systemUTC), properties);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(
            prefix = "loadup.components.signature.request",
            name = "verification-enabled",
            havingValue = "true",
            matchIfMissing = true)
    public RequestVerifier requestVerifier(
            KmsTemplate kms,
            RequestNonceStore nonces,
            ObjectProvider<Clock> clock,
            RequestSignatureProperties properties) {
        return new RequestVerifier(kms, nonces, clock.getIfAvailable(Clock::systemUTC), properties);
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(StringRedisTemplate.class)
    @ConditionalOnBean(StringRedisTemplate.class)
    @ConditionalOnProperty(
            prefix = "loadup.components.signature.request",
            name = "verification-enabled",
            havingValue = "true",
            matchIfMissing = true)
    static class RedisNonceConfiguration {
        @Bean
        @ConditionalOnMissingBean(RequestNonceStore.class)
        RedisRequestNonceStore requestNonceStore(StringRedisTemplate redis) {
            return new RedisRequestNonceStore(redis);
        }
    }
}
