package com.github.loadup.components.gateway.service.impl;

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
import com.github.loadup.components.gateway.common.util.CommonUtil;
import com.github.loadup.components.gateway.common.util.ResultUtil;
import com.github.loadup.components.gateway.core.common.Constant;
import com.github.loadup.components.gateway.core.common.GatewayErrorCode;
import com.github.loadup.components.gateway.core.common.enums.*;
import com.github.loadup.components.gateway.core.ctrl.RuntimeProcessContextHolder;
import com.github.loadup.components.gateway.core.ctrl.action.BusinessAction;
import com.github.loadup.components.gateway.core.ctrl.action.atom.ExceptionAssembleAction;
import com.github.loadup.components.gateway.core.ctrl.context.GatewayRuntimeProcessContext;
import com.github.loadup.components.gateway.core.model.common.MessageEnvelope;
import com.github.loadup.components.gateway.core.prototype.util.DigestLoggerUtil;
import com.github.loadup.components.gateway.core.prototype.util.MetricLoggerUtil;
import com.github.loadup.components.gateway.facade.api.MessageService;
import com.github.loadup.components.gateway.facade.request.SPIRequest;
import com.github.loadup.components.gateway.facade.response.SPIResponse;
import com.github.loadup.components.gateway.facade.spi.LimitRuleService;
import com.github.loadup.components.gateway.facade.util.LogUtil;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Message send service
 * Provide method to send message to the integration side
 */
@Component("messageService")
public class MessageServiceImpl implements MessageService {

	private static final Logger logger = LoggerFactory
			.getLogger(MessageServiceImpl.class);

	@Resource
	@Qualifier("springBeanServiceAction")
	private BusinessAction springBeanServiceAction;

	@Resource
	@Qualifier("exceptionAssembleAction")
	private ExceptionAssembleAction exceptionAssembleAction;

	@Resource
	private LimitRuleService limitRuleService;

	/**
	 * Send string.
	 */
	@Override
	public String send(String integrationUrl, Map<String, String> message) {

		return send(new SPIRequest(integrationUrl, message)).getContent();
	}

	@Override
	public SPIResponse send(SPIRequest request) {

		long timecost = System.currentTimeMillis();
		SPIResponse spiResponse = new SPIResponse(StringUtils.EMPTY, new HashMap<>(),
				ResultUtil.buildSuccessResult());
		GatewayRuntimeProcessContext processContext = RuntimeProcessContextHolder
				.createRuntimeProcessContext();
		processContext.setTransactionType(InterfaceType.SPI.getCode());

		// init tracer
		String tracerIdByGatewaylite = null;
		//        Span span = Span.current();
		//TracerUtil.getSpan();
		//        if (null != span) {
		////            processContext.setTraceId(TracerUtil.getTracerId());
		//        } else {
		////            Span spangateway = TracerUtil.createSpan(Constant.COMPONENT_NAME);
		////            TracerUtil.putSpan(spangateway);
		////            TracerUtil.logTraceId(spangateway);
		////            tracerIdByGatewaylite = TracerUtil.getTracerId();
		////            processContext.setTraceId(tracerIdByGatewaylite);
		//        }
		//        processContext.setTraceId(TracerUtil.getTracerId());
		processContext.setIntegratorUrl(request.getIntegrationUrl());
		processContext.setIntegratorInterfaceId(request.getIntegrationUrl());
		MessageEnvelope requestMessage = new MessageEnvelope(MessageFormat.MAP, "");
		requestMessage.setContent(request.getMessage());
		processContext.setRequestMessage(requestMessage);
		processContext.setNeedStatus(false);

		// set mdc traceId
		MDC.put(Constant.TRACER_KEY, processContext.getTraceId());

		MessageEnvelope integrationReceiveResponse = null;
		String callRespMsg = null;
		boolean success = false;
		try {
			springBeanServiceAction.process(processContext);
			success = true;
			callRespMsg = "SUCCESS";
		} catch (CommonException e) {
			LogUtil.error(logger, e, "Message Service send message fail, throw business exception");
			processContext.setBusinessException(e);
			exceptionAssembleAction.process(processContext);
			callRespMsg = e.getMessage();
			spiResponse.setResult(ResultUtil.buildResult(e.getResultCode()));
			MetricLoggerUtil.countError(request.getIntegrationUrl(),
					System.currentTimeMillis() - timecost,
					request.getMessage().get(Constant.KEY_HTTP_CLIENT_ID), InterfaceType.SPI,
					processContext.getTraceId(), e.getResultCode());
		} catch (Throwable e) {
			LogUtil.error(logger, e, "Message Service send message fail! throw exception");
			CommonException commonException = new CommonException(
					GatewayErrorCode.UNKNOWN_EXCEPTION, e);
			processContext.setBusinessException(commonException);
			exceptionAssembleAction.process(processContext);
			callRespMsg = e.getMessage();
			spiResponse.setResult(
					ResultUtil.buildResult(GatewayErrorCode.UNKNOWN_EXCEPTION, e.getMessage()));

			MetricLoggerUtil.countError(request.getIntegrationUrl(),
					System.currentTimeMillis() - timecost,
					request.getMessage().get(Constant.KEY_HTTP_CLIENT_ID), InterfaceType.SPI,
					processContext.getTraceId(), commonException.getResultCode());
		} finally {
			//limitRuleService.resetToken(processContext.getIntegratorInterfaceId());
			integrationReceiveResponse = processContext.getResponseMessage();

			spiResponse.setContent(CommonUtil.getMsgContent(integrationReceiveResponse));
			Map<String, String> headers = integrationReceiveResponse == null ?
					Collections.emptyMap() : integrationReceiveResponse.getHeaders();
			spiResponse.setHeaders(headers);

			if (!MetricLoggerUtil.judgeBinderEnabled()) {
				DigestLoggerUtil.printSimpleDigestLog(request.getIntegrationUrl(), callRespMsg,
						System.currentTimeMillis() - timecost);
			} else {
				MetricLoggerUtil.monitor(request.getIntegrationUrl(),
						System.currentTimeMillis() - timecost, success,
						request.getMessage().get(Constant.KEY_HTTP_CLIENT_ID), InterfaceType.SPI,
						processContext.getTraceId(), InterfaceScope.INBOUND);
			}
			RuntimeProcessContextHolder.cleanActionContext();
			MDC.clear();

			// clear tracer
			if (StringUtils.isNotBlank(tracerIdByGatewaylite)) {
				//                Span activeSpan = TracerUtil.getSpan();
				//                if (activeSpan != null) {
				//                    activeSpan.finish();
				//                }
			}
		}

		return spiResponse;

	}

}
