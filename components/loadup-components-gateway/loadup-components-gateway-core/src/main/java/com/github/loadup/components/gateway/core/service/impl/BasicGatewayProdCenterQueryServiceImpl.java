package com.github.loadup.components.gateway.core.service.impl;

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

import com.github.loadup.components.gateway.core.service.BasicGatewayProdCenterQueryService;
import com.github.loadup.components.gateway.facade.config.model.BasicGatewayConditionGroup;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component("gatewayBasicGatewayProdCenterQueryService")
public class BasicGatewayProdCenterQueryServiceImpl implements BasicGatewayProdCenterQueryService {

    /**
     * 配置中心查询客户端
     */
    //    @Resource
    //    private ConfigDataQueryClientFacade configDataQueryClientFacade;

    /**
     * @see BasicGatewayProdCenterQueryService#queryBasicGatewayConditionGroup()
     */
    public BasicGatewayConditionGroup queryBasicGatewayConditionGroup() {
        //        ClientResponse<BasicGatewayConditionGroup> clientResponse = configDataQueryClientFacade
        //            .getConditionGroupConfig(buildConditionGroupQueryRequest(),
        //                BasicGatewayConditionGroup.class);
        //        checkClientResponse(clientResponse, "BasicGatewayConditionGroup");
        //
        //        BasicGatewayConditionGroup gatewayConditionGroup = clientResponse.getResultObj();
        //        AssertUtil.isNotNull(gatewayConditionGroup, GatewayliteErrorCode.CONFIGURATION_LOAD_ERROR,
        //            "BasicGatewayConditionGroup query from config center is null");

        return null;
    }

    /**
     * build query request

     */
    //    private ClientConditionGroupRequest buildConditionGroupQueryRequest() {
    //
    //        ClientConditionGroupRequest clientConditionGroupRequest = new ClientConditionGroupRequest();
    //
    //        clientConditionGroupRequest.setTntInstId(TenantUtil.getTenantId());
    //        clientConditionGroupRequest.setMainProductCode(Constant.PLATFORM_GATEWAY_PRODUCT_CODE);
    //        clientConditionGroupRequest.setProductCode(Constant.PLATFORM_GATEWAY_PRODUCT_CODE);
    //
    //        return clientConditionGroupRequest;
    //    }

    /**
     * validate response



     */
    //    private <T> void checkClientResponse(ClientResponse<T> clientResponse,
    //                                         String resultObjectName) {
    //        AssertUtil.isNotNull(clientResponse, GatewayliteErrorCode.CONFIGURATION_LOAD_ERROR,
    //            MessageFormat.format("ClientResponse<{0}> query from config center is null",
    //                resultObjectName));
    //        AssertUtil.isTrue(clientResponse.isSuccess(), GatewayliteErrorCode.CONFIGURATION_LOAD_ERROR,
    //            resultObjectName + " query from config center failed");
    //    }
}
