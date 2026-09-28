package io.github.loadup.components.globalunique;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.flyway.autoconfigure.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;

/**
 * Test application that starts a fresh schema for every integration test run.
 */
@SpringBootApplication
public class TestApplication {

    @Bean
    public FlywayMigrationStrategy cleanMigrateStrategy() {
        return flyway -> {
            flyway.clean();
            flyway.migrate();
        };
    }

    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }
}
