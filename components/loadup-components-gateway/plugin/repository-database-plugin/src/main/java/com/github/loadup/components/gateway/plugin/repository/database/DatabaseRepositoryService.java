package com.github.loadup.components.gateway.plugin.repository.database;

/*-
 * #%L
 * repository-database-plugin
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

import com.alibaba.cola.extension.Extension;
import com.github.loadup.commons.error.CommonException;
import com.github.loadup.components.gateway.common.exception.util.AssertUtil;
import com.github.loadup.components.gateway.common.util.InterfaceConfigUtil;
import com.github.loadup.components.gateway.core.common.GatewayErrorCode;
import com.github.loadup.components.gateway.facade.enums.InterfaceStatus;
import com.github.loadup.components.gateway.facade.enums.InterfaceType;
import com.github.loadup.components.gateway.facade.extpoint.RepositoryServiceExtPt;
import com.github.loadup.components.gateway.facade.model.*;
import com.github.loadup.components.gateway.facade.util.LogUtil;
import com.github.loadup.components.gateway.plugin.repository.database.config.*;
import com.github.loadup.components.gateway.plugin.repository.database.converter.*;
import com.github.loadup.components.gateway.plugin.repository.database.dal.daointerface.*;
import com.github.loadup.components.gateway.plugin.repository.database.dal.dataobject.*;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 *
 */
@Extension(bizId = "DATABASE")
@Component("loadUpDatabaseRepositoryExtPt")
@Order(-2)
public class DatabaseRepositoryService implements RepositoryServiceExtPt,
		ApplicationListener<ApplicationStartedEvent> {

	private static final Logger logger = LoggerFactory
			.getLogger(DatabaseRepositoryService.class);

	/**
	 * cert config dto list
	 */
	private List<CertConfigDto> certConfigDtoList = new ArrayList<>();

	/**
	 * cert algorithm config dto list
	 */
	private List<CertAlgorithmConfigDto> certAlgorithmConfigDtoList = new ArrayList<>();

	/**
	 * interface config dto list
	 */
	private List<InterfaceConfigDto> interfaceConfigDtoList = new ArrayList<>();

	/**
	 * communication config dto list
	 */
	private List<CommunicationConfigDto> communicationConfigDtoList = new ArrayList<>();

	/**
	 * message process config dto list
	 */
	private List<MessageProcessConfigDto> messageProcessConfigDtoList = new ArrayList<>();

	/**
	 * message receiver config dto list
	 */
	private List<MessageReceiverConfigDto> messageReceiverConfigDtoList = new ArrayList<>();

	/**
	 * message sender config dto list
	 */
	private List<MessageSenderConfigDto> messageSenderConfigDtoList = new ArrayList<>();

	/**
	 * inst config dto list
	 */
	private List<InstConfigDto> instConfigDtoList = new ArrayList<>();

	/**
	 * inst interface config dto list
	 */
	private List<InstInterfaceConfigDto> instInterfaceConfigDtoList = new ArrayList<>();

	/**
	 * interface DAO
	 */
	@Resource
	private InterfaceDAO gatewayInterfaceDAO;

	/**
	 * F
	 * inst DAO
	 */
	@Resource
	private InstDAO gatewayInstDAO;

	/**
	 * inst interface map DAo
	 */
	@Resource
	private InstInterfaceMapDAO gatewayInstInterfaceMapDAO;

	/**
	 * security DAo
	 */
	@Resource
	private SecurityDAO gatewaySecurityDAO;

	/**
	 * cert algorithm builder
	 */
	@Resource
	@Qualifier("databaseCertAlgorithmConfigBuilder")
	private CertAlgorithmConfigBuilder certAlgorithmConfigBuilder;

	/**
	 * cert builder
	 */
	@Resource
	@Qualifier("databaseCertConfigBuilder")
	private CertConfigBuilder certConfigBuilder;

	/**
	 * message sender builder
	 */
	@Resource
	@Qualifier("databaseMessageSenderConfigBuilder")
	private MessageSenderConfigBuilder messageSenderConfigBuilder;

	/**
	 * interface builder
	 */
	@Resource
	@Qualifier("databaseInterfaceConfigBuilder")
	private InterfaceConfigBuilder interfaceConfigBuilder;

	/**
	 * message process config builder
	 */
	@Resource
	@Qualifier("databaseMessageProcessConfigBuilder")
	private MessageProcessConfigBuilder messageProcessConfigBuilder;

	/**
	 * communication config builder
	 */
	@Resource
	@Qualifier("databaseCommunicationConfigBuilder")
	private CommunicationConfigBuilder communicationConfigBuilder;

	/**
	 * message receiver config builder
	 */
	@Resource
	@Qualifier("databaseMessageReceiverConfigBuilder")
	private MessageReceiverConfigBuilder messageReceiverConfigBuilder;

	/**
	 * inst config builder
	 */
	@Resource
	@Qualifier("databaseInstConfigBuilder")
	private InstConfigBuilder instConfigBuilder;

	/**
	 * inst interface config builder
	 */
	@Resource
	@Qualifier("databaseInstInterfaceConfigBuilder")
	private InstInterfaceConfigBuilder instInterfaceConfigBuilder;

	/**
	 * transaction template
	 */
	@Resource
	@Qualifier("transactionTemplate")
	private TransactionTemplate gatewayTransactionTemplate;

	/**
	 * Init.
	 */
	@Override
	public void onApplicationEvent(ApplicationStartedEvent e) {
		initCertFile();
		initInterface();
		initInst();
		initInstInterface();
		LogUtil.info(logger, "==database init done==");
	}

	/**
	 * Init interface.
	 */
	public void initByInterfaceId(String interfaceId) {

		InterfaceDO interfaceDO = gatewayInterfaceDAO.loadByInterfaceId(interfaceId);

		AssertUtil.isNotNull(interfaceDO, GatewayErrorCode.PARAM_ILLEGAL,
				"Interface is not exist!");

		// 1. build api config
		buildAPIConfig(Arrays.asList(interfaceDO));

		// 2. build spi config
		buildSPIConfig(Arrays.asList(interfaceDO));

	}

	/**
	 * Init cert.
	 */
	public void initCertByClientId(String clientId) {

		List<SecurityDO> securityDOS = gatewaySecurityDAO.loadByClientId(clientId);

		AssertUtil.isFalse(CollectionUtils.isEmpty(securityDOS), GatewayErrorCode.PARAM_ILLEGAL,
				"Cert configs is not exist!");

		try {

			securityDOS.forEach(securityDO -> {
				//获取certConfig
				CertConfigDto certConfigDto = certConfigBuilder.buildDto(securityDO);
				if (null == certConfigDto) {
					LogUtil.error(logger, "failed building certConfig, security_strategy_code: ",
							securityDO.getSecurityStrategyCode());
				} else {
					this.certConfigDtoList.add(certConfigDto);
				}

				CertAlgorithmConfigDto certAlgorithmConfigDto = certAlgorithmConfigBuilder
						.build(securityDO);
				this.certAlgorithmConfigDtoList.add(certAlgorithmConfigDto);
			});

		} catch (Exception e) {
			LogUtil.error(logger, e, "init fail");
		}
	}

	@Override
	public void saveOrUpdateInterface(InterfaceDto interfaceDto) {
		InterfaceDO interfaceDO = InterfaceConvertor.dto2DO(interfaceDto);
		try {
			gatewayInterfaceDAO.insert(interfaceDO);
		} catch (DataIntegrityViolationException e) {
			gatewayInterfaceDAO.update(interfaceDO);
		}
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
	 * Load inst config list.
	 */
	public List<InstConfigDto> loadInstConfig() {
		return this.instConfigDtoList;
	}

	/**
	 * Load inst interface config list.
	 */
	public List<InstInterfaceConfigDto> loadInstInterfaceConfig() {
		return this.instInterfaceConfigDtoList;
	}

	/**
	 *
	 */
	@Override
	public String addClient(ClientConfigDto clientConfigAddDto) {
		try {
			InstDO instDO = InstConvertor.dto2DO(clientConfigAddDto);
			return gatewayInstDAO.insert(instDO);
		} catch (DataIntegrityViolationException exception) {
			throw new CommonException(GatewayErrorCode.CLIENT_ALREADY_EXIST,
					GatewayErrorCode.CLIENT_ALREADY_EXIST.getMessage());
		}
	}

	/**
	 *
	 */
	@Override
	public void authorizeClient(ClientInterfaceConfigDto request) {
		gatewayTransactionTemplate.execute(transactionStatus -> {

			InstDO load = gatewayInstDAO.lock(request.getClientId());
			AssertUtil.isNotNull(load, GatewayErrorCode.CLIENT_NOT_EXIST);

			InterfaceDO interfaceDO = gatewayInterfaceDAO
					.lockByInterfaceId(request.getInterfaceId());
			AssertUtil.isNotNull(interfaceDO, GatewayErrorCode.INTERFACE_NOT_EXIST);

			try {
				gatewayInstInterfaceMapDAO.insert(InstInterfaceMapConvertor.Dto2DO(request));
			} catch (DataIntegrityViolationException exception) {
				// idempotent success
				LogUtil.debug(logger, "Interface has already authorized to client.", request);
			}
			return null;
		});
	}

	/**
	 *
	 */
	@Override
	public void deauthorizeClient(ClientInterfaceConfigDto request) {
		gatewayInstInterfaceMapDAO.delete(request.getClientId(), request.getInterfaceId());
	}

	/**
	 *
	 */
	@Override
	public void updateClient(ClientConfigDto request) {
		gatewayTransactionTemplate.execute(transactionStatus -> {
			// 1. lock client
			InstDO instDO = gatewayInstDAO.lock(request.getClientId());
			AssertUtil.isNotNull(instDO, GatewayErrorCode.CLIENT_NOT_EXIST);

			// 2. update client
			gatewayInstDAO.update(InstConvertor.dto2DO(request));
			return null;
		});
	}

	/**
	 *
	 */
	@Override
	public ClientConfigDto queryClient(String clientId) {
		InstDO instDO = gatewayInstDAO.load(clientId);
		return InstConvertor.DO2dto(instDO);
	}

	/**
	 *
	 */
	@Override
	public int removeClient(String clientId) {
		return gatewayInstDAO.delete(clientId);
	}

	/**
	 *
	 */
	@Override
	public SecurityConfigDto addSecurity(SecurityConfigDto request) {
		try {
			gatewaySecurityDAO.insert(SecurityConvertor.dto2DO(request));
			return request;
		} catch (DataIntegrityViolationException e) {
			throw new CommonException(GatewayErrorCode.SECURITY_ALREADY_EXIST,
					GatewayErrorCode.SECURITY_ALREADY_EXIST.getMessage());
		}
	}

	/**
	 *
	 */
	@Override
	public void updateSecurity(SecurityConfigDto request) {
		gatewayTransactionTemplate.execute(transactionStatus -> {
			// 1. lock
			SecurityDO securityDO = gatewaySecurityDAO.load(request.getClientId(),
					request.getSecurityStrategyCode(), request.getOperateType(), request.getAlgoName());
			AssertUtil.isNotNull(securityDO, GatewayErrorCode.SECURITY_NOT_EXIST);

			// 2. update
			return gatewaySecurityDAO.update(SecurityConvertor.dto2DO(request));
		});
	}

	/**
	 *
	 */
	@Override
	public List<SecurityConfigDto> querySecurityByClient(String clientId) {
		List<SecurityConfigDto> result = new ArrayList<>();
		List<SecurityDO> securityDOS = gatewaySecurityDAO.loadByClientId(clientId);

		if (!CollectionUtils.isEmpty(securityDOS)) {
			for (SecurityDO securityDO : securityDOS) {
				result.add(SecurityConvertor.DO2CertConfigInnerDto(securityDO));
			}
		}
		return result;
	}

	/**
	 *
	 */
	@Override
	public int removeSecurity(SecurityConfigDto request) {
		return gatewaySecurityDAO.delete(request.getClientId(),
				request.getSecurityStrategyCode(), request.getOperateType(), request.getAlgoName());
	}

	/**
	 * Init interface.
	 */
	public void initInterface() {
		List<InterfaceDO> interfaceDOS = gatewayInterfaceDAO.loadAll();

		// 1. build api configs
		buildAPIConfig(interfaceDOS);

		// 2. build spi configs
		buildSPIConfig(interfaceDOS);

	}

	/**
	 * Init inst.
	 */
	public void initInst() {
		List<InstDO> instDOS = gatewayInstDAO.loadAll();

		instDOS.forEach(instDO -> {
			//获取certConfig
			InstConfigDto instConfigDto = instConfigBuilder.build(instDO.getClientId(),
					instDO.getName(), instDO.getProperties());
			this.instConfigDtoList.add(instConfigDto);
		});
	}

	/**
	 * Init inst.
	 */
	public void initInstInterface() {
		List<InstInterfaceMapDO> instInterfaceDOS = gatewayInstInterfaceMapDAO.loadAll();

		instInterfaceDOS.forEach(instInterfaceDO -> {
			//获取certConfig
			InstInterfaceConfigDto instInterfaceConfigDto = instInterfaceConfigBuilder
					.build(instInterfaceDO.getClientId(), instInterfaceDO.getInterfaceId());
			instInterfaceConfigDtoList.add(instInterfaceConfigDto);
		});
	}

	/**
	 * 加载certConf的文件
	 */
	public void initCertFile() {
		try {
			List<SecurityDO> securityDOS = gatewaySecurityDAO.loadAll();

			securityDOS.forEach(securityDO -> {
				//获取certConfig
				CertConfigDto certConfigDto = certConfigBuilder.buildDto(securityDO);
				if (null == certConfigDto) {
					LogUtil.error(logger, "failed building certConfig, security_strategy_code: ",
							securityDO.getSecurityStrategyCode());
				} else {
					this.certConfigDtoList.add(certConfigDto);
				}

				CertAlgorithmConfigDto certAlgorithmConfigDto = certAlgorithmConfigBuilder
						.build(securityDO);
				this.certAlgorithmConfigDtoList.add(certAlgorithmConfigDto);
			});

		} catch (Exception e) {
			LogUtil.error(logger, e, "Init cert fail.");
		}

	}

	/**
	 * build api configs, include both inbound and outbound configs.
	 */
	private void buildAPIConfig(List<InterfaceDO> apiConfigs) {
		int index = 0;
		for (InterfaceDO apiConfig : apiConfigs) {
			if (InterfaceType.SPI == InterfaceType.getEnumByCode(apiConfig.getType())) {
				continue;
			}

			MessageSenderConfigDto msgSender = messageSenderConfigBuilder.build(apiConfig.getUrl(),
					apiConfig.getSecurityStrategyCode());
			if (null != msgSender) {
				messageSenderConfigDtoList.add(msgSender);
			}

			// 1. build outbound interface
			buildConfig(apiConfig.getInterfaceId(), apiConfig.getIntegrationUrl(),
					apiConfig.getSecurityStrategyCode(),
					apiConfig.getIntegrationRequestHeaderAssemble(),
					apiConfig.getIntegrationRequestBodyAssemble(),
					apiConfig.getIntegrationResponseParser(), apiConfig.getCommunicationProperties(),
					null, apiConfig.getInterfaceName(), apiConfig.getVersion(), apiConfig.getStatus(),
					index);

			// 2. build inbound interface
			String inboundInterfaceId = InterfaceConfigUtil.generateInterfaceId(apiConfig.getUrl(),
					apiConfig.getTenantId(), apiConfig.getVersion(), apiConfig.getType(),
					new HashMap<>());

			buildConfig(inboundInterfaceId, apiConfig.getUrl(), apiConfig.getSecurityStrategyCode(),
					apiConfig.getInterfaceResponseHeaderAssemble(),
					apiConfig.getInterfaceResponseBodyAssemble(), apiConfig.getInterfaceRequestParser(),
					apiConfig.getCommunicationProperties(), apiConfig.getInterfaceId(),
					apiConfig.getInterfaceName(), apiConfig.getVersion(), apiConfig.getStatus(), index);

			index++;
		}
	}

	/**
	 * build interface config, either inbound config or outbound config
	 *
	 *
	 *
	 *
	 * request header assemble.
	 *
	 * request body assemble.
	 *
	 * parser.
	 */
	private void buildConfig(String interfaceId, String uriString, String securityStrategyCode,
							String headerAssemble, String bodyAssemble, String parser,
							String communicationProperties, String integrationInterfaceId,
							String interfaceName, String version, String status, int index) {

		MessageReceiverConfigDto msgReceiver = messageReceiverConfigBuilder.build(uriString,
				securityStrategyCode);
		if (null != msgReceiver) {
			messageReceiverConfigDtoList.add(msgReceiver);
		}

		InterfaceConfigDto interfaceConfigDto = interfaceConfigBuilder.build(interfaceId,
				securityStrategyCode, integrationInterfaceId, interfaceName, version, status,
				communicationProperties);

		if (null != interfaceConfigDto) {
			interfaceConfigDtoList.add(interfaceConfigDto);
		}

		MessageProcessConfigDto messageProcessConfigDto = messageProcessConfigBuilder
				.buildByTemplateContent(interfaceId, headerAssemble, bodyAssemble, parser);

		if (null != messageProcessConfigDto) {
			messageProcessConfigDtoList.add(messageProcessConfigDto);
		}

		CommunicationConfigDto communicationConfigDto = communicationConfigBuilder.build(uriString,
				interfaceId, securityStrategyCode, communicationProperties, index);

		if (null != communicationConfigDto) {
			communicationConfigDtoList.add(communicationConfigDto);
		}

	}

	/**
	 * build spi config. only for outbound configs.
	 */
	private void buildSPIConfig(List<InterfaceDO> spiConfigs) {
		int index = 0;
		for (InterfaceDO spiConfig : spiConfigs) {
			// only build spi configs
			if (InterfaceType.OPENAPI == InterfaceType.getEnumByCode(spiConfig.getType())) {
				continue;
			}

			buildConfig(spiConfig.getInterfaceId(), spiConfig.getIntegrationUrl(),
					spiConfig.getSecurityStrategyCode(),
					spiConfig.getIntegrationRequestHeaderAssemble(),
					spiConfig.getIntegrationRequestBodyAssemble(),
					spiConfig.getIntegrationResponseParser(), spiConfig.getCommunicationProperties(),
					null, spiConfig.getInterfaceName(), spiConfig.getVersion(), spiConfig.getStatus(),
					index);

			index++;
		}
	}

	/**
	 *
	 */
	@Override
	public void addInterface(InterfaceDto dto) {
		InterfaceDO interfaceDO = InterfaceConvertor.dto2DO(dto);
		try {
			gatewayInterfaceDAO.insert(interfaceDO);
		} catch (DataIntegrityViolationException exception) {
			throw new CommonException(GatewayErrorCode.INTERFACE_ALREADY_EXISTS,
					GatewayErrorCode.INTERFACE_ALREADY_EXISTS.getMessage());
		}

	}

	/**
	 *
	 */
	@Override
	public void updateInterface(InterfaceDto dto) {
		gatewayTransactionTemplate.execute(transactionStatus -> {
			// 1. lock interface
			InterfaceDO interfaceDO = gatewayInterfaceDAO
					.lockByInterfaceId(dto.getInterfaceId());
			AssertUtil.isNotNull(interfaceDO, GatewayErrorCode.INTERFACE_NOT_EXIST);
			InterfaceDO updateDO = InterfaceConvertor.dto2DO(dto);

			// 2. update
			gatewayInterfaceDAO.update(updateDO);
			return null;
		});
	}

	/**
	 *
	 */
	@Override
	public void onlineInterface(String interfaceId) {
		gatewayTransactionTemplate.execute(transactionStatus -> {
			// 1. lock interface
			InterfaceDO interfaceDO = gatewayInterfaceDAO.loadByInterfaceId(interfaceId);
			AssertUtil.isNotNull(interfaceDO, GatewayErrorCode.INTERFACE_NOT_EXIST);

			// 2. lock all version interface
			List<InterfaceDO> interfaceDOS = gatewayInterfaceDAO.lock(interfaceDO.getType(),
					interfaceDO.getUrl(), interfaceDO.getTenantId());
			AssertUtil.isTrue(!CollectionUtils.isEmpty(interfaceDOS),
					GatewayErrorCode.INTERFACE_NOT_EXIST,
					GatewayErrorCode.INTERFACE_NOT_EXIST.getMessage());

			// 3. online current version, offline other versions
			boolean online = false;
			for (InterfaceDO interfaceDO1 : interfaceDOS) {
				if (StringUtils.equals(interfaceDO1.getInterfaceId(), interfaceId)) {
					interfaceDO1.setStatus(InterfaceStatus.VALID.getCode());
					gatewayInterfaceDAO.update(interfaceDO1);
					online = true;
				} else {
					interfaceDO1.setStatus(InterfaceStatus.INVALID.getCode());
					gatewayInterfaceDAO.update(interfaceDO1);
				}
			}
			// 4. make sure current version is ONLINE
			AssertUtil.isTrue(online, GatewayErrorCode.INTERFACE_NOT_EXIST,
					GatewayErrorCode.INTERFACE_NOT_EXIST.getMessage());
			return null;
		});
	}

	/**
	 *
	 */
	@Override
	public void offlineInterface(String interfaceId) {
		gatewayTransactionTemplate.execute(transactionStatus -> {
			// 1. lock interface
			InterfaceDO interfaceDO = gatewayInterfaceDAO.lockByInterfaceId(interfaceId);
			AssertUtil.isNotNull(interfaceDO, GatewayErrorCode.INTERFACE_NOT_EXIST);

			interfaceDO.setStatus(InterfaceStatus.INVALID.getCode());
			// 2. update
			gatewayInterfaceDAO.update(interfaceDO);
			return null;
		});
	}

	/**
	 *
	 */
	@Override
	public List<InterfaceDto> queryInterface(Integer pageSize, Integer page, String tntInstId,
											String interfaceId, String clientId, String type,
											String status, String interfaceName) {
		List<InterfaceDO> loadAllPage = gatewayInterfaceDAO.loadByPage(tntInstId, interfaceId,
				clientId, type, status, interfaceName, (page - 1) * pageSize, pageSize);
		List<InterfaceDto> result = new ArrayList<>();
		for (InterfaceDO interfaceDO : loadAllPage) {
			result.add(InterfaceConvertor.do2Dto(interfaceDO));
		}
		return result;
	}

	/**
	 *
	 */
	@Override
	public void removeInterface(String interfaceId) {
		gatewayInstInterfaceMapDAO.deleteByInterfaceId(interfaceId);
	}

	@Override
	public void upgradeInterface(InterfaceDto dto) {
		gatewayTransactionTemplate.execute(status -> {
			// 1. load interface
			InterfaceDO interfaceDO = gatewayInterfaceDAO
					.loadByInterfaceId(dto.getInterfaceId());
			AssertUtil.isNotNull(interfaceDO, GatewayErrorCode.INTERFACE_NOT_EXIST);

			// 2. lock all version for current interface
			List<InterfaceDO> dos = gatewayInterfaceDAO.lock(interfaceDO.getTenantId(),
					interfaceDO.getUrl(), interfaceDO.getType());

			// 3. make sure request version is bigger than any other version
			for (InterfaceDO doItem : dos) {
				if (StringUtils.compare(dto.getVersion(), doItem.getVersion()) <= 0) {
					// confirm the request version is the biggest.
					throw new CommonException(
							GatewayErrorCode.INTERFACE_VERSION_NOT_BIGGEST);
				}
			}
			// 4. save current version
			dto.setInterfaceId(
					InterfaceConfigUtil.generateInterfaceId(dto.getUrl(), dto.getTenantId(),
							dto.getVersion(), dto.getType(), dto.getCommunicationProperties()));
			gatewayInterfaceDAO.insert(InterfaceConvertor.dto2DO(dto));
			return null;
		});

	}
}
