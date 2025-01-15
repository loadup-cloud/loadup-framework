package com.github.loadup.components.gateway.core.ctrl.context;

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

import com.github.loadup.components.gateway.common.exception.GatewayException;
import com.github.loadup.components.gateway.core.model.CommunicationConfig;
import com.github.loadup.components.gateway.core.model.InterfaceConfig;
import com.github.loadup.components.gateway.core.model.common.MessageEnvelope;
import com.github.loadup.components.gateway.facade.model.LimitConfig;

import java.util.Collections;
import java.util.List;

/**
 * <p>:ProcessContext.java
 */
public class GatewayRuntimeProcessContext {

	/**
	 * request interface config
	 */
	private InterfaceConfig requesterInterfaceConfig;

	/**
	 * integrator interface config
	 */
	private InterfaceConfig integratorInterfaceConfig;

	/**
	 * request communication config
	 */
	private CommunicationConfig requesterCommunicationConfig;

	/**
	 * integrator communication config
	 */
	private CommunicationConfig integratorCommunicationConfig;

	/**
	 * trace id
	 */
	private String traceId;

	/**
	 * transaction type, see InterfaceType
	 */
	private String transactionType;

	/**
	 * receiver client id
	 */
	private String requesterClientId;

	/**
	 * integrator client id
	 */
	private String integratorClientId;

	/**
	 * requester cert code
	 */
	private String requesterCertCode;

	/**
	 * integratorCertCode
	 */
	private String integratorCertCode;

	/**
	 * requester url
	 */
	private String requesterUrl;

	/**
	 * requester uri
	 */
	private String requesterUri;

	/**
	 * integrator url
	 */
	private String integratorUrl;

	/**
	 * integrator interface id
	 */
	private String integratorInterfaceId;

	/**
	 * requester http method
	 */
	private String requesterHttpMethod;

	/**
	 * need status
	 */
	private boolean needStatus = true;

	/**
	 * limit entry key id list
	 */
	private List<LimitConfig> limitConfigListList = Collections.emptyList();

	/**
	 * Gets get transaction type.
	 */
	public String getTransactionType() {
		return transactionType;
	}

	/**
	 * Sets set transaction type.
	 */
	public void setTransactionType(String transactionType) {
		this.transactionType = transactionType;
	}

	/**
	 * Is need status boolean.
	 */
	public boolean isNeedStatus() {
		return needStatus;
	}

	/**
	 * Sets set need status.
	 */
	public void setNeedStatus(boolean needStatus) {
		this.needStatus = needStatus;
	}

	/**
	 * Gets get request uri.
	 */
	public String getRequesterUri() {
		return requesterUri;
	}

	/**
	 * Sets set request uri.
	 */
	public void setRequesterUri(String requesterUri) {
		this.requesterUri = requesterUri;
	}

	/**
	 * request message: this is from the requester side
	 */
	private MessageEnvelope requestMessage;

	/**
	 * response message: the message that we replay to the requester
	 */
	private MessageEnvelope responseMessage;

	/**
	 * resultMessage: each result we get from the previous action. all the middle result is in it.
	 */
	private MessageEnvelope resultMessage;

	/**
	 * business exception
	 */
	public GatewayException businessException;

	/**
	 * Gets get requester url.
	 */
	public String getRequesterUrl() {
		return requesterUrl;
	}

	/**
	 * Sets set requester url.
	 */
	public void setRequesterUrl(String requesterUrl) {
		this.requesterUrl = requesterUrl;
	}

	/**
	 * Gets get integration url.
	 */
	public String getIntegratorUrl() {
		return integratorUrl;
	}

	/**
	 * Sets set integrator url.
	 */
	public void setIntegratorUrl(String integratorUrl) {
		this.integratorUrl = integratorUrl;
	}

	/**
	 * Gets get requester http method.
	 */
	public String getRequesterHttpMethod() {
		return requesterHttpMethod;
	}

	/**
	 * Sets set requester http method.
	 */
	public void setRequesterHttpMethod(String requesterHttpMethod) {
		this.requesterHttpMethod = requesterHttpMethod;
	}

	/**
	 * Sets set business exception.
	 */
	public void setBusinessException(GatewayException businessException) {
		this.businessException = businessException;
	}

	/**
	 * Gets get requester interface config.
	 */
	public InterfaceConfig getRequesterInterfaceConfig() {
		return requesterInterfaceConfig;
	}

	/**
	 * Sets set requester interface config.
	 */
	public void setRequesterInterfaceConfig(InterfaceConfig requesterInterfaceConfig) {
		this.requesterInterfaceConfig = requesterInterfaceConfig;
	}

	/**
	 * Gets get integrator interface config.
	 */
	public InterfaceConfig getIntegratorInterfaceConfig() {
		return integratorInterfaceConfig;
	}

	/**
	 * Sets set integrator interface config.
	 */
	public void setIntegratorInterfaceConfig(InterfaceConfig integratorInterfaceConfig) {
		this.integratorInterfaceConfig = integratorInterfaceConfig;
	}

	/**
	 * Gets get requester communication config.
	 */
	public CommunicationConfig getRequesterCommunicationConfig() {
		return requesterCommunicationConfig;
	}

	/**
	 * Sets set requester communication config.
	 */
	public void setRequesterCommunicationConfig(CommunicationConfig requesterCommunicationConfig) {
		this.requesterCommunicationConfig = requesterCommunicationConfig;
	}

	/**
	 * Gets get integrator communication config.
	 */
	public CommunicationConfig getIntegratorCommunicationConfig() {
		return integratorCommunicationConfig;
	}

	/**
	 * Sets set integrator communication config.
	 */
	public void setIntegratorCommunicationConfig(CommunicationConfig integratorCommunicationConfig) {
		this.integratorCommunicationConfig = integratorCommunicationConfig;
	}

	/**
	 * Gets get trace id.
	 */
	public String getTraceId() {
		return traceId;
	}

	/**
	 * Sets set trace id.
	 */
	public void setTraceId(String traceId) {
		this.traceId = traceId;
	}

	/**
	 * Gets get receiver client id.
	 */
	public String getRequesterClientId() {
		return requesterClientId;
	}

	/**
	 * Sets set receiver client id.
	 */
	public void setRequesterClientId(String requesterClientId) {
		this.requesterClientId = requesterClientId;
	}

	/**
	 * Gets get integrator client id.
	 */
	public String getIntegratorClientId() {
		return integratorClientId;
	}

	/**
	 * Sets set integrator client id.
	 */
	public void setIntegratorClientId(String integratorClientId) {
		this.integratorClientId = integratorClientId;
	}

	/**
	 * Gets get cert code from requester.
	 */
	public String getRequesterCertCode() {
		return requesterCertCode;
	}

	/**
	 * Sets set cert code from requester.
	 */
	public void setRequesterCertCode(String requesterCertCode) {
		this.requesterCertCode = requesterCertCode;
	}

	/**
	 * Gets get cert code from integrator.
	 */
	public String getIntegratorCertCode() {
		return integratorCertCode;
	}

	/**
	 * Sets set cert code from integrator.
	 */
	public void setIntegratorCertCode(String integratorCertCode) {
		this.integratorCertCode = integratorCertCode;
	}

	/**
	 * Gets get request message.
	 */
	public MessageEnvelope getRequestMessage() {
		return requestMessage;
	}

	/**
	 * Sets set request message.
	 */
	public void setRequestMessage(MessageEnvelope requestMessage) {
		this.requestMessage = requestMessage;
	}

	/**
	 * Gets get response message.
	 */
	public MessageEnvelope getResponseMessage() {
		return responseMessage;
	}

	/**
	 * Sets set response message.
	 */
	public void setResponseMessage(MessageEnvelope responseMessage) {
		this.responseMessage = responseMessage;
	}

	/**
	 * Gets get result message.
	 */
	public MessageEnvelope getResultMessage() {
		return resultMessage;
	}

	/**
	 * Sets set result message.
	 */
	public void setResultMessage(MessageEnvelope resultMessage) {
		this.resultMessage = resultMessage;
	}

	/**
	 * Gets get business exception.
	 */
	public GatewayException getBusinessException() {
		return businessException;
	}

	/**
	 * Gets get integrator interface id.
	 */
	public String getIntegratorInterfaceId() {
		return integratorInterfaceId;
	}

	/**
	 * Sets set integrator interface id.
	 */
	public void setIntegratorInterfaceId(String integratorInterfaceId) {
		this.integratorInterfaceId = integratorInterfaceId;
	}

	/**
	 * Gets get limitConfigListList.
	 */
	public List<LimitConfig> getLimitConfigList() {
		return limitConfigListList;
	}

	/**
	 * Sets set limitConfigListList.
	 */
	public void setLimitConfigList(List<LimitConfig> limitConfigListList) {
		this.limitConfigListList = limitConfigListList;
	}
}
