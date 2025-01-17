package com.github.loadup.components.gateway.cache;

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
import com.github.loadup.components.gateway.common.convertor.InterfaceConfigConvertor;
import com.github.loadup.components.gateway.common.exception.util.AssertUtil;
import com.github.loadup.components.gateway.common.util.CacheLogUtil;
import com.github.loadup.components.gateway.common.util.RepositoryUtil;
import com.github.loadup.components.gateway.core.common.GatewayErrorCode;
import com.github.loadup.components.gateway.core.common.enums.InterfaceType;
import com.github.loadup.components.gateway.core.common.enums.RepositoryType;
import com.github.loadup.components.gateway.core.common.enums.RoleType;
import com.github.loadup.components.gateway.core.model.InterfaceConfig;
import com.github.loadup.components.gateway.core.service.InterfaceProdCenterQueryService;
import com.github.loadup.components.gateway.facade.config.model.APIConditionGroup;
import com.github.loadup.components.gateway.facade.config.model.SPIConditionGroup;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 * InterfaceCache.java
 * </p>
 */
@Component("interfaceConfigCache")
public class InterfaceConfigCache {

    private static Map<String, InterfaceConfig> interfaceConfigMap = new ConcurrentHashMap<>();

    /**
     * interface product center query service
     */
    private static InterfaceProdCenterQueryService interfaceProdCenterQueryService;

    /**
     * build the cache
     */
    public static void putAll(boolean clear, List<InterfaceConfig> interfaceConfigList) {
        if (CollectionUtils.isEmpty(interfaceConfigList)) {
            return;
        }

        Map<String, InterfaceConfig> tempMap = new ConcurrentHashMap<>();
        interfaceConfigList.forEach(interfaceConfig -> tempMap.put(interfaceConfig.getInterfaceId(), interfaceConfig));
        if (clear) {
            interfaceConfigMap = tempMap;
        } else {
            interfaceConfigMap.putAll(tempMap);
        }
        CacheLogUtil.printLog("interfaceConfigMap", interfaceConfigMap);
    }

    /**
     * get with interface Id
     */
    public static InterfaceConfig getWithInterfaceId(String interfaceId, RoleType roleType,
                                                     String interfaceTypeStr) {
        RepositoryType repositoryType = RepositoryUtil.getRepositoryType();
        if (repositoryType.isConfigInInternalCache()) {
            return interfaceConfigMap.get(interfaceId);
        }
        if (repositoryType == RepositoryType.PRODCENTER) {
            // will not use interfaceId to query config from PRODCENTER
            InterfaceType interfaceType = InterfaceType.getEnumByCode(interfaceTypeStr);
            AssertUtil.isNotNull(interfaceType, GatewayErrorCode.CONFIGURATION_NOT_FOUND);
            switch (interfaceType) {
                case SPI:
                    // 传过来的interfaceId是接收方integrationUrl。想要获取接收方的CommunicationConfig
                    SPIConditionGroup spiConditionGroup = interfaceProdCenterQueryService
                            .querySPIConditionGroup(interfaceId);
                    return InterfaceConfigConvertor.convertToReceiverConfig(spiConditionGroup);
                case OPENAPI:
                    if (roleType == RoleType.SENDER) {
                        // 传过来的url是发送方url。想要获取发送方的CommunicationConfig
                        APIConditionGroup apiConditionGroup = interfaceProdCenterQueryService
                                .queryAPIConditionGroup(interfaceId, null);
                        return InterfaceConfigConvertor.convertToSenderConfig(apiConditionGroup);
                    } else {
                        // 传过来的url是integrationUrl。想要获取接收方的CommunicationConfig
                        APIConditionGroup apiConditionGroup = interfaceProdCenterQueryService
                                .queryAPIConditionGroup(null, interfaceId);
                        return InterfaceConfigConvertor.convertToReceiverConfig(apiConditionGroup);
                    }
                default:
                    break;
            }
        }
        return null;
    }

    /**
     * get receiver config with open urls
     */
    public static InterfaceConfig getIntegratorConfigWithOpenUrls(String... openUrls) {
        InterfaceConfig result = null;
        if (RepositoryUtil.getRepositoryType() == RepositoryType.PRODCENTER) {
            for (String openUrl : openUrls) {
                APIConditionGroup apiConditionGroup = null;
                try {
                    apiConditionGroup = interfaceProdCenterQueryService
                            .queryAPIConditionGroup(openUrl, null);
                } catch (CommonException ex) {
                    if (ex.getResultCode() != GatewayErrorCode.CONFIGURATION_LOAD_ERROR) {
                        throw ex;
                    }
                }
                if (apiConditionGroup == null) {
                    continue;
                }
                result = InterfaceConfigConvertor.convertToReceiverConfig(apiConditionGroup);
            }
        }
        AssertUtil.isNotNull(result, GatewayErrorCode.CONFIGURATION_NOT_FOUND);
        return result;
    }

    /**
     * Setter method for property <tt>interfaceProdCenterQueryService</tt>.
     */
    @Resource
    public void setInterfaceProdCenterQueryService(InterfaceProdCenterQueryService interfaceProdCenterQueryService) {
        InterfaceConfigCache.interfaceProdCenterQueryService = interfaceProdCenterQueryService;
    }
}
