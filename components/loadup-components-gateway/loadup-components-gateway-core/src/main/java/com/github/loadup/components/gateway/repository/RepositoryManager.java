/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.gateway.repository;

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

import com.github.loadup.components.extension.core.BizScenario;
import com.github.loadup.components.extension.exector.ExtensionExecutor;
import com.github.loadup.components.gateway.cache.common.DefaultGatewayConfigs;
import com.github.loadup.components.gateway.certification.cache.CacheUtil;
import com.github.loadup.components.gateway.common.convertor.*;
import com.github.loadup.components.gateway.common.util.UriUtil;
import com.github.loadup.components.gateway.core.common.Constant;
import com.github.loadup.components.gateway.core.common.cache.CacheName;
import com.github.loadup.components.gateway.core.model.*;
import com.github.loadup.components.gateway.core.model.CertConfig;
import com.github.loadup.components.gateway.facade.extpoint.RepositoryServiceExt;
import com.github.loadup.components.gateway.facade.model.*;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * <p>
 * RepositoryManager.java
 * </p>
 */
@Slf4j
@Component("repositoryManager")
public class RepositoryManager {


    @Resource
    private ExtensionExecutor extensionExecutor;

    public void saveOrUpdateInterface(InterfaceDto interfaceDto) {
        String bizCode = DefaultGatewayConfigs.get(Constant.REPOSITORY_EXTPOINT_BIZCODE);
        extensionExecutor.executeVoid(
                RepositoryServiceExt.class,
                BizScenario.valueOf(bizCode),
                ext -> ext.saveOrUpdateInterface(interfaceDto));
    }

    /**
     * load the repository
     */
    public Map<CacheName, List> load() {
        Map<CacheName, List> loadResult = new HashMap<>(7);

        List<MessageReceiverConfig> messageReceiverConfigList = getMessageReceiverConfigList();
        List<MessageSenderConfig> messageSenderConfigList = getMessageSenderConfigList();
        List<CertConfig> certConfigList = getCertConfigList();
        List<CertAlgorithmConfig> certAlgorithmConfigList = getCertAlgorithmConfigList();
        List<InterfaceConfig> interfaceConfigList = getInterfaceConfigList();
        List<MessageProcessConfig> messageProcessConfigList = getMessageProcessConfigList();
        List<CommunicationConfig> communicationConfigList = getCommunicationConfigList();
        List<InstConfig> instConfigList = getInstConfigList();

        loadResult.put(CacheName.INTERFACE, interfaceConfigList);
        loadResult.put(CacheName.MESSAGE_PROCESS, messageProcessConfigList);
        loadResult.put(CacheName.COMMUNICATION, communicationConfigList);
        loadResult.put(CacheName.MESSAGE_RECEIVER, messageReceiverConfigList);
        loadResult.put(CacheName.MESSAGE_SENDER, messageSenderConfigList);
        loadResult.put(CacheName.CERT_CONFIG, certConfigList);
        loadResult.put(CacheName.CERT_ALGO_CONFIG, certAlgorithmConfigList);
        loadResult.put(CacheName.INST, instConfigList);

        return loadResult;
    }

    /**
     * load the repository
     */
    public Map<CacheName, List> loadByInterfaceId(String interfaceId) {
        Map<CacheName, List> loadResult = new HashMap<>(7);

        List<MessageReceiverConfig> messageReceiverConfigList = getMessageReceiverConfigList();
        List<MessageSenderConfig> messageSenderConfigList = getMessageSenderConfigList();
        List<InterfaceConfig> interfaceConfigList = getInterfaceConfigList();
        List<MessageProcessConfig> messageProcessConfigList = getMessageProcessConfigList();
        List<CommunicationConfig> communicationConfigList = getCommunicationConfigList();

        loadResult.put(CacheName.INTERFACE, interfaceConfigList);
        loadResult.put(CacheName.MESSAGE_PROCESS, messageProcessConfigList);
        loadResult.put(CacheName.COMMUNICATION, communicationConfigList);
        loadResult.put(CacheName.MESSAGE_RECEIVER, messageReceiverConfigList);
        loadResult.put(CacheName.MESSAGE_SENDER, messageSenderConfigList);

        return loadResult;
    }

    /**
     * load the repository
     */
    public Map<CacheName, List> loadCertByClientId(String clientId) {
        Map<CacheName, List> loadResult = new HashMap<>(7);

        List<CertConfig> certConfigList = getCertConfigList();
        List<CertAlgorithmConfig> certAlgorithmConfigList = getCertAlgorithmConfigList();

        loadResult.put(CacheName.CERT_CONFIG, certConfigList);
        loadResult.put(CacheName.CERT_ALGO_CONFIG, certAlgorithmConfigList);

        return loadResult;
    }

    private List<CertAlgorithmConfig> getCertAlgorithmConfigList() {
        String bizCode = DefaultGatewayConfigs.get(Constant.REPOSITORY_EXTPOINT_BIZCODE);
        List<CertAlgorithmConfigDto> tempItems = extensionExecutor.execute(
                RepositoryServiceExt.class,
                BizScenario.valueOf(bizCode),
                RepositoryServiceExt::loadCertAlgorithmConfig);

        List<CertAlgorithmConfig> certAlgorithmConfigList = null;
        if (CollectionUtils.isNotEmpty(tempItems)) {
            certAlgorithmConfigList = new ArrayList<>(tempItems.size());
            for (CertAlgorithmConfigDto item : tempItems) {
                CertAlgorithmConfig certAlgorithmConfig = new CertAlgorithmConfig();
                if (StringUtils.isBlank(item.getCertCode())) {
                    String securityCode = item.getSecurityStrategyCode();
                    String operateType = item.getOperateType();
                    String algorithm = item.getAlgoName();
                    String clientId = item.getClientId();
                    String certCode = CacheUtil.generateKey(securityCode, operateType, algorithm, clientId);
                    item.setCertCode(certCode);
                }
                certAlgorithmConfig.setCertCode(item.getCertCode());
                certAlgorithmConfig.setOperateType(item.getOperateType());
                certAlgorithmConfig.setAlgoName(item.getAlgoName());
                certAlgorithmConfig.setCertTypes(item.getCertType());
                certAlgorithmConfig.setAlgoProperties(item.getAlgoProperties());
                certAlgorithmConfig.setAlgoType(item.getAlgoType());
                certAlgorithmConfigList.add(certAlgorithmConfig);
            }
        }
        return certAlgorithmConfigList;
    }

    private List<CertConfig> getCertConfigList() {
        String bizCode = DefaultGatewayConfigs.get(Constant.REPOSITORY_EXTPOINT_BIZCODE);
        List<CertConfigDto> tempItems = extensionExecutor.execute(
                RepositoryServiceExt.class, BizScenario.valueOf(bizCode), RepositoryServiceExt::loadCertConfig);

        List<CertConfig> certConfigList = null;
        if (CollectionUtils.isNotEmpty(tempItems)) {
            certConfigList = new ArrayList<>();
            for (CertConfigDto item : tempItems) {
                CertConfig certConfig = new CertConfig();
                if (StringUtils.isBlank(item.getCertCode())) {
                    String securityCode = item.getSecurityStrategyCode();
                    String operateType = item.getOperateType();
                    String algorithm = item.getAlgoName();
                    String clientId = item.getClientId();
                    String certCode = CacheUtil.generateKey(securityCode, operateType, algorithm, clientId);
                    item.setCertCode(certCode);
                }
                certConfig.setCertCode(item.getCertCode());
                certConfig.setCertType(item.getCertType());
                certConfig.setCertContent(item.getCertContent());
                certConfig.setCertStatus(item.getCertStatus());
                certConfig.setGmtInValid(item.getGmtInValid());
                certConfig.setGmtValid(item.getGmtValid());
                certConfig.setCertSpecial(item.getProperties());
                certConfigList.add(certConfig);
            }
        }
        return certConfigList;
    }

    private List<MessageSenderConfig> getMessageSenderConfigList() {
        String bizCode = DefaultGatewayConfigs.get(Constant.REPOSITORY_EXTPOINT_BIZCODE);
        List<MessageSenderConfigDto> tempItems = extensionExecutor.execute(
                RepositoryServiceExt.class,
                BizScenario.valueOf(bizCode),
                RepositoryServiceExt::loadMessageSenderConfig);

        List<MessageSenderConfig> messageSenderConfigList = null;
        if (CollectionUtils.isNotEmpty(tempItems)) {
            messageSenderConfigList = MessageSenderConfigConvertor.dtoList2ModelList(tempItems);
        }
        return messageSenderConfigList;
    }

    private List<MessageReceiverConfig> getMessageReceiverConfigList() {
        String bizCode = DefaultGatewayConfigs.get(Constant.REPOSITORY_EXTPOINT_BIZCODE);
        List<MessageReceiverConfigDto> tempItems = extensionExecutor.execute(
                RepositoryServiceExt.class,
                BizScenario.valueOf(bizCode),
                RepositoryServiceExt::loadMessageReceiverConfig);
        List<MessageReceiverConfig> messageReceiverConfigList = null;
        if (CollectionUtils.isNotEmpty(tempItems)) {
            messageReceiverConfigList = MessageReceiverConfigConvertor.dtoList2ModelList(tempItems);
        }
        return messageReceiverConfigList;
    }

    private List<CommunicationConfig> getCommunicationConfigList() {

        String bizCode = DefaultGatewayConfigs.get(Constant.REPOSITORY_EXTPOINT_BIZCODE);
        List<CommunicationConfigDto> tempItems = extensionExecutor.execute(
                RepositoryServiceExt.class,
                BizScenario.valueOf(bizCode),
                RepositoryServiceExt::loadCommunicationConfig);

        List<CommunicationConfig> communicationConfigList = null;
        if (CollectionUtils.isNotEmpty(tempItems)) {
            communicationConfigList = CommunicationConfigConvertor.dtoList2ModelList(tempItems);
        }
        return communicationConfigList;
    }

    /**
     * get inst config with interface list
     */
    private List<InstConfig> getInstConfigList() {

        String bizCode = DefaultGatewayConfigs.get(Constant.REPOSITORY_EXTPOINT_BIZCODE);
        List<InstConfigDto> tempItems = extensionExecutor.execute(
                RepositoryServiceExt.class, BizScenario.valueOf(bizCode), RepositoryServiceExt::loadInstConfig);
        List<InstInterfaceConfigDto> instInterfaceConfigDtos = extensionExecutor.execute(
                RepositoryServiceExt.class,
                BizScenario.valueOf(bizCode),
                RepositoryServiceExt::loadInstInterfaceConfig);

        List<InstConfig> instConfigList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(tempItems)) {
            instConfigList = InstConfigConvertor.dtoList2ModelList(tempItems);
        }

        Map<String, InterfaceConfig> interfaceMap = new HashMap<>();
        List<InterfaceConfig> interfaceConfigs = getInterfaceConfigList();
        interfaceConfigs.forEach(interfaceConfig -> {
            interfaceMap.put(interfaceConfig.getInterfaceId(), interfaceConfig);
        });

        Map<String, InstConfig> instConfigMap = new HashMap<>();

        instConfigList.forEach(instConfig -> {
            instConfigMap.put(instConfig.getClientId(), instConfig);
        });

        instInterfaceConfigDtos.forEach(instInterfaceConfigDto -> {
            String clientId = instInterfaceConfigDto.getClientId();
            String interfaceId = generateBizKey(instInterfaceConfigDto.getInterfaceId());
            instConfigMap.get(clientId).getInterfaceMap().put(interfaceId, interfaceMap.get(interfaceId));
        });

        return instConfigList;
    }

    /**
     * generate interface id biz key
     */
    public String generateBizKey(String url) {
        return StringUtils.replace(UriUtil.getURIPath(url), Constant.PATH_SEPARATOR, Constant.PATH_CONJUNCTION);
    }

    private List<MessageProcessConfig> getMessageProcessConfigList() {
        String bizCode = DefaultGatewayConfigs.get(Constant.REPOSITORY_EXTPOINT_BIZCODE);
        List<MessageProcessConfigDto> tempItems = extensionExecutor.execute(
                RepositoryServiceExt.class,
                BizScenario.valueOf(bizCode),
                RepositoryServiceExt::loadMessageProcessConfig);

        List<MessageProcessConfig> messageProcessConfigList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(tempItems)) {
            messageProcessConfigList = MessageProcessConfigConvertor.dtoList2ModelList(tempItems);
        }
        return messageProcessConfigList;
    }

    private List<InterfaceConfig> getInterfaceConfigList() {
        String bizCode = DefaultGatewayConfigs.get(Constant.REPOSITORY_EXTPOINT_BIZCODE);
        List<InterfaceConfigDto> tempItems = extensionExecutor.execute(
                RepositoryServiceExt.class,
                BizScenario.valueOf(bizCode),
                RepositoryServiceExt::loadInterfaceConfig);

        List<InterfaceConfig> interfaceConfigList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(tempItems)) {
            interfaceConfigList = InterfaceConfigConvertor.dtoList2ModelList(tempItems);
        }
        return interfaceConfigList;
    }
}
