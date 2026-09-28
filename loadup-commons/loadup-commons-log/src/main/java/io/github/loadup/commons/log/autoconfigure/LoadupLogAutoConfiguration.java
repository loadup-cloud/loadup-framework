package io.github.loadup.commons.log.autoconfigure;

import io.github.loadup.commons.log.LoadupLogProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@AutoConfiguration
@EnableConfigurationProperties(LoadupLogProperties.class)
@ConditionalOnProperty(prefix = "loadup.log", name = "enabled", havingValue = "true", matchIfMissing = true)
public class LoadupLogAutoConfiguration {}
