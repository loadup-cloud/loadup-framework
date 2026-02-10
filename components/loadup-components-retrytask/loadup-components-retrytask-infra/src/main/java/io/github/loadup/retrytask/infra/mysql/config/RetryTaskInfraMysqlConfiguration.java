package io.github.loadup.retrytask.infra.mysql.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;

@Configuration
@EnableJdbcRepositories(basePackages = "io.github.loadup.retrytask.infra.mysql.repository")
public class RetryTaskInfraMysqlConfiguration {

}
