package io.github.loadup.modules.upms;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Test Application for UPMS Module Tests
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@SpringBootApplication
// @Import(MyBatisTestConfig.class)
@ComponentScan(
    basePackages = {
      "io.github.loadup.modules.upms.infrastructure.repository",
      "io.github.loadup.modules.upms.infrastructure.converter",
      "io.github.loadup.components.database"
    })
@MapperScan("io.github.loadup.modules.upms.infrastructure.mapper")
public class TestApplication {


  public static void main(String[] args) {
    SpringApplication.run(TestApplication.class, args);
  }
}
