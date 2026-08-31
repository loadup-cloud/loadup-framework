package io.github.loadup.gateway.plugins.config;

/*-
 * #%L
 * Proxy RPC Plugin
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

import io.github.loadup.gateway.facade.config.GatewayProperties;
import io.github.loadup.gateway.plugins.RpcProxyProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(org.apache.dubbo.rpc.service.GenericService.class)
public class RpcProxyAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RpcProxyProcessor rpcProxyProcessor(GatewayProperties props) {
        return new RpcProxyProcessor(props);
    }
}
