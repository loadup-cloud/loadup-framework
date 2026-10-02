package io.github.loadup.modules.audit.web;

import io.github.loadup.modules.audit.AuditService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Optional MVC capture and query surface for the audit center. */
@AutoConfiguration(afterName = "io.github.loadup.modules.audit.autoconfigure.AuditAutoConfiguration")
@ConditionalOnBean(AuditService.class)
@ConditionalOnProperty(prefix = "loadup.audit.web", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(AuditWebProperties.class)
public class AuditWebAutoConfiguration {
    @Bean
    public AuditController auditController(AuditService service) {
        return new AuditController(service);
    }

    @Bean
    public AuditResponseAdvice auditResponseAdvice() {
        return new AuditResponseAdvice();
    }

    @Bean
    public WebMvcConfigurer auditMvcConfigurer(AuditService service, AuditWebProperties properties) {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                if (!properties.getIncludePaths().isEmpty()) {
                    registry.addInterceptor(new AuditCaptureInterceptor(service, properties))
                            .addPathPatterns(properties.getIncludePaths());
                }
            }
        };
    }
}
