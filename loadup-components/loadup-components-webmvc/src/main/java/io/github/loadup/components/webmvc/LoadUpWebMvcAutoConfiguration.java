package io.github.loadup.components.webmvc;

import io.github.loadup.commons.dto.DTO;
import io.github.loadup.commons.util.JsonUtil;
import io.github.loadup.components.observability.ApiResultMetrics;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.boot.webmvc.error.ErrorAttributes;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import tools.jackson.databind.ObjectMapper;

@AutoConfiguration(beforeName = "org.springframework.boot.webmvc.autoconfigure.error.ErrorMvcAutoConfiguration")
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class LoadUpWebMvcAutoConfiguration {
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
    @ConditionalOnMissingBean(ApiResponseAdvice.class)
    public ApiResponseAdvice loadUpApiResponseAdvice(
            ApiPathMatcher pathMatcher, ObjectMapper objectMapper, ApiResultMetrics resultMetrics) {
        return new ApiResponseAdvice(pathMatcher, objectMapper, resultMetrics);
    }

    @Bean
    @ConditionalOnMissingBean(ErrorController.class)
    public ApiErrorController loadUpApiErrorController(ApiPathMatcher pathMatcher, ErrorAttributes errorAttributes) {
        return new ApiErrorController(pathMatcher, errorAttributes);
    }
}
