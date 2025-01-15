package com.github.loadup.components.gateway.core.ctrl.action.origin;

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

import com.github.loadup.components.gateway.cache.CommunicationConfigCache;
import com.github.loadup.components.gateway.cache.InterfaceConfigCache;
import com.github.loadup.components.gateway.common.exception.GatewayException;
import com.github.loadup.components.gateway.common.exception.util.AssertUtil;
import com.github.loadup.components.gateway.common.util.RepositoryUtil;
import com.github.loadup.components.gateway.core.common.GatewayliteErrorCode;
import com.github.loadup.components.gateway.core.common.annotation.LogTraceId;
import com.github.loadup.components.gateway.core.common.enums.RepositoryType;
import com.github.loadup.components.gateway.core.common.enums.RoleType;
import com.github.loadup.components.gateway.core.ctrl.action.AbstractBusinessAction;
import com.github.loadup.components.gateway.core.ctrl.action.BusinessAction;
import com.github.loadup.components.gateway.core.ctrl.context.GatewayRuntimeProcessContext;
import com.github.loadup.components.gateway.core.model.CommunicationConfig;
import com.github.loadup.components.gateway.core.model.InterfaceConfig;
import com.github.loadup.components.gateway.core.prototype.util.SupergwGatewayConfigurationUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * <p>
 * HttpServiceAction.java
 * HttpServiceAction -> RequestParseAction -> RequestAssembleAction -> SendToIntegratorAction -> ResponseParseAction ->
 * ResponseAssembleAction
 * </p>
 */
@Component("httpServiceAction")
public class HttpServiceAction extends AbstractBusinessAction {

	@Override
	@LogTraceId
	public void doBusiness(GatewayRuntimeProcessContext gatewayRuntimeProcessContext) {

		String integratorUrl = gatewayRuntimeProcessContext.getIntegratorUrl();
		String interfaceId = gatewayRuntimeProcessContext.getIntegratorInterfaceId();

		CommunicationConfig receiverCommunicationConfig = null;

		if (StringUtils.isBlank(integratorUrl) && StringUtils.isBlank(interfaceId)) {
			integratorUrl = fillUpRequesterConfig(gatewayRuntimeProcessContext);

			//remove all extend information
			integratorUrl = SupergwGatewayConfigurationUtils.getStrBeforeCharset(integratorUrl,
					"?");
			receiverCommunicationConfig = CommunicationConfigCache.getWithUrl(integratorUrl,
					gatewayRuntimeProcessContext.getTransactionType());
		} else {
			receiverCommunicationConfig = getCommunicationConfigWithUrlOrInterfaceId(gatewayRuntimeProcessContext, integratorUrl,
					interfaceId);
		}
		AssertUtil.isNotNull(receiverCommunicationConfig, "integrator communication config is not found");

		InterfaceConfig integratorInterfaceConfig = null;

		integratorInterfaceConfig = InterfaceConfigCache.getWithInterfaceId(
				receiverCommunicationConfig.getInterfaceId(), RoleType.RECEIVER,
				gatewayRuntimeProcessContext.getTransactionType());

		gatewayRuntimeProcessContext.setIntegratorInterfaceConfig(integratorInterfaceConfig);
		gatewayRuntimeProcessContext.setIntegratorCommunicationConfig(receiverCommunicationConfig);
	}

	/**
	 * fetch communication config with url or interface id
	 */
	private CommunicationConfig getCommunicationConfigWithUrlOrInterfaceId(GatewayRuntimeProcessContext gatewayRuntimeProcessContext,
																		String integratorUrl,
																		String interfaceId) {
		CommunicationConfig receiverCommunicationConfig;
		if (StringUtils.isNotBlank(interfaceId)) {
			receiverCommunicationConfig = CommunicationConfigCache.getWithInterfaceId(interfaceId);
		} else {
			receiverCommunicationConfig = CommunicationConfigCache.getWithUrl(integratorUrl,
					gatewayRuntimeProcessContext.getTransactionType());
		}
		return receiverCommunicationConfig;
	}

	/**
	 * fill sender config
	 */
	private String fillUpRequesterConfig(GatewayRuntimeProcessContext gatewayRuntimeProcessContext) {
		CommunicationConfig senderCommunicationConfig = getSenderCommunicationConfig(gatewayRuntimeProcessContext);
		InterfaceConfig senderInterfaceConfig = getSenderInterfaceConfig(gatewayRuntimeProcessContext);
		//FIXME Temp plan, set the integrator in the message receiver interface id
		String integratorInterfaceId = senderInterfaceConfig.getMessageReceiverInterfaceId();
		String senderCertCode = senderInterfaceConfig.getSecurityStrategyCode();
		gatewayRuntimeProcessContext.setRequesterInterfaceConfig(senderInterfaceConfig);
		gatewayRuntimeProcessContext.setRequesterCertCode(senderCertCode);
		gatewayRuntimeProcessContext.getRequestMessage().setSignatureCertCode(senderCertCode);
		gatewayRuntimeProcessContext.setRequesterCommunicationConfig(senderCommunicationConfig);
		CommunicationConfig receiverCommunicationConfig = null;
		if (RepositoryUtil.getRepositoryType() == RepositoryType.PRODCENTER) {
			// get receiver communication config from product center
			receiverCommunicationConfig = CommunicationConfigCache.getFromProdCenterWithOpenUrls(
					RoleType.RECEIVER, gatewayRuntimeProcessContext.getRequesterUri(),
					gatewayRuntimeProcessContext.getRequesterUrl());
		} else {
			receiverCommunicationConfig = CommunicationConfigCache
					.getWithInterfaceId(integratorInterfaceId);
		}
		AssertUtil.isNotNull(receiverCommunicationConfig);
		return receiverCommunicationConfig.getUrl();
	}

	/**
	 * get sender interface config
	 */
	private InterfaceConfig getSenderInterfaceConfig(GatewayRuntimeProcessContext gatewayRuntimeProcessContext) {
		CommunicationConfig senderCommunicationConfig = getSenderCommunicationConfig(
				gatewayRuntimeProcessContext);
		return InterfaceConfigCache.getWithInterfaceId(senderCommunicationConfig.getInterfaceId(),
				RoleType.SENDER, gatewayRuntimeProcessContext.getTransactionType());
	}

	/**
	 * get sender communication config
	 */
	private CommunicationConfig getSenderCommunicationConfig(GatewayRuntimeProcessContext gatewayRuntimeProcessContext) {
		CommunicationConfig senderCommunicationConfig = null;
		for (String url : Arrays.asList(gatewayRuntimeProcessContext.getRequesterUri(),
				gatewayRuntimeProcessContext.getRequesterUrl())) {
			try {
				senderCommunicationConfig = CommunicationConfigCache.getWithUrl(url,
						gatewayRuntimeProcessContext.getTransactionType());
			} catch (GatewayException ex) {
				if (ex.getErrorCode() != GatewayliteErrorCode.CONFIGURATION_LOAD_ERROR) {
					throw ex;
				}
			}
			if (senderCommunicationConfig != null) {
				break;
			}
		}
		AssertUtil.isNotNull(senderCommunicationConfig,
				GatewayliteErrorCode.CONFIGURATION_NOT_FOUND, "senderCommunicationConfig is null");
		return senderCommunicationConfig;
	}

	@Override
	@Resource
	@Qualifier("requestParseAction")
	public void setNextAction(BusinessAction requestParseAction) {
		this.nextAction = requestParseAction;
	}
}
