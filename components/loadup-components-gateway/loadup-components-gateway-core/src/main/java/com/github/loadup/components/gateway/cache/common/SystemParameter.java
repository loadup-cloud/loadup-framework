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
import com.github.loadup.components.gateway.facade.util.LogUtil;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * <p>
 * SystemParameter.java
 * </p>
 */
@Component("gatewaySystemParameter")
@Order(Integer.MIN_VALUE)
public class SystemParameter implements ApplicationListener<ApplicationStartedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(SystemParameter.class);
    /**
     * system parameters
     */
    public static Map<String, String> systemParameters = new ConcurrentHashMap<>();
    /**
     * configRootPathPropertyName can be set in JVM argument for configuration root path
     */
    @Value("${gateway.config.root.path.property.name:gateway.config.rootpath}")
    private String configRootPathPropertyName;
    /**
     * the configuration file path of SECURITY_STRATEGY_CONF
     */
    @Value("${cert.algorithm.config.file.path:config/gateway/SECURITY_STRATEGY_CONF.csv}")
    private String certAlgorithmConfigFilePath;
    /**
     * the configuration file path of OPENAPI_CONF
     */
    @Value("${openapi.config.file.path:config/gateway/OPENAPI_CONF.csv}")
    private String openapiConfigFilePath;
    /**
     * the configuration file path of SPI_CONF
     */
    @Value("${spi.config.file.path:config/gateway/SPI_CONF.csv}")
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
     * Put parameter.
     */
    public static void putParameter(String key, String value) {
        systemParameters.put(key, value);
    }

    /**
     * Gets get parameter.
     */
    public static String getParameter(String key) {
        return systemParameters.get(key);
    }

    /**
     * Print.
     */
    public static void printLog() {
        systemParameters.forEach((k, v) -> LogUtil.debug(logger, "system parameter key is ", k, ", value is ", v));
    }

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        // check if there is an argument declared with configRootPathPropertyName in JVM
        String configRootPath = StringUtils.defaultIfBlank(
                System.getProperty(configRootPathPropertyName),
                this.getClass().getResource("/").getPath());

        SystemParameter.putParameter("configRootPathPropertyName", configRootPathPropertyName);
        SystemParameter.putParameter("certAlgorithmConfigFilePath", certAlgorithmConfigFilePath);
        SystemParameter.putParameter("openapiConfigFilePath", openapiConfigFilePath);
        SystemParameter.putParameter("spiConfigFilePath", spiConfigFilePath);
        SystemParameter.putParameter("assembleTemplateFileDirectory", assembleTemplateFileDirectory);
        SystemParameter.putParameter("parseTemplateFileDirectory", parseTemplateFileDirectory);
        SystemParameter.putParameter("configRootPath", configRootPath);
        SystemParameter.putParameter(Constant.REPOSITORY_EXTPOINT_BIZCODE, repositoryExtendPoint);
        SystemParameter.putParameter("maxLogLength", maxLogLength);

        SystemParameter.printLog();
    }
}
