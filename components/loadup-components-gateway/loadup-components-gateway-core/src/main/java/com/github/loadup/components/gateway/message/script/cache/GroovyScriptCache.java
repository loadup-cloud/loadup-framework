package com.github.loadup.components.gateway.message.script.cache;

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

import com.github.loadup.commons.error.CommonException;
import com.github.loadup.components.gateway.common.util.RepositoryUtil;
import com.github.loadup.components.gateway.common.util.TenantUtil;
import com.github.loadup.components.gateway.common.util.UriUtil;
import com.github.loadup.components.gateway.core.common.GatewayErrorCode;
import com.github.loadup.components.gateway.core.common.enums.InterfaceType;
import com.github.loadup.components.gateway.core.common.enums.RepositoryType;
import com.github.loadup.components.gateway.core.common.enums.RoleType;
import com.github.loadup.components.gateway.core.model.InterfaceConfig;
import com.github.loadup.components.gateway.core.model.MessageProcessConfig;
import com.github.loadup.components.gateway.core.service.InterfaceProdCenterQueryService;
import com.github.loadup.components.gateway.facade.config.model.APIConditionGroup;
import com.github.loadup.components.gateway.facade.config.model.Constant;
import com.github.loadup.components.gateway.facade.config.model.SPIConditionGroup;
import com.github.loadup.components.gateway.message.script.parser.groovy.GroovyInnerCache;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.github.loadup.components.gateway.core.common.Constant.PATH_CONJUNCTION;

/**
 * groovy脚本名称缓存
 */
@Component("gatewayGroovyScriptCache")
public class GroovyScriptCache {

    /**
     * 读写锁
     */
    private static final ReadWriteLock lock = new ReentrantReadWriteLock();
    /**
     * interfaceId-beanName cache
     */
    private static Map<String, String> cache = new HashMap<String, String>();
    /**
     * interface product center query service
     */
    private static InterfaceProdCenterQueryService interfaceProdCenterQueryService;

    /**
     * 初始化groovy的beanName缓存
     *
     * @clear clear
     */
    public static void putAll(
            boolean clear, List<InterfaceConfig> interfaceConfigs, Map<String, MessageProcessConfig> processConfigs) {
        if (interfaceConfigs == null) {
            return;
        }
        Lock writelock = lock.writeLock();
        writelock.lock();
        try {
            String processorId = null;
            String interfaceId = null;
            Map<String, String> tempCache = new HashMap<String, String>(interfaceConfigs.size());
            for (InterfaceConfig interfaceConfig : interfaceConfigs) {
                interfaceId = interfaceConfig.getInterfaceId();
                processorId = interfaceConfig.getMessageProcessorId();
                MessageProcessConfig processConfig = processConfigs.get(processorId);
                if (processConfig != null) {
                    tempCache.put(interfaceId + "_PARSER", generateParserBeanName(processConfig));
                    tempCache.put(interfaceId + "_ASSEMBLER", generateAssembleBeanName(processConfig));
                }
            }
            if (clear) {
                cache = tempCache;
            } else {
                cache.putAll(tempCache);
            }
        } finally {
            writelock.unlock();
        }
    }

    /**
     * 更新单个接口部分缓存
     */
    public static void putPart(
            List<InterfaceConfig> interfaceConfigs, Map<String, MessageProcessConfig> processConfigs) {
        Lock writelock = lock.writeLock();
        writelock.lock();
        try {
            for (InterfaceConfig interfaceConfig : interfaceConfigs) {
                String interfaceId = interfaceConfig.getInterfaceId();
                cache.remove(interfaceId);
                MessageProcessConfig processConfig = processConfigs.get(interfaceConfig.getMessageProcessorId());
                if (processConfig != null) {
                    cache.put(interfaceId, generateParserBeanName(processConfig));
                }
            }
        } finally {
            writelock.unlock();
        }
    }

    /**
     * 根据接口Id获得groovy的beanName
     */
    public static String getBeanName(String interfaceId, RoleType roleType, String interfaceTypeStr) {
        if (StringUtils.isBlank(interfaceId)) {
            return null;
        }
        Lock readlock = lock.readLock();
        readlock.lock();
        try {
            if (RepositoryUtil.getRepositoryType() == RepositoryType.PRODCENTER) {
                // beanName format: url-roleType-interfaceType
                if (InterfaceType.getEnumByCode(interfaceTypeStr) == InterfaceType.OPENAPI) {
                    if (RoleType.RECEIVER == roleType) {
                        return getBeanNameByIntegratorInterfaceId(interfaceId, roleType, interfaceTypeStr);
                    }

                    APIConditionGroup apiConditionGroup = null;
                    try {
                        apiConditionGroup = interfaceProdCenterQueryService.queryAPIConditionGroup(interfaceId, null);
                    } catch (CommonException ex) {
                        if (ex.getResultCode() != GatewayErrorCode.CONFIGURATION_LOAD_ERROR) {
                            throw ex;
                        }
                    }
                    if (apiConditionGroup != null) {
                        return generateProdCenterBeanName(
                                apiConditionGroup.getUrl(),
                                apiConditionGroup.getInterfaceRequestParser(),
                                TenantUtil.getCurrentTenantId(),
                                roleType.getCode(),
                                interfaceTypeStr);
                    }
                } else if (InterfaceType.getEnumByCode(interfaceTypeStr) == InterfaceType.SPI) {
                    SPIConditionGroup spiConditionGroup =
                            interfaceProdCenterQueryService.querySPIConditionGroup(interfaceId);

                    return generateProdCenterBeanName(
                            spiConditionGroup.getIntegrationUrl(),
                            spiConditionGroup.getIntegrationResponseParser(),
                            TenantUtil.getCurrentTenantId(),
                            roleType.getCode(),
                            interfaceTypeStr);
                }
            } else {
                return cache.get(interfaceId);
            }
        } finally {
            readlock.unlock();
        }
        return null;
    }

    /**
     * 根据接口Id获得groovy的beanName
     */
    public static String getBeanNameByIntegratorInterfaceId(
            String interfaceId, RoleType roleType, String interfaceTypeStr) {
        if (StringUtils.isBlank(interfaceId)) {
            return null;
        }
        Lock readlock = lock.readLock();
        readlock.lock();
        try {
            if (RepositoryUtil.getRepositoryType() == RepositoryType.PRODCENTER) {
                // beanName format: url-roleType-interfaceType
                if (InterfaceType.getEnumByCode(interfaceTypeStr) == InterfaceType.OPENAPI) {
                    APIConditionGroup apiConditionGroup = null;
                    try {
                        apiConditionGroup = interfaceProdCenterQueryService.queryAPIConditionGroup(null, interfaceId);
                    } catch (CommonException ex) {
                        if (ex.getResultCode() != GatewayErrorCode.CONFIGURATION_LOAD_ERROR) {
                            throw ex;
                        }
                    }
                    if (apiConditionGroup != null) {
                        return generateProdCenterBeanName(
                                apiConditionGroup.getIntegrationUrl(),
                                apiConditionGroup.getIntegrationResponseParser(),
                                TenantUtil.getCurrentTenantId(),
                                roleType.getCode(),
                                interfaceTypeStr);
                    }
                } else if (InterfaceType.getEnumByCode(interfaceTypeStr) == InterfaceType.SPI) {
                    SPIConditionGroup spiConditionGroup =
                            interfaceProdCenterQueryService.querySPIConditionGroup(interfaceId);
                    return generateProdCenterBeanName(
                            spiConditionGroup.getIntegrationUrl(),
                            spiConditionGroup.getIntegrationResponseParser(),
                            TenantUtil.getCurrentTenantId(),
                            roleType.getCode(),
                            interfaceTypeStr);
                }
            } else {
                return cache.get(interfaceId);
            }
        } finally {
            readlock.unlock();
        }
        return null;
    }

    /**
     * 根据openapi的url获得groovy的beanName
     */
    public static String getBeanNameByOpenUrls(String[] openUrls, RoleType roleType, String interfaceTypeStr) {
        Lock readlock = lock.readLock();
        readlock.lock();
        try {
            for (String openUrl : openUrls) {
                APIConditionGroup apiConditionGroup = null;
                try {
                    apiConditionGroup = interfaceProdCenterQueryService.queryAPIConditionGroup(openUrl, null);
                } catch (CommonException ex) {
                    // do nothing because apiConditionGroup by openurl could be null
                    continue;
                }
                if (apiConditionGroup != null) {
                    return generateProdCenterBeanName(
                            apiConditionGroup.getUrl(),
                            apiConditionGroup.getInterfaceRequestParser(),
                            TenantUtil.getCurrentTenantId(),
                            roleType.getCode(),
                            interfaceTypeStr);
                }
            }
        } finally {
            readlock.unlock();
        }
        return null;
    }

    /**
     * generate prodcenter bean name
     */
    private static String generateProdCenterBeanName(
            String url, String parseContent, String tntInstId, String roleType, String interfaceType) {

        if (StringUtils.isBlank(parseContent)) {
            return null;
        }

        if (StringUtils.startsWith(
                parseContent, com.github.loadup.components.gateway.core.common.Constant.PARSE_TEMPLATE_NAME_PREFIX)) {
            return parseContent;
        }

        // 1. get tenant gateway config for prodcenter first, if no, then get platform gateway config.
        String tenantBeanName = generateParserBeanName(url, tntInstId, roleType, interfaceType);
        if (GroovyInnerCache.getByName(tenantBeanName) != null) {
            return tenantBeanName;
        }
        // platform gateway configs have "PUBLIC" tenant id in prodcenter.
        return generateParserBeanName(url, Constant.PLATFORM_TENANT_ID, roleType, interfaceType);
    }

    /**
     * 生成beanName
     */
    public static String generateParserBeanName(MessageProcessConfig processConfig) {
        return StringUtils.joinWith(
                "_",
                "PARSER",
                StringUtils.lowerCase(processConfig.getParserClassName()),
                StringUtils.lowerCase(processConfig.getMessageProcessId()));
    }

    public static String generateAssembleBeanName(MessageProcessConfig processConfig) {
        return StringUtils.joinWith(
                "_",
                "ASSEMBLER",
                StringUtils.lowerCase(processConfig.getAssembleClassName()),
                StringUtils.lowerCase(processConfig.getMessageProcessId()));
    }

    /**
     * 生成beanName
     */
    public static String generateParserBeanName(String url, String tntInstId, String roleType, String interfaceType) {
        return StringUtils.lowerCase(StringUtils.join(
                new String[]{UriUtil.getUriWithDot(url), roleType, interfaceType, tntInstId}, PATH_CONJUNCTION));
    }

    /**
     * 
     */
    @Resource
    public void setInterfaceProdCenterQueryService(InterfaceProdCenterQueryService interfaceProdCenterQueryService) {
        GroovyScriptCache.interfaceProdCenterQueryService = interfaceProdCenterQueryService;
    }
}
