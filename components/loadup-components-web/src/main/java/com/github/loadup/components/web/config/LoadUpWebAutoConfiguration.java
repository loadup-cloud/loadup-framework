package com.github.loadup.components.web.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.loadup.components.web.advice.GlobalExceptionHandler;
import com.github.loadup.components.web.advice.UnifiedResponseAdvice;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@AutoConfiguration
// 允许宿主项目通过配置文件关闭此功能：loadup.framework.web.response-advice.enabled=false
@ConditionalOnProperty(
    prefix = "loadup.framework.web.response-advice",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true)
@Import({UnifiedResponseAdvice.class}) // 导入拦截器
public class LoadUpWebAutoConfiguration {

  @Bean
  public UnifiedResponseAdvice unifiedResponseAdvice(ObjectMapper objectMapper) {
    return new UnifiedResponseAdvice(objectMapper);
  }

  @Bean
  public GlobalExceptionHandler globalExceptionHandler() {
    return new GlobalExceptionHandler();
  }
}
