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

import jakarta.validation.Validator;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.GrpcChannelFactory;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import io.github.loadup.components.resourceserver.LoadUpJwtAuthenticationConverter;
import io.github.loadup.components.resourceserver.ResourceServerProperties;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(RpcClientProperties.class)
public class GrpcSupportConfiguration {
    @Bean
    GrpcTransport grpcTransport(GrpcChannelFactory channels, RpcClientProperties properties) {
        return new GrpcTransport(channels, properties);
    }
    @Bean
    RpcServerSupport rpcServerSupport(Validator validator) { return new RpcServerSupport(validator); }
    @Bean
    RpcJwtInterceptor rpcJwtInterceptor(JwtDecoder decoder, LoadUpJwtAuthenticationConverter converter,
            ResourceServerProperties properties) {
        return new RpcJwtInterceptor(decoder, converter, properties);
    }
}
