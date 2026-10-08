/*
 * #%L
 * LoadUp Web MVC
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
package io.github.loadup.components.webmvc;

import io.github.loadup.commons.dto.DTO;
import io.github.loadup.commons.util.JsonUtil;
import io.github.loadup.components.observability.ApiResultMetrics;
import jakarta.servlet.DispatcherType;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.http.converter.autoconfigure.ServerHttpMessageConvertersCustomizer;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.webmvc.error.ErrorAttributes;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import tools.jackson.databind.ObjectMapper;

@AutoConfiguration(beforeName = "org.springframework.boot.webmvc.autoconfigure.error.ErrorMvcAutoConfiguration")
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class LoadUpWebMvcAutoConfiguration {
    @Bean
    public FilterRegistrationBean<ExecutionContextFilter> loadUpExecutionContextFilter() {
        var registration = new FilterRegistrationBean<>(new ExecutionContextFilter());
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        registration.setAsyncSupported(true);
        registration.setDispatcherTypes(DispatcherType.REQUEST, DispatcherType.ASYNC, DispatcherType.ERROR);
        return registration;
    }

    @Bean
    public ApiPathMatcher loadUpApiPathMatcher() {
        return new ApiPathMatcher();
    }

    @Bean
    @Order(1)
    public JsonMapperBuilderCustomizer loadUpJsonMapperBuilderCustomizer() {
        return JsonUtil::customize;
    }

    @Bean
    public SmartInitializingSingleton loadUpJsonMapperSync(ObjectMapper objectMapper) {
        return () -> {
            DTO.setObjectMapper(objectMapper);
            JsonUtil.setObjectMapper(objectMapper);
        };
    }

    @Bean
    @ConditionalOnMissingBean(ApiMaskingJson.class)
    public ApiMaskingJson loadUpApiMaskingJson(ObjectMapper objectMapper) {
        return new ApiMaskingJson(objectMapper);
    }

    @Bean
    @Order(100)
    public ServerHttpMessageConvertersCustomizer loadUpMaskingServerConverter(ApiMaskingJson masking) {
        return builder -> builder.withJsonConverter(new JacksonJsonHttpMessageConverter(masking.mapper()));
    }

    @Bean
    @ConditionalOnMissingBean(ApiResponseAdvice.class)
    public ApiResponseAdvice loadUpApiResponseAdvice(
            ApiPathMatcher pathMatcher, ApiMaskingJson masking, ApiResultMetrics resultMetrics) {
        return new ApiResponseAdvice(pathMatcher, masking, resultMetrics);
    }

    @Bean
    @ConditionalOnMissingBean(ErrorController.class)
    public ApiErrorController loadUpApiErrorController(ApiPathMatcher pathMatcher, ErrorAttributes errorAttributes) {
        return new ApiErrorController(pathMatcher, errorAttributes);
    }
}
