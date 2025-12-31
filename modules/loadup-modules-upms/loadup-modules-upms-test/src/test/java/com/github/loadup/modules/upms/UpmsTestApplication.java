package com.github.loadup.modules.upms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * Test Application for UPMS Module Tests
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@SpringBootApplication(
    exclude = {DataSourceAutoConfiguration.class},
    scanBasePackages = {"com.github.loadup.modules.upms"})
public class UpmsTestApplication {

  public static void main(String[] args) {
    SpringApplication.run(UpmsTestApplication.class, args);
  }
}
