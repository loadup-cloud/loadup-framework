/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.gateway.plugin.repository.file;

/*-
 * #%L
 * repository-file-plugin
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
import com.github.loadup.components.extension.annotation.Extension;
import com.github.loadup.components.gateway.cache.common.DefaultGatewayConfigs;
import com.github.loadup.components.gateway.certification.util.CommonUtil;
import com.github.loadup.components.gateway.core.common.GatewayErrorCode;
import com.github.loadup.components.gateway.facade.extpoint.RepositoryServiceExt;
import com.github.loadup.components.gateway.facade.model.*;
import com.github.loadup.components.gateway.facade.util.CsvUtil;
import com.github.loadup.components.gateway.plugin.repository.file.config.*;
import com.github.loadup.components.gateway.plugin.repository.file.model.*;
import com.github.loadup.components.gateway.plugin.repository.file.service.impl.ConfigFileBuilderImpl;
import io.vavr.control.Try;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.URL;
import java.util.*;

/**
 * <p>
 * FileRepositoryService.java
 * </p>
 */
@Extension(bizCode = "FILE")
@Component("loadUpFileRepositoryExtPt")
@Order(-1)
@Slf4j
public class FileRepositoryExt implements RepositoryServiceExt, ApplicationListener<ApplicationStartedEvent> {

    // 组装脚本存在的路径
    private static Map<String, String> assembleTemplateCache = new HashMap<>();
    // groovy java 脚本存在的路径
    private static Map<String, String> parseTemplateCache = new HashMap<>();
    /**
     * 入参集合
     **/
    // 保存证书信息的文件内容
    public List<CertConfigRepository> certConfigRepositoryList = new ArrayList<>();
    /**
     * 结果集合
     **/
    private List<CertConfigDto> certConfigDtoList = new ArrayList<>();

    private List<CertAlgorithmConfigDto> certAlgorithmConfigDtoList = new ArrayList<>();

    private List<InterfaceConfigDto> interfaceConfigDtoList = new ArrayList<>();

    private List<CommunicationConfigDto> communicationConfigDtoList = new ArrayList<>();

    private List<MessageProcessConfigDto> messageProcessConfigDtoList = new ArrayList<>();

    private List<MessageReceiverConfigDto> messageReceiverConfigDtoList = new ArrayList<>();

    private List<MessageSenderConfigDto> messageSenderConfigDtoList = new ArrayList<>();

    private List<InstConfigDto> instConfigDtoList = new ArrayList<>();

    private List<InstInterfaceConfigDto> instInterfaceConfigDtoList = new ArrayList<>();

    /**
     * 需要引用的资源
     */
    @Resource
    private ConfigFileBuilderImpl configFileBuilder;

    @Resource
    @Qualifier("gatewayFileCertAlgorithmConfigBuilder")
    private CertAlgorithmConfigBuilder certAlgorithmConfigBuilder;

    @Resource
    @Qualifier("gatewayFileCertConfigBuilder")
    private CertConfigBuilder certConfigBuilder;

    @Resource
    @Qualifier("gatewayFileMessageSenderConfigBuilder")
    private MessageSenderConfigBuilder messageSenderConfigBuilder;

    @Resource
    @Qualifier("gatewayFileInterfaceConfigBuilder")
    private InterfaceConfigBuilder interfaceConfigBuilder;

    @Resource
    @Qualifier("gatewayFileMessageProcessConfigBuilder")
    private MessageProcessConfigBuilder messageProcessConfigBuilder;

    @Resource
    @Qualifier("gatewayFileCommunicationConfigBuilder")
    private CommunicationConfigBuilder communicationConfigBuilder;

    @Resource
    @Qualifier("gatewayFileMessageReceiverConfigBuilder")
    private MessageReceiverConfigBuilder messageReceiverConfigBuilder;

    /**
     * Init.
     */
    @Override
    public void onApplicationEvent(ApplicationStartedEvent e) {
        initCertFile();
        initTemplate();
        initInterface();
    }

    @Override
    public void saveOrUpdateInterface(InterfaceDto interfaceDto) {
        throw new CommonException(
                GatewayErrorCode.UNKNOWN_EXCEPTION, "fileRepositoryService not support saveOrUpdateInterface method");
    }

    /**
     * Load cert config list.
     */
    public List<CertConfigDto> loadCertConfig() {
        return this.certConfigDtoList;
    }

    /**
     * Load cert algorithm config list.
     */
    public List<CertAlgorithmConfigDto> loadCertAlgorithmConfig() {
        return this.certAlgorithmConfigDtoList;
    }

    /**
     * Load interface config list.
     */
    public List<InterfaceConfigDto> loadInterfaceConfig() {
        return this.interfaceConfigDtoList;
    }

    /**
     * Load message process config list.
     */
    public List<MessageProcessConfigDto> loadMessageProcessConfig() {
        return this.messageProcessConfigDtoList;
    }

    /**
     * Load communication config list.
     */
    public List<CommunicationConfigDto> loadCommunicationConfig() {
        return this.communicationConfigDtoList;
    }

    /**
     * Load message receiver config list.
     */
    public List<MessageReceiverConfigDto> loadMessageReceiverConfig() {
        return this.messageReceiverConfigDtoList;
    }

    /**
     * Load message sender config list.
     */
    public List<MessageSenderConfigDto> loadMessageSenderConfig() {
        return this.messageSenderConfigDtoList;
    }

    /**
     * Load message sender config list.
     */
    public List<InstConfigDto> loadInstConfig() {
        return this.instConfigDtoList;
    }

    /**
     * Load message sender config list.
     */
    public List<InstInterfaceConfigDto> loadInstInterfaceConfig() {
        return this.instInterfaceConfigDtoList;
    }

    @Override
    public void addInterface(InterfaceDto dto) {
        throw new CommonException(
                GatewayErrorCode.UNKNOWN_EXCEPTION, "fileRepositoryService not support add interface");
    }

    @Override
    public void updateInterface(InterfaceDto dto) {
        throw new CommonException(
                GatewayErrorCode.UNKNOWN_EXCEPTION, "fileRepositoryService not support update interface");
    }

    @Override
    public List<InterfaceDto> queryInterface(
            Integer pageSize,
            Integer page,
            String tntInstId,
            String interfaceId,
            String clientId,
            String type,
            String status,
            String interfaceName) {
        throw new CommonException(
                GatewayErrorCode.UNKNOWN_EXCEPTION, "fileRepositoryService not support query interface");
    }

    @Override
    public void removeInterface(String interfaceId) {
        throw new CommonException(
                GatewayErrorCode.UNKNOWN_EXCEPTION, "fileRepositoryService not support remove interface");
    }

    @Override
    public void upgradeInterface(InterfaceDto dto) {
        throw new CommonException(
                GatewayErrorCode.UNKNOWN_EXCEPTION, "fileRepositoryService not support upgrade interface");
    }

    @Override
    public void onlineInterface(String interfaceId) {
        throw new CommonException(
                GatewayErrorCode.UNKNOWN_EXCEPTION, "fileRepositoryService not support online interface");
    }

    @Override
    public void offlineInterface(String interfaceId) {
        throw new CommonException(
                GatewayErrorCode.UNKNOWN_EXCEPTION, "fileRepositoryService not support offline interface");
    }

    @Override
    public String addClient(ClientConfigDto clientConfigAddDto) {
        throw new CommonException(GatewayErrorCode.SYSTEM_EXCEPTION, "fileRepositoryService not support addmethod");
    }

    @Override
    public void authorizeClient(ClientInterfaceConfigDto clientConfigAuthorizeDto) {
        throw new CommonException(
                GatewayErrorCode.SYSTEM_EXCEPTION, "fileRepositoryService not support authrizemethod");
    }

    @Override
    public void deauthorizeClient(ClientInterfaceConfigDto clientConfigDeauthorizeDto) {
        throw new CommonException(
                GatewayErrorCode.SYSTEM_EXCEPTION, "fileRepositoryService not support deauthrizemethod");
    }

    @Override
    public void updateClient(ClientConfigDto clientConfigUpdateDto) {
        throw new CommonException(GatewayErrorCode.SYSTEM_EXCEPTION, "fileRepositoryService not support updatemethod");
    }

    @Override
    public ClientConfigDto queryClient(String clientId) {
        throw new CommonException(GatewayErrorCode.SYSTEM_EXCEPTION, "fileRepositoryService not support querymethod");
    }

    @Override
    public int removeClient(String clientId) {
        throw new CommonException(GatewayErrorCode.SYSTEM_EXCEPTION, "fileRepositoryService not support removemethod");
    }

    @Override
    public SecurityConfigDto addSecurity(SecurityConfigDto dto) {
        throw new CommonException(GatewayErrorCode.SYSTEM_EXCEPTION, "fileRepositoryService not support addmethod");
    }

    @Override
    public void updateSecurity(SecurityConfigDto request) {
        throw new CommonException(GatewayErrorCode.SYSTEM_EXCEPTION, "fileRepositoryService not support updatemethod");
    }

    @Override
    public List<SecurityConfigDto> querySecurityByClient(String clientId) {
        throw new CommonException(GatewayErrorCode.SYSTEM_EXCEPTION, "fileRepositoryService not support querymethod");
    }

    @Override
    public int removeSecurity(SecurityConfigDto certCode) {
        throw new CommonException(GatewayErrorCode.SYSTEM_EXCEPTION, "fileRepositoryService not support removemethod");
    }

    /**
     * Init interface.
     */
    public void initInterface() {
        String rootPath = DefaultGatewayConfigs.get("configRootPath");
        String spiPath = DefaultGatewayConfigs.get("spiConfigFilePath");
        String apiPath = DefaultGatewayConfigs.get("openapiConfigFilePath");

        Try.run(() -> {
            List<ApiConfigRepository> apiConfig = CsvUtil.readCSVFile(configFileBuilder.buildFilePath(rootPath, apiPath),
                    ApiConfigRepository.class);
            buildAPIConfig(apiConfig);

            List<SpiConfigRepository> spiConfig = CsvUtil.readCSVFile(configFileBuilder.buildFilePath(rootPath, spiPath),
                    SpiConfigRepository.class);
            buildSPIConfig(spiConfig);

        }).onFailure((e) -> {
            log.error("failed reading csv file {}", e);

        });

    }

    /**
     * Init template.
     */
    public void initTemplate() {
        String rootPath = DefaultGatewayConfigs.get("configRootPath");
        String assembleFilePath = DefaultGatewayConfigs.get("assembleTemplateFileDirectory");
        String parseFilePath = DefaultGatewayConfigs.get("parseTemplateFileDirectory");
        Map<String, String> assembleMap = new HashMap<>();
        Map<String, String> parseMap = new HashMap<>();
        String assemblePath = configFileBuilder.buildFilePath(rootPath, assembleFilePath);
        String parsePath = configFileBuilder.buildFilePath(rootPath, parseFilePath);
        log.info("assemblePath config path is {}", assemblePath);
        log.info("parsePath config path is {}", parsePath);
        readJarTemplates(assembleFilePath, parseFilePath);

        File assembleFile = new File(assemblePath);
        File parsePathFile = new File(parsePath);
        if (assembleFile.exists() && parsePathFile.exists()) {
            assembleMap = configFileBuilder.readToStringForDirectory(assembleFile);
            parseMap = configFileBuilder.readToStringForDirectory(parsePathFile);
        }
        assembleTemplateCache.putAll(assembleMap);
        parseTemplateCache.putAll(parseMap);
    }

    private void readJarTemplates(String assembleFilePath, String parseFilePath) {
        ClassLoader classLoader = this.getClass().getClassLoader();
        URL assembleResource = classLoader.getResource(assembleFilePath);
        if (Objects.nonNull(assembleResource)) {
            Map<String, String> assembleTemplateMap = configFileBuilder.readToStringForDirectory(assembleResource.getPath(),
                    assembleFilePath);
            assembleTemplateCache.putAll(assembleTemplateMap);
        }
        URL parseResource = classLoader.getResource(assembleFilePath);
        if (Objects.nonNull(parseResource)) {
            Map<String, String> parseTemplateMap = configFileBuilder.readToStringForDirectory(parseResource.getPath(), parseFilePath);
            parseTemplateCache.putAll(parseTemplateMap);
        }

    }

    /**
     * 加载certConf的文件
     */
    public void initCertFile() {
        String rootPath = DefaultGatewayConfigs.get("configRootPath");
        String filePath = DefaultGatewayConfigs.get("certAlgorithmConfigFilePath");
        List<String> lines = new ArrayList<String>();
        try {
            lines = configFileBuilder.readToStringList(rootPath, filePath);
            // 4. compute the config model from the 2nd line of content
            // covert to the entry model
            List<String> bodyLines = lines.subList(1, lines.size());
            bodyLines.forEach(item -> {
                CertConfigRepository certConfigRepository = new CertConfigRepository(item);
                certConfigRepositoryList.add(certConfigRepository);
            });

            certConfigRepositoryList.forEach(certConfigRepository -> {
                // 获取certConfig
                CertConfigDto certConfigDto = certConfigBuilder.buildDto(certConfigRepository);
                if (null == certConfigDto) {
                    log.error(
                            "failed building certConfig, securityStrategyCode: ",
                            certConfigRepository.getSecurityStrategyCode());
                } else {
                    this.certConfigDtoList.add(certConfigDto);
                }

                CertAlgorithmConfigDto certAlgorithmConfigDto = certAlgorithmConfigBuilder.build(certConfigRepository);
                if (null == certAlgorithmConfigDto) {
                    log.error(
                            "failed building certAlgorithmConfig, securityStrategyCode: ",
                            certConfigRepository.getSecurityStrategyCode());
                } else {
                    this.certAlgorithmConfigDtoList.add(certAlgorithmConfigDto);
                }
            });

        } catch (Exception e) {
            log.error("init fail", e);
        }
    }

    private void buildAPIConfig(List<ApiConfigRepository> apiConfigs) {

        apiConfigs.forEach(apiConfig -> {
            messageSenderConfigDtoList.addAll(messageSenderConfigBuilder.build(apiConfig));
            interfaceConfigDtoList.addAll(interfaceConfigBuilder.build(apiConfig));
            messageReceiverConfigDtoList.addAll(messageReceiverConfigBuilder.build(apiConfig));

            messageProcessConfigBuilder.setAssembleTemplateCache(assembleTemplateCache);
            messageProcessConfigBuilder.setParseTemplateCache(parseTemplateCache);
            messageProcessConfigDtoList.addAll(messageProcessConfigBuilder.build(apiConfig));

            communicationConfigDtoList.addAll(communicationConfigBuilder.build(apiConfig));
        });

    }

    /**
     *
     */
    @Deprecated
    private void buildConfig(String url, String securityStrategyCode,
                             String integrationServiceRequestHeaderAssemble, String integrationServiceRequestAssemble,
                             String integrationServiceResponseParser, String communicationProperties,
                             String integrationInterfaceId, int index) {

        Map<String, String> propertyMap = CommonUtil.Str2Kv(communicationProperties);

        MessageReceiverConfigDto msgReceiver = messageReceiverConfigBuilder.build(url, securityStrategyCode);
        if (null != msgReceiver) {
            messageReceiverConfigDtoList.add(msgReceiver);
        }

        String interfaceId = propertyMap.get("INTERFACE_ID");

        InterfaceConfigDto interfaceConfigDto = interfaceConfigBuilder.build(
                url, securityStrategyCode, communicationProperties, integrationInterfaceId);
        if (StringUtils.isNotBlank(interfaceId)) {
            interfaceConfigDto.setInterfaceId(interfaceId);
            interfaceConfigDto.setMessageProcessId(interfaceId);
        }
        if (null != interfaceConfigDto) {
            interfaceConfigDtoList.add(interfaceConfigDto);
        }

        messageProcessConfigBuilder.setAssembleTemplateCache(assembleTemplateCache);
        messageProcessConfigBuilder.setParseTemplateCache(parseTemplateCache);
        MessageProcessConfigDto messageProcessConfigDto = messageProcessConfigBuilder.build(
                url,
                securityStrategyCode,
                integrationServiceRequestHeaderAssemble,
                integrationServiceRequestAssemble,
                integrationServiceResponseParser);
        if (StringUtils.isNotBlank(interfaceId)) {
            messageProcessConfigDto.setMessageProcessId(interfaceId);
        }
        if (null != messageProcessConfigDto) {
            messageProcessConfigDtoList.add(messageProcessConfigDto);
        }

        CommunicationConfigDto communicationConfigDto =
                communicationConfigBuilder.build(url, securityStrategyCode, communicationProperties);
        if (StringUtils.isNotBlank(interfaceId)) {
            communicationConfigDto.setInterfaceId(interfaceId);
        }
        if (null != communicationConfigDto) {
            communicationConfigDtoList.add(communicationConfigDto);
        }
    }

    private void buildSPIConfig(List<SpiConfigRepository> spiConfigs) {
        int index = 0;
        for (SpiConfigRepository spiConfig : spiConfigs) {
            // index 0
            String integrationUri = spiConfig.getIntegrationUri();
            // index 1
            String securityStrategyCode = spiConfig.getSecurityStrategyCode();
            // index 2
            String integrationHeaderAssemble = spiConfig.getHeaderAssembler();
            // index 3
            String integrationAssemble = spiConfig.getBodyAssembler();
            // index 4
            String integrationParser = spiConfig.getResponseParser();
            // index 5
            String communicationProperties = spiConfig.getCommunicationProperties();
            buildConfig(
                    integrationUri,
                    securityStrategyCode,
                    integrationHeaderAssemble,
                    integrationAssemble,
                    integrationParser,
                    communicationProperties,
                    null,
                    index);
            index++;
        }
    }
}
