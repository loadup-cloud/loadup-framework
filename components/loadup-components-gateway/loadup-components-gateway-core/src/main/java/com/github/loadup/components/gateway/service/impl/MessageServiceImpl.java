package com.github.loadup.components.gateway.service.impl;

import com.github.loadup.components.gateway.common.exception.GatewayException;
import com.github.loadup.components.gateway.common.util.CommonUtil;
import com.github.loadup.components.gateway.common.util.ResultUtil;
import com.github.loadup.components.gateway.core.common.Constant;
import com.github.loadup.components.gateway.core.common.GatewayliteErrorCode;
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
        } catch (GatewayException e) {
            LogUtil.error(logger, e, "Message Service send message fail, throw business exception");
            processContext.setBusinessException(e);
            exceptionAssembleAction.process(processContext);
            callRespMsg = e.getMessage();
            spiResponse.setResult(ResultUtil.buildResult(e.getErrorCode()));
            MetricLoggerUtil.countError(request.getIntegrationUrl(),
                    System.currentTimeMillis() - timecost,
                    request.getMessage().get(Constant.KEY_HTTP_CLIENT_ID), InterfaceType.SPI,
                    processContext.getTraceId(), e.getErrorCode());
        } catch (Throwable e) {
            LogUtil.error(logger, e, "Message Service send message fail! throw exception");
            GatewayException gatewayException = new GatewayException(
                    GatewayliteErrorCode.UNKNOWN_EXCEPTION, e);
            processContext.setBusinessException(gatewayException);
            exceptionAssembleAction.process(processContext);
            callRespMsg = e.getMessage();
            spiResponse.setResult(
                    ResultUtil.buildResult(GatewayliteErrorCode.UNKNOWN_EXCEPTION, e.getMessage()));

            MetricLoggerUtil.countError(request.getIntegrationUrl(),
                    System.currentTimeMillis() - timecost,
                    request.getMessage().get(Constant.KEY_HTTP_CLIENT_ID), InterfaceType.SPI,
                    processContext.getTraceId(), gatewayException.getErrorCode());
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