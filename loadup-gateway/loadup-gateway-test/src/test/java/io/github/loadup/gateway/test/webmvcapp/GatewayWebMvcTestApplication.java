package io.github.loadup.gateway.test.webmvcapp;

import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Minimal Spring Boot context for gateway MVC engine tests. The application scans only this
 * package, so the legacy core engine beans are never activated.
 */
@SpringBootApplication
public class GatewayWebMvcTestApplication {}
