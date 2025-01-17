package com.github.loadup.components.gateway.core.prototype.handler;

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
import com.github.loadup.components.gateway.core.common.Constant;
import com.github.loadup.components.gateway.core.common.GatewayErrorCode;
import com.github.loadup.components.gateway.core.common.enums.*;
import com.github.loadup.components.gateway.core.ctrl.RuntimeProcessContextHolder;
import com.github.loadup.components.gateway.core.ctrl.action.atom.ExceptionAssembleAction;
import com.github.loadup.components.gateway.core.ctrl.action.origin.HttpServiceAction;
import com.github.loadup.components.gateway.core.ctrl.context.GatewayRuntimeProcessContext;
import com.github.loadup.components.gateway.core.model.CommunicationConfig;
import com.github.loadup.components.gateway.core.model.common.MessageEnvelope;
import com.github.loadup.components.gateway.core.prototype.constant.ProcessConstants;
import com.github.loadup.components.gateway.core.prototype.parser.SignatureService;
import com.github.loadup.components.gateway.core.prototype.util.*;
import com.github.loadup.components.gateway.facade.util.LogUtil;
import com.github.loadup.components.tracer.TraceUtil;
import io.opentelemetry.api.trace.Span;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.util.Map;

import static com.github.loadup.components.gateway.core.prototype.constant.ProcessConstants.KEY_HTTP_INTEGRATION_URL;
import static com.github.loadup.components.gateway.core.prototype.constant.ProcessConstants.KEY_HTTP_INTERFACE_ID;

/**
 * Http Client Handler
 */
@Component
public class HttpProcessHandler {

	/**
	 * logger
	 */
	private static final Logger logger = LoggerFactory
			.getLogger(HttpProcessHandler.class);

	/**
	 * client-id is dispatch to the merchant
	 */
	public static final String KEY_HTTP_CLIENT_ID = "client-id";

	@Resource
	@Qualifier("httpServiceAction")
	private HttpServiceAction httpServiceAction;

	@Resource
	@Qualifier("exceptionAssembleAction")
	private ExceptionAssembleAction exceptionAssembleAction;

	@Resource
	@Qualifier("acSignatureInfoParserImpl")
	private SignatureService signatureService;

	/**
	 * openapi or spi process
	 * 0. prepare stage, get the request information and covert the data.
	 * 1. parse the incoming request.
	 * 2. obtain the integrationUrl. For spi scenario, get this from the field directly. For openapi, get it from configuration
	 * 3. invoke the integrationUrl.
	 * 4. assemble the response message.
	 */
	public void handler(InterfaceType messageType, HttpServletRequest httpRequest,
						HttpServletResponse httpResponse) {
		String requestURI = httpRequest.getRequestURI();
		StopWatch stopWatch = new StopWatch(requestURI);
		stopWatch.start();
		GatewayRuntimeProcessContext runtimeProcessContext = RuntimeProcessContextHolder
				.createRuntimeProcessContext();
		runtimeProcessContext.setTransactionType(messageType.getCode());
		if (messageType == InterfaceType.SPI) {
			runtimeProcessContext.setNeedStatus(false);
		}

		String callRespMsg = null;
		boolean success = false;
		String tracerId = null;
		try {
			//init tracer
			Span span = TraceUtil.getSpan();
			if (null != span) {
				runtimeProcessContext.setTraceId(TraceUtil.getTracerId());
			} else {
				Span newSpan = TraceUtil.createSpan(Constant.COMPONENT_NAME);
				TraceUtil.logTraceId(newSpan);
				tracerId = TraceUtil.getTracerId();
				runtimeProcessContext.setTraceId(tracerId);
			}

			MDC.put(Constant.TRACER_KEY, runtimeProcessContext.getTraceId());
			byte[] requestBody = HttpToolUtil.getDataFromHttpRequest(httpRequest);
			String requestContent = new String(requestBody);
			MessageEnvelope messageRequestEnvelope = new MessageEnvelope(MessageFormat.TEXT,
					requestContent);
			Map<String, String> headers = HttpToolUtil.getHeaderFromHttpRequest(httpRequest);
			messageRequestEnvelope.setHeaders(headers);

			String signatureStr = signatureService.getSignatureInfo(httpRequest,
					messageRequestEnvelope);
			String originSignatureStr = signatureService.getOriginSignatureInfo(httpRequest,
					messageRequestEnvelope);
			messageRequestEnvelope.putExtMap(ProcessConstants.KEY_HTTP_REQUEST_URI,
					httpRequest.getRequestURI());
			messageRequestEnvelope.putExtMap(ProcessConstants.KEY_HTTP_METHOD,
					httpRequest.getMethod());
			messageRequestEnvelope.setSignatureContent(signatureStr);
			messageRequestEnvelope.setOriginSignContent(originSignatureStr);

			runtimeProcessContext.setRequestMessage(messageRequestEnvelope);
			runtimeProcessContext.setRequesterUri(httpRequest.getRequestURI());
			runtimeProcessContext.setRequesterUrl(httpRequest.getRequestURL().toString());
			runtimeProcessContext.setIntegratorUrl(headers.get(KEY_HTTP_INTEGRATION_URL));
			runtimeProcessContext.setIntegratorInterfaceId(headers.get(KEY_HTTP_INTERFACE_ID));
			runtimeProcessContext.setRequesterHttpMethod(httpRequest.getMethod());
			runtimeProcessContext.setRequesterClientId(headers.get(KEY_HTTP_CLIENT_ID));

			printHttpRequestLog(httpRequest, runtimeProcessContext);

			httpServiceAction.process(runtimeProcessContext);
			callRespMsg = "SUCCESS";
			success = true;
		} catch (CommonException e) {
			stopWatch.stop();
			LogUtil.error(logger, e, "Http process handler fail! throw business exception");
			runtimeProcessContext.setBusinessException(e);
			exceptionAssembleAction.process(runtimeProcessContext);
			callRespMsg = e.getMessage();

			MetricLoggerUtil.countError(httpRequest.getRequestURI(),
					stopWatch.getTotalTimeMillis(), runtimeProcessContext.getRequesterClientId(),
					messageType, runtimeProcessContext.getTraceId(), e.getResultCode());
		} catch (Throwable e) {
			stopWatch.stop();
			LogUtil.error(logger, e, "Http process handler fail! throw exception");
			CommonException commonException = new CommonException(
					GatewayErrorCode.UNKNOWN_EXCEPTION, e);
			runtimeProcessContext.setBusinessException(commonException);
			exceptionAssembleAction.process(runtimeProcessContext);
			callRespMsg = e.getMessage();

			MetricLoggerUtil.countError(httpRequest.getRequestURI(),
					stopWatch.getTotalTimeMillis(), runtimeProcessContext.getRequesterClientId(),
					messageType, runtimeProcessContext.getTraceId(),
					commonException.getResultCode());
		} finally {
			if (stopWatch.isRunning()) {
				stopWatch.stop();
			}
			MessageEnvelope httpResponseMessage = runtimeProcessContext.getResponseMessage();
			CommunicationConfig requesterCommunicationConfig = runtimeProcessContext
					.getRequesterCommunicationConfig();
			HttpToolUtil.returnResponse(httpResponse, httpResponseMessage,
					requesterCommunicationConfig);
			String interfaceId = requesterCommunicationConfig != null
					? requesterCommunicationConfig.getInterfaceId()
					: "";
			String url = requesterCommunicationConfig != null
					? requesterCommunicationConfig.getUrl()
					: "";
			MessageLoggerUtil.printHttpReceiveLog(interfaceId, InterfaceType.OPENAPI,
					runtimeProcessContext.getTraceId(), url, httpRequest.getMethod(),
					CommonUtil.getMsgContent(httpResponseMessage), httpResponseMessage.getHeaders());

			DigestLoggerUtil.printInfoLog(runtimeProcessContext,
					stopWatch.getTotalTimeMillis());

			if (!MetricLoggerUtil.judgeBinderEnabled()) {
				DigestLoggerUtil.printSimpleDigestLog(httpRequest.getRequestURI(), callRespMsg,
						stopWatch.getTotalTimeMillis());
			} else {
				MetricLoggerUtil.monitor(httpRequest.getRequestURI(),
						stopWatch.getTotalTimeMillis(), success,
						runtimeProcessContext.getRequesterClientId(), messageType,
						runtimeProcessContext.getTraceId(), InterfaceScope.INBOUND);
			}
			RuntimeProcessContextHolder.cleanActionContext();
			MDC.clear();

			// clear tracer
			if (StringUtils.isNotBlank(tracerId)) {
				//                Span activeSpan = TracerUtil.getSpan();
				//                if (activeSpan != null) {
				//                    activeSpan.finish();
				//                }
			}
		}

	}

	private void printHttpRequestLog(HttpServletRequest httpRequest,
									GatewayRuntimeProcessContext runtimeProcessContext) {
		MessageEnvelope httpRequestMessage = runtimeProcessContext.getRequestMessage();
		CommunicationConfig integratorCommunicationConfig = runtimeProcessContext
				.getIntegratorCommunicationConfig();
		String interfaceId = integratorCommunicationConfig != null
				? integratorCommunicationConfig.getInterfaceId()
				: "";
		interfaceId = StringUtils.defaultIfBlank(interfaceId, httpRequest.getRequestURI());
		MessageLoggerUtil.printHttpLog("get request attributes", interfaceId, InterfaceType.OPENAPI,
				runtimeProcessContext.getTraceId(), httpRequest.getRequestURI(),
				httpRequest.getMethod(), CommonUtil.getMsgContent(httpRequestMessage),
				httpRequestMessage.getHeaders());
	}

}
