package com.github.loadup.components.gateway.cache;

import com.github.loadup.components.gateway.common.convertor.CommunicationConfigConvertor;
import com.github.loadup.components.gateway.common.exception.GatewayException;
import com.github.loadup.components.gateway.common.exception.util.AssertUtil;
import com.github.loadup.components.gateway.common.util.CacheLogUtil;
import com.github.loadup.components.gateway.common.util.RepositoryUtil;
import com.github.loadup.components.gateway.core.common.GatewayliteErrorCode;
import com.github.loadup.components.gateway.core.common.enums.*;
import com.github.loadup.components.gateway.core.model.CommunicationConfig;
import com.github.loadup.components.gateway.core.prototype.util.SupergwGatewayConfigurationUtils;
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
 * CommunicationCache.java
 * </p>
 */
@Component("gatewayCommunicationConfigCache")
public class CommunicationConfigCache {

    /**
     * key is url
     */
    private static Map<String, CommunicationConfig> communicationConfigMapWithInterface = new ConcurrentHashMap<>();

    /**
     * key is interface
     */
    private static Map<String, CommunicationConfig> communicationConfigMapWithUrl = new ConcurrentHashMap<>();

    /**
     * interface product center query service
     */
    private static InterfaceProdCenterQueryService interfaceProdCenterQueryService;

    /**
     * Create Cache
     */
    public static void putAll(boolean clear, List<CommunicationConfig> communicationConfigList) {
        if (CollectionUtils.isEmpty(communicationConfigList)) {
            return;
        }

        Map<String, CommunicationConfig> tempMapWithInterface = new ConcurrentHashMap<String, CommunicationConfig>();
        Map<String, CommunicationConfig> tempMapWithUrl = new ConcurrentHashMap<String, CommunicationConfig>();

        communicationConfigList.forEach(communicationConfig -> {
            String uriString = communicationConfig.getUrl();
            uriString = SupergwGatewayConfigurationUtils.getStrBeforeCharset(uriString, "?");
            tempMapWithUrl.put(uriString, communicationConfig);
            tempMapWithInterface.put(communicationConfig.getInterfaceId(), communicationConfig);
        });
        if (clear) {
            communicationConfigMapWithInterface = tempMapWithInterface;
            communicationConfigMapWithUrl = tempMapWithUrl;
        } else {
            communicationConfigMapWithInterface.putAll(tempMapWithInterface);
            communicationConfigMapWithUrl.putAll(tempMapWithUrl);
        }
        CacheLogUtil.printLog("communicationConfigMapWithInterface", communicationConfigMapWithInterface);
        CacheLogUtil.printLog("communicationConfigMapWithUrl", communicationConfigMapWithUrl);
    }

    /**
     * Get with interface id
     */
    public static CommunicationConfig getWithInterfaceId(String interfaceId) {
        RepositoryType repositoryType = RepositoryUtil.getRepositoryType();
        if (repositoryType.isConfigInInternalCache()) {
            return communicationConfigMapWithInterface.get(interfaceId);
        }
        if (repositoryType == RepositoryType.PRODCENTER) {
            // will not use interfaceId to query config from PRODCENTER
            return null;
        }
        return null;
    }

    /**
     * Gets get with url.
     */
    public static CommunicationConfig getWithUrl(String url, String interfaceTypeStr) {
        RepositoryType repositoryType = RepositoryUtil.getRepositoryType();
        if (repositoryType.isConfigInInternalCache()) {
            return communicationConfigMapWithUrl.get(url);
        }
        if (repositoryType == RepositoryType.PRODCENTER) {
            InterfaceType interfaceType = InterfaceType.getEnumByCode(interfaceTypeStr);
            AssertUtil.isNotNull(interfaceType, GatewayliteErrorCode.CONFIGURATION_NOT_FOUND);
            switch (interfaceType) {
                case SPI:
                    // 传过来的url是接收方integrationUrl。想要获取接收方的CommunicationConfig
                    SPIConditionGroup spiConditionGroup = interfaceProdCenterQueryService
                            .querySPIConditionGroup(url);
                    return CommunicationConfigConvertor.convertToReceiverConfig(spiConditionGroup);
                case OPENAPI:
                    // 传过来的url是发送方url。想要获取发送方的CommunicationConfig
                    APIConditionGroup apiConditionGroup = interfaceProdCenterQueryService
                            .queryAPIConditionGroup(url, null);
                    return CommunicationConfigConvertor.convertToSenderConfig(apiConditionGroup);
                default:
                    break;
            }
        }
        return null;
    }

    /**
     * Gets get with url.
     */
    public static CommunicationConfig getFromProdCenterWithOpenUrls(RoleType roleType,
                                                                    String... openUrls) {
        CommunicationConfig result = null;
        if (RepositoryUtil.getRepositoryType() == RepositoryType.PRODCENTER) {
            for (String openUrl : openUrls) {
                APIConditionGroup apiConditionGroup = null;
                try {
                    apiConditionGroup = interfaceProdCenterQueryService
                            .queryAPIConditionGroup(openUrl, null);
                } catch (GatewayException ex) {
                    if (ex.getErrorCode() != GatewayliteErrorCode.CONFIGURATION_LOAD_ERROR) {
                        throw ex;
                    }
                }
                if (apiConditionGroup == null) {
                    continue;
                }
                if (roleType == RoleType.RECEIVER) {
                    result = CommunicationConfigConvertor
                            .convertToReceiverConfig(apiConditionGroup);
                } else {
                    result = CommunicationConfigConvertor.convertToSenderConfig(apiConditionGroup);
                }
            }
        }
        AssertUtil.isNotNull(result, GatewayliteErrorCode.CONFIGURATION_NOT_FOUND);
        return result;
    }

    /**
     * Setter method for property <tt>interfaceProdCenterQueryService</tt>.
     */
    @Resource
    public void setInterfaceProdCenterQueryService(InterfaceProdCenterQueryService interfaceProdCenterQueryService) {
        CommunicationConfigCache.interfaceProdCenterQueryService = interfaceProdCenterQueryService;
    }
}