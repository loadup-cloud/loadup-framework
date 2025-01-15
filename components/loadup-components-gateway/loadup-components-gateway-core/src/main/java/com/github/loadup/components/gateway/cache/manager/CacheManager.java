package com.github.loadup.components.gateway.cache.manager;

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

import com.github.loadup.components.gateway.cache.*;
import com.github.loadup.components.gateway.common.util.RepositoryUtil;
import com.github.loadup.components.gateway.core.common.cache.CacheName;
import com.github.loadup.components.gateway.core.common.enums.RepositoryType;
import com.github.loadup.components.gateway.core.communication.http.cache.HttpClientCache;
import com.github.loadup.components.gateway.core.model.*;
import com.github.loadup.components.gateway.facade.util.LogUtil;
import com.github.loadup.components.gateway.message.script.cache.MessageComponentCacheManager;
import com.github.loadup.components.gateway.repository.RepositoryManager;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * <p>
 * CacheManger.java
 * </p>
 */
@Component("cacheManager")
public class CacheManager {

	private static final Logger logger = LoggerFactory.getLogger(CacheManager.class);

	@Resource
	@Qualifier("repositoryManager")
	private RepositoryManager repositoryManager;

	@Resource
	private MessageComponentCacheManager messageComponentCacheManager;

	@Resource
	private HttpClientCache httpClientCache;

	/**
	 * is initialization success
	 */
	private boolean isInitOk = false;

	/**
	 * Init.
	 */
	public void init() {
		if (RepositoryUtil.getRepositoryType() == RepositoryType.PRODCENTER) {
			return;
		}
		try {

			//1. 查询所有列表结果
			Map<CacheName, List> resultMap = repositoryManager.load();
			List<InterfaceConfig> interfaceConfigList = resultMap.get(CacheName.INTERFACE);
			List<MessageProcessConfig> messageProcessConfigList = resultMap
					.get(CacheName.MESSAGE_PROCESS);
			List<CommunicationConfig> communicationConfigList = resultMap
					.get(CacheName.COMMUNICATION);
			List<MessageReceiverConfig> messageReceiverConfigList = resultMap
					.get(CacheName.MESSAGE_RECEIVER);
			List<MessageSenderConfig> messageSenderConfigList = resultMap
					.get(CacheName.MESSAGE_SENDER);
			List<CertConfig> certConfigList = resultMap.get(CacheName.CERT_CONFIG);
			List<CertAlgorithmConfig> certAlgorithmConfigList = resultMap
					.get(CacheName.CERT_ALGO_CONFIG);
			List<InstConfig> instConfigList = resultMap.get(CacheName.INST);

			//2. 推送到对应Cache中
			pushToCache(interfaceConfigList, messageProcessConfigList, communicationConfigList,
					messageReceiverConfigList, messageSenderConfigList, certConfigList,
					certAlgorithmConfigList, instConfigList);
		} catch (Exception e) {
			LogUtil.error(logger, e,
					"CacheManager init fail, pls check the business-component-dependencies and upgrade to 1.0.5 or the latest");
			this.isInitOk = false;

		}
		this.isInitOk = true;
	}

	/**
	 * push new configs to cache
	 */
	public void pushToCache(List<InterfaceConfig> interfaceConfigList,
							List<MessageProcessConfig> messageProcessConfigList,
							List<CommunicationConfig> communicationConfigList,
							List<MessageReceiverConfig> messageReceiverConfigList,
							List<MessageSenderConfig> messageSenderConfigList,
							List<CertConfig> certConfigList,
							List<CertAlgorithmConfig> certAlgorithmConfigList,
							List<InstConfig> instConfigList) {
		initCacheInfo(false, interfaceConfigList, messageProcessConfigList, communicationConfigList,
				messageReceiverConfigList, messageSenderConfigList, certConfigList,
				certAlgorithmConfigList, instConfigList);

		// 2. push new configs to script cache
		messageComponentCacheManager.init(interfaceConfigList, messageProcessConfigList,
				new ArrayList<DynamicScriptBeanConfig>());
		// 3. init communication
		httpClientCache.init(communicationConfigList);
	}

	/**
	 * Is init ok boolean.
	 */
	public boolean isInitOk() {
		return this.isInitOk;
	}

	/**
	 * Refresh.
	 */
	public void refresh(Object... obj) {
		if (RepositoryUtil.getRepositoryType() == RepositoryType.PRODCENTER) {
			return;
		}
		//1. 查询所有列表结果
		Map<CacheName, List> resultMap = repositoryManager.load();

		List<InterfaceConfig> interfaceConfigList = resultMap.get(CacheName.INTERFACE);
		List<MessageProcessConfig> messageProcessConfigList = resultMap
				.get(CacheName.MESSAGE_PROCESS);
		List<CommunicationConfig> communicationConfigList = resultMap.get(CacheName.COMMUNICATION);
		List<MessageReceiverConfig> messageReceiverConfigList = resultMap
				.get(CacheName.MESSAGE_RECEIVER);
		List<MessageSenderConfig> messageSenderConfigList = resultMap.get(CacheName.MESSAGE_SENDER);
		List<CertConfig> certConfigList = resultMap.get(CacheName.CERT_CONFIG);
		List<CertAlgorithmConfig> certAlgorithmConfigList = resultMap
				.get(CacheName.CERT_ALGO_CONFIG);
		List<InstConfig> instConfigList = resultMap.get(CacheName.INST);

		//2. 推送到对应Cache中
		initCacheInfo(true, interfaceConfigList, messageProcessConfigList, communicationConfigList,
				messageReceiverConfigList, messageSenderConfigList, certConfigList,
				certAlgorithmConfigList, instConfigList);

		messageComponentCacheManager.refresh(interfaceConfigList, messageProcessConfigList,
				new ArrayList<DynamicScriptBeanConfig>());
		//4. refresh communication
		httpClientCache.refresh(communicationConfigList);
	}

	private void initCacheInfo(boolean clear, List<InterfaceConfig> interfaceConfigList,
							List<MessageProcessConfig> messageProcessConfigList,
							List<CommunicationConfig> communicationConfigList,
							List<MessageReceiverConfig> messageReceiverConfigList,
							List<MessageSenderConfig> messageSenderConfigList,
							List<CertConfig> certConfigList,
							List<CertAlgorithmConfig> certAlgorithmConfigList,
							List<InstConfig> instConfigList) {
		InterfaceConfigCache.putAll(clear, interfaceConfigList);
		MessageProcessConfigCache.putAll(clear, messageProcessConfigList);
		CommunicationConfigCache.putAll(clear, communicationConfigList);
		MessageReceiverConfigCache.putAll(clear, messageReceiverConfigList);
		MessageSenderConfigCache.putAll(clear, messageSenderConfigList);
		CertConfigCache.putAll(clear, certConfigList);
		CertAlgorithmConfigCache.putAll(clear, certAlgorithmConfigList);
		InstConfigCache.putAll(clear, instConfigList);
	}

	/**
	 * Refresh cache by interface id.
	 */
	public void refreshByInterfaceId(String interfaceId) {
		if (RepositoryUtil.getRepositoryType() == RepositoryType.PRODCENTER) {
			return;
		}
		//1. 查询所有列表结果
		Map<CacheName, List> resultMap = repositoryManager.loadByInterfaceId(interfaceId);

		List<InterfaceConfig> interfaceConfigList = resultMap.get(CacheName.INTERFACE);
		List<MessageProcessConfig> messageProcessConfigList = resultMap
				.get(CacheName.MESSAGE_PROCESS);
		List<CommunicationConfig> communicationConfigList = resultMap.get(CacheName.COMMUNICATION);
		List<MessageReceiverConfig> messageReceiverConfigList = resultMap
				.get(CacheName.MESSAGE_RECEIVER);
		List<MessageSenderConfig> messageSenderConfigList = resultMap.get(CacheName.MESSAGE_SENDER);

		//2. 推送到对应Cache中
		initCacheInfo(false, interfaceConfigList, messageProcessConfigList, communicationConfigList,
				messageReceiverConfigList, messageSenderConfigList, null, null, null);

		messageComponentCacheManager.refresh(interfaceConfigList, messageProcessConfigList,
				new ArrayList<DynamicScriptBeanConfig>());
		//4. refresh communication
		httpClientCache.refresh(communicationConfigList);
	}

	/**
	 * Refresh cert.
	 */
	public void refreshCert(String clientId) {

		//1. 查询所有列表结果
		Map<CacheName, List> resultMap = repositoryManager.loadCertByClientId(clientId);
		List<CertConfig> certConfigList = resultMap.get(CacheName.CERT_CONFIG);
		List<CertAlgorithmConfig> certAlgorithmConfigList = resultMap
				.get(CacheName.CERT_ALGO_CONFIG);

		//2. 推送到对应Cache中
		initCacheInfo(true, null, null, null, null, null, certConfigList, certAlgorithmConfigList,
				null);
	}

}
