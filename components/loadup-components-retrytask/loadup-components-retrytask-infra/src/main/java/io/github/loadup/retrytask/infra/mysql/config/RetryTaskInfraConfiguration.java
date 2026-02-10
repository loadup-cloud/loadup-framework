package io.github.loadup.retrytask.infra.mysql.config;

import io.github.loadup.retrytask.infra.api.management.RetryTaskManagement;
import io.github.loadup.retrytask.infra.mysql.converter.PriorityToStringConverter;
import io.github.loadup.retrytask.infra.mysql.converter.StringToPriorityConverter;
import io.github.loadup.retrytask.infra.mysql.repository.RetryTaskDORepository;
import io.github.loadup.retrytask.infra.mysql.repository.RetryTaskManagementImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;

import java.util.Arrays;

import org.springframework.context.annotation.Primary;

@Configuration
public class RetryTaskInfraConfiguration extends AbstractJdbcConfiguration {

    @Bean
    @Primary
    public RetryTaskManagement retryTaskRepository(RetryTaskDORepository retryTaskDORepository) {
        return new RetryTaskManagementImpl(retryTaskDORepository);
    }

    @Override
    public JdbcCustomConversions jdbcCustomConversions() {
        return new JdbcCustomConversions(Arrays.asList(
                new PriorityToStringConverter(),
                new StringToPriorityConverter()
        ));
    }
}
