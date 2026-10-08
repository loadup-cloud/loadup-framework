package io.github.loadup.components.observability;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/** Uses Spring Boot's MeterRegistry and tracing auto-configuration as the single backend. */
@AutoConfiguration
public class ObservabilityAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(LoadUpContextTaskDecorator.class)
    public LoadUpContextTaskDecorator loadUpContextTaskDecorator() {
        return new LoadUpContextTaskDecorator();
    }

    @Bean
    public ApiResultMetrics apiResultMetrics(MeterRegistry registry) {
        return new ApiResultMetrics(registry);
    }
}
