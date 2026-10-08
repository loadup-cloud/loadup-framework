package io.github.loadup.components.pipeline.config;

import io.github.loadup.components.pipeline.engine.PipelineExecutor;
import io.github.loadup.components.pipeline.tx.DefaultSpringTxInitializer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

/**
 * Spring Boot auto-configuration for {@code loadup-components-pipeline}.
 *
 * <p>Registers:
 * <ul>
 *   <li>{@link PipelineExecutor} — the core execution engine</li>
 *   <li>{@link DefaultSpringTxInitializer} — default REQUIRED-propagation TX initialiser</li>
 * </ul>
 */
@AutoConfiguration
public class PipelineAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(PipelineExecutor.class)
    public PipelineExecutor pipelineExecutor(ApplicationContext applicationContext) {
        return new PipelineExecutor(applicationContext);
    }

    @Bean
    @ConditionalOnMissingBean(DefaultSpringTxInitializer.class)
    public DefaultSpringTxInitializer defaultSpringTxInitializer() {
        return new DefaultSpringTxInitializer();
    }
}
