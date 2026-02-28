package io.github.loadup.modules.log;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(scanBasePackages = "io.github.loadup.modules.log")
@MapperScan("io.github.loadup.modules.log.infrastructure.mapper")
@EnableAsync
public class LogTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(LogTestApplication.class, args);
    }
}

