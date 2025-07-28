/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.gateway.cache.common;

/*-
 * #%L
 * loadup-components-gateway-core
 * %%
 * Copyright (C) 2022 - 2025 loadup_cloud
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import com.github.loadup.components.gateway.core.common.Constant;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * DefaultGatewayConfigs
 */
@Component("gatewaySystemParameter")
@Order(Integer.MIN_VALUE)
@Slf4j
public class DefaultGatewayConfigs implements ApplicationListener<ApplicationStartedEvent> {

    private static Map<String, String> parameters = new ConcurrentHashMap<>();
    /**
     * configRootPathPropertyName can be set in JVM argument for configuration root path
     */
    @Value("${gateway.config.root.path.property.name:gateway.config.rootpath}")
    private        String              configRootPathPropertyName;
    /**
     * the configuration file path of SECURITY_STRATEGY
     */
    @Value("${cert.algorithm.config.file.path:config/gateway/SECURITY_STRATEGY.csv}")
    private String certAlgorithmConfigFilePath;
    /**
     * the configuration file path of OPENAPI
     */
    @Value("${openapi.config.file.path:config/gateway/OPENAPI.csv}")
    private String openapiConfigFilePath;
    /**
     * the configuration file path of SPI
     */
    @Value("${spi.config.file.path:config/gateway/SPI.csv}")
    private String spiConfigFilePath;
    /**
     * the configuration file path of assembler
     */
    @Value("${assemble.template.file.directory:config/gateway/assembler/}")
    private String assembleTemplateFileDirectory;
    /**
     * the configuration file path of parser
     */
    @Value("${parse.template.file.directory:config/gateway/parser/}")
    private String parseTemplateFileDirectory;

    @Value("${repository.extend.point:FILE}")
    private String repositoryExtendPoint;

    @Value("${print.log.max.length:10000}")
    private String maxLogLength;

    /**
     * Put parameter
     */
    public static void put(String key, String value) {
        parameters.put(key, value);
    }

    /**
     *  Get parameter
     */
    public static String get(String key) {
        return parameters.get(key);
    }

    /**
     * Print
     */
    public static void log() {
        parameters.forEach((k, v) -> log.debug("system parameter key is {}, value is {}", k, v));
    }

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        // check if there is an argument declared with configRootPathPropertyName in JVM
        String configRootPath = StringUtils.defaultIfBlank(System.getProperty(configRootPathPropertyName),
                this.getClass().getResource("/").getPath());

        DefaultGatewayConfigs.put("configRootPathPropertyName", configRootPathPropertyName);
        DefaultGatewayConfigs.put("certAlgorithmConfigFilePath", certAlgorithmConfigFilePath);
        DefaultGatewayConfigs.put("openapiConfigFilePath", openapiConfigFilePath);
        DefaultGatewayConfigs.put("spiConfigFilePath", spiConfigFilePath);
        DefaultGatewayConfigs.put("assembleTemplateFileDirectory", assembleTemplateFileDirectory);
        DefaultGatewayConfigs.put("parseTemplateFileDirectory", parseTemplateFileDirectory);
        DefaultGatewayConfigs.put("configRootPath", configRootPath);
        DefaultGatewayConfigs.put(Constant.REPOSITORY_EXTPOINT_BIZCODE, repositoryExtendPoint);
        DefaultGatewayConfigs.put("maxLogLength", maxLogLength);
        DefaultGatewayConfigs.log();
    }
}
