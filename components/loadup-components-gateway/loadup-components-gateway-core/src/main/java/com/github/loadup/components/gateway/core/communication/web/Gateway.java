package com.github.loadup.components.gateway.core.communication.web;

import com.alibaba.fastjson2.JSONObject;
import com.github.loadup.components.gateway.common.util.CommonUtil;
import com.github.loadup.components.gateway.core.common.enums.InterfaceType;
import com.github.loadup.components.gateway.core.common.enums.MessageFormat;
import com.github.loadup.components.gateway.core.model.common.MessageEnvelope;
import com.github.loadup.components.gateway.core.prototype.constant.ProcessConstants;
import com.github.loadup.components.gateway.core.prototype.handler.HttpProcessHandler;
import com.github.loadup.components.gateway.core.prototype.util.ExceptionUtil;
import com.github.loadup.components.gateway.core.prototype.util.HttpToolUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;

import static com.github.loadup.components.gateway.core.common.GatewayliteErrorCode.CONFIGURATION_NOT_FOUND;
import static com.github.loadup.components.gateway.core.prototype.constant.ProcessConstants.KEY_HTTP_INTEGRATION_URL;
import static com.github.loadup.components.gateway.core.prototype.constant.ProcessConstants.KEY_HTTP_INTERFACE_ID;

/**
 *
 */
@Controller
public class Gateway {

    /**
     * Process Handler
     */
    @Resource
    private HttpProcessHandler httpProcessHandler;

    @Value("${use.ac.format.result.when.gateway.exception:false}")
    private String useAcFormatResultWhenGatewayException;

    @RequestMapping(value = "/index.html", method = RequestMethod.GET)
    public void renderIndex(HttpServletRequest request, HttpServletResponse response) {
        try {
            response.getOutputStream().write("Hello, Welcome to Gateway!".getBytes());
        } catch (Exception e) {
            ExceptionUtil.caught(e, "renderIndex error !");
        }
    }

    @RequestMapping(value = "/index.html", method = RequestMethod.HEAD)
    public void renderHead(HttpServletRequest request, HttpServletResponse response) {
    }

    /**
     * OPENAPI
     */
    @RequestMapping(value = "/xx")
    public void receiver(HttpServletRequest request, HttpServletResponse response) {
        try {
            httpProcessHandler.handler(InterfaceType.OPENAPI, request, response);
        } catch (Exception ex) {
            ExceptionUtil.caught(ex, "http error request:");
        }
    }

    /**
     * SPI
     */
    @RequestMapping(value = ProcessConstants.KEY_HTTP_SEND_METHOD)
    public void sender(HttpServletRequest request, HttpServletResponse response) {
        try {
            //校验
            if (verifyParameterOfSpi(request, response)) {
                return;
            }
            httpProcessHandler.handler(InterfaceType.SPI, request, response);
        } catch (Exception ex) {
            // 其他异常打印后结束
            ExceptionUtil.caught(ex, "http error request:");
        }
    }

    /**
     * verify the spi parameter
     *
     * @throws IOException
     */
    private boolean verifyParameterOfSpi(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (StringUtils.isBlank(
                request.getHeader(KEY_HTTP_INTEGRATION_URL)) && StringUtils.isBlank(request.getHeader(KEY_HTTP_INTERFACE_ID))) {
            JSONObject jsonResult = new JSONObject();
            // get switch
            if (Boolean.parseBoolean(useAcFormatResultWhenGatewayException)) {
                // real ac format error result
                jsonResult.put("result",
                        CommonUtil.assembleAcResultWhenException(CONFIGURATION_NOT_FOUND));
            } else {
                // gateway format error result
                jsonResult.put("result", CommonUtil.assembleAcResult(CONFIGURATION_NOT_FOUND));
                jsonResult.put("errorMessage", CONFIGURATION_NOT_FOUND.getMessage());
            }
            MessageEnvelope messageEnvelope = new MessageEnvelope(MessageFormat.TEXT, jsonResult.toJSONString());
            HttpToolUtil.returnResponse(response, messageEnvelope, null);
            return true;
        }
        return false;
    }

}