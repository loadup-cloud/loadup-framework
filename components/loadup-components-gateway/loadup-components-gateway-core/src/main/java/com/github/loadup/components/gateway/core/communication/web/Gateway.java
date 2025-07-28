///* Copyright (C) LoadUp Cloud 2022-2025 */
//package com.github.loadup.components.gateway.core.communication.web;
//
///*-
// * #%L
// * loadup-components-gateway-core
// * %%
// * Copyright (C) 2022 - 2025 loadup_cloud
// * %%
// * Permission is hereby granted, free of charge, to any person obtaining a copy
// * of this software and associated documentation files (the "Software"), to deal
// * in the Software without restriction, including without limitation the rights
// * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// * copies of the Software, and to permit persons to whom the Software is
// * furnished to do so, subject to the following conditions:
// *
// * The above copyright notice and this permission notice shall be included in
// * all copies or substantial portions of the Software.
// *
// * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// * THE SOFTWARE.
// * #L%
// */
//
//import static com.github.loadup.components.gateway.core.common.GatewayErrorCode.CONFIGURATION_NOT_FOUND;
//import static com.github.loadup.components.gateway.core.prototype.constant.ProcessConstants.KEY_HTTP_INTEGRATION_URL;
//import static com.github.loadup.components.gateway.core.prototype.constant.ProcessConstants.KEY_HTTP_INTERFACE_ID;
//
//import com.alibaba.fastjson2.JSONObject;
//import com.github.loadup.components.gateway.common.util.CommonUtil;
//import com.github.loadup.components.gateway.core.common.enums.InterfaceType;
//import com.github.loadup.components.gateway.core.common.enums.MessageFormat;
//import com.github.loadup.components.gateway.core.model.common.MessageEnvelope;
//import com.github.loadup.components.gateway.core.prototype.constant.ProcessConstants;
//import com.github.loadup.components.gateway.core.prototype.handler.HttpProcessHandler;
//import com.github.loadup.components.gateway.core.prototype.util.ExceptionUtil;
//import com.github.loadup.components.gateway.core.prototype.util.HttpToolUtil;
//import jakarta.annotation.Resource;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import java.io.IOException;
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//
///**
// *
// */
//@Controller
//public class Gateway {
//
//    /**
//     * Process Handler
//     */
//    @Resource
//    private HttpProcessHandler httpProcessHandler;
//
//    @Value("${use.ac.format.result.when.gateway.exception:false}")
//    private String useAcFormatResultWhenGatewayException;
//
//    @RequestMapping(value = "/index.html", method = RequestMethod.GET)
//    public void renderIndex(HttpServletRequest request, HttpServletResponse response) {
//        try {
//            response.getOutputStream().write("Hello, Welcome to Gateway!".getBytes());
//        } catch (Exception e) {
//            ExceptionUtil.caught(e, "renderIndex error !");
//        }
//    }
//
//    @RequestMapping(value = "/index.html", method = RequestMethod.HEAD)
//    public void renderHead(HttpServletRequest request, HttpServletResponse response) {}
//
//    /**
//     * OPENAPI
//     */
//    @RequestMapping(value = "/xx")
//    public void receiver(HttpServletRequest request, HttpServletResponse response) {
//        try {
//            httpProcessHandler.handler(InterfaceType.OPENAPI, request, response);
//        } catch (Exception ex) {
//            ExceptionUtil.caught(ex, "http error request:");
//        }
//    }
//
//    /**
//     * SPI
//     */
//    @RequestMapping(value = ProcessConstants.KEY_HTTP_SEND_METHOD)
//    public void sender(HttpServletRequest request, HttpServletResponse response) {
//        try {
//            // 校验
//            if (verifyParameterOfSpi(request, response)) {
//                return;
//            }
//            httpProcessHandler.handler(InterfaceType.SPI, request, response);
//        } catch (Exception ex) {
//            // 其他异常打印后结束
//            ExceptionUtil.caught(ex, "http error request:");
//        }
//    }
//
//    /**
//     * verify the spi parameter
//     *
//     * @throws IOException
//     */
//    private boolean verifyParameterOfSpi(HttpServletRequest request, HttpServletResponse response) throws IOException {
//        if (StringUtils.isBlank(request.getHeader(KEY_HTTP_INTEGRATION_URL))
//                && StringUtils.isBlank(request.getHeader(KEY_HTTP_INTERFACE_ID))) {
//            JSONObject jsonResult = new JSONObject();
//            // get switch
//            if (Boolean.parseBoolean(useAcFormatResultWhenGatewayException)) {
//                // real ac format error result
//                jsonResult.put("result", CommonUtil.assembleAcResultWhenException(CONFIGURATION_NOT_FOUND));
//            } else {
//                // gateway format error result
//                jsonResult.put("result", CommonUtil.assembleAcResult(CONFIGURATION_NOT_FOUND));
//                jsonResult.put("errorMessage", CONFIGURATION_NOT_FOUND.getMessage());
//            }
//            MessageEnvelope messageEnvelope = new MessageEnvelope(MessageFormat.TEXT, jsonResult.toJSONString());
//            HttpToolUtil.returnResponse(response, messageEnvelope, null);
//            return true;
//        }
//        return false;
//    }
//}
