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

import com.github.loadup.components.gateway.common.convertor.AssembleTemplateConvertor;
import com.github.loadup.components.gateway.common.util.RepositoryUtil;
import com.github.loadup.components.gateway.core.common.enums.*;
import com.github.loadup.components.gateway.core.model.InterfaceConfig;
import com.github.loadup.components.gateway.core.model.MessageProcessConfig;
import com.github.loadup.components.gateway.core.service.InterfaceProdCenterQueryService;
import com.github.loadup.components.gateway.facade.config.model.APIConditionGroup;
import com.github.loadup.components.gateway.facade.config.model.SPIConditionGroup;
import com.github.loadup.components.gateway.facade.util.LogUtil;
import com.github.loadup.components.gateway.message.base.model.AssembleTemplate;
import com.github.loadup.components.gateway.message.base.model.MessageStruct;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.locks.*;

/**
 * Velocity组装模板缓存
 */
@Component("gatewayVelocityScriptCache")
public class VelocityScriptCache {

	protected static final Logger logger = LoggerFactory
			.getLogger(VelocityScriptCache.class);

	/**
	 * 读写锁
	 */
	private static final ReadWriteLock lock = new ReentrantReadWriteLock();

	/**
	 * vm模板缓存
	 */
	private static Map<String, AssembleTemplate> scriptCache = new HashMap<String, AssembleTemplate>();

	/**
	 * interface product center query service
	 */
	private static InterfaceProdCenterQueryService interfaceProdCenterQueryService;

	/**
	 * 设置缓存
	 *
	 * @clear clear cache if true
	 */
	public static void putAll(boolean clear, List<InterfaceConfig> interfaceConfigs,
							Map<String, MessageProcessConfig> processConfigs) {
		if (interfaceConfigs == null) {
			return;
		}
		Lock writelock = lock.writeLock();
		writelock.lock();
		try {
			String processorId = null;
			String interfaceId = null;
			Map<String, AssembleTemplate> tempCache = new HashMap<String, AssembleTemplate>(
					interfaceConfigs.size());
			LogUtil.info(logger, "init velocity script,interface size=" + interfaceConfigs.size()
					+ ",processConfigs size=" + processConfigs.size());
			for (InterfaceConfig interfaceConfig : interfaceConfigs) {
				interfaceId = interfaceConfig.getInterfaceId();
				processorId = interfaceConfig.getMessageProcessorId();
				MessageProcessConfig processConfig = processConfigs.get(processorId);
				if (processConfig != null) {
					AssembleTemplate template = convert(processConfig);
					tempCache.put(interfaceId, template);
				}
			}
			if (clear) {
				scriptCache = tempCache;
			} else {
				scriptCache.putAll(tempCache);
			}
		} finally {
			writelock.unlock();
		}
	}

	/**
	 * 更新单个接口部分缓存
	 */
	public static void putPart(List<InterfaceConfig> interfaceConfigs,
							Map<String, MessageProcessConfig> processConfigs) {
		Lock writelock = lock.writeLock();
		writelock.lock();
		try {
			for (InterfaceConfig interfaceConfig : interfaceConfigs) {
				String interfaceId = interfaceConfig.getInterfaceId();
				scriptCache.remove(interfaceId);
				MessageProcessConfig processConfig = processConfigs
						.get(interfaceConfig.getMessageProcessorId());
				if (processConfig != null) {
					AssembleTemplate template = convert(processConfig);
					scriptCache.put(interfaceId, template);
				}
			}
		} finally {
			writelock.unlock();
		}
	}

	/**
	 * 根据接口ID返回组装模板
	 */
	public static AssembleTemplate getAssembleTemplate(String interfaceId, RoleType roleType,
													String interfaceTypeStr) {
		if (StringUtils.isBlank(interfaceId)) {
			return null;
		}
		Lock readlock = lock.readLock();
		readlock.lock();
		try {
			if (RepositoryUtil.getRepositoryType() == RepositoryType.PRODCENTER) {
				if (RoleType.SENDER == roleType
						&& InterfaceType.OPENAPI == InterfaceType.getEnumByCode(interfaceTypeStr)) {
					APIConditionGroup apiConditionGroup = interfaceProdCenterQueryService
							.queryAPIConditionGroup(interfaceId, null);
					return AssembleTemplateConvertor.convertToSenderConfig(apiConditionGroup);
				} else if (RoleType.RECEIVER == roleType && InterfaceType.OPENAPI == InterfaceType
						.getEnumByCode(interfaceTypeStr)) {
					APIConditionGroup apiConditionGroup = interfaceProdCenterQueryService
							.queryAPIConditionGroup(null, interfaceId);
					return AssembleTemplateConvertor.convertToReceiverConfig(apiConditionGroup);
				} else if (InterfaceType.SPI == InterfaceType.getEnumByCode(interfaceTypeStr)) {
					SPIConditionGroup spiConditionGroup = interfaceProdCenterQueryService
							.querySPIConditionGroup(interfaceId);
					return AssembleTemplateConvertor.convertToReceiverConfig(spiConditionGroup);
				}
			} else {
				return scriptCache.get(interfaceId);
			}
		} finally {
			readlock.unlock();
		}
		return null;
	}

	private static AssembleTemplate convert(MessageProcessConfig processConfig) {
		AssembleTemplate template = new AssembleTemplate();
		template.setMessageProcessId(processConfig.getMessageProcessId());
		template.setMainTemplate(processConfig.getAssembleTemplate());
		template.setSubTemplate(processConfig.getAssembleSubTemplate());
		template.setExtraTemplate(processConfig.getAssembleExtTemplate());
		template.setHeaderTemplate(processConfig.getHeaderTemplate());
		template.setErrorTemplate(processConfig.getErrorTemplate());
		template.setErrorSubTemplate(processConfig.getErrorSubTemplate());
		String assembleType = processConfig.getAssembleType();
		try {
			MessageStruct messageStruct = MessageStruct.valueOf(assembleType);
			template.setMessageStruct(messageStruct);
		} catch (Exception e) {
			LogUtil.error(logger, e, "invalid assembleType:" + assembleType);
			return null;
		}

		return template;
	}

	/**
	 * Setter method for property <tt>interfaceProdCenterQueryService</tt>.
	 */
	@Resource
	public void setInterfaceProdCenterQueryService(InterfaceProdCenterQueryService interfaceProdCenterQueryService) {
		VelocityScriptCache.interfaceProdCenterQueryService = interfaceProdCenterQueryService;
	}

}
