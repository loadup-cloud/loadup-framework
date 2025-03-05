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

import com.github.loadup.components.gateway.core.service.InterfaceProdCenterQueryService;
import com.github.loadup.components.gateway.facade.config.model.APIConditionGroup;
import com.github.loadup.components.gateway.facade.config.model.CommunicationPropertiesGroup;
import com.github.loadup.components.gateway.facade.config.model.SPIConditionGroup;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component("gatewayInterfaceProdCenterQueryService")
public class InterfaceProdCenterQueryServiceImpl implements InterfaceProdCenterQueryService {

    /**
     * 配置中心查询客户端
     */
    //    @Resource
    //    private ConfigDataQueryClientFacade configDataQueryClientFacade;

    /**
     * @see InterfaceProdCenterQueryService#queryAPIConditionGroup(String, String)
     */
    @Override
    public APIConditionGroup queryAPIConditionGroup(String url, String integrationUrl) {
        //        APIConditionGroup result = null;
        //        for (String productCode : Arrays.asList(Constant.PLATFORM_GATEWAY_PRODUCT_CODE,
        //            Constant.TENANT_GATEWAY_PRODUCT_CODE)) {
        //            ClientResponse<APIConditionGroup> clientResponse = configDataQueryClientFacade
        //                .getConditionGroupConfig(
        //                    buildConditionGroupQueryRequest(url, integrationUrl, productCode),
        //                    APIConditionGroup.class);
        //            checkClientResponse(clientResponse, "APIConditionGroup");
        //            result = clientResponse.getResultObj();
        //
        //            if (result == null) {
        //                continue;
        //            }
        //
        //            AssertUtil.isNotBlank(result.getUrl(), GatewayliteErrorCode.CONFIGURATION_LOAD_ERROR,
        //                "api config url query from config center is blank");
        //            AssertUtil.isNotBlank(result.getIntegrationUrl(),
        //                GatewayliteErrorCode.CONFIGURATION_LOAD_ERROR,
        //                "api config integrationUrl query from config center is blank");
        //            AssertUtil.isNotBlank(result.getSecurityStrategyCode(),
        //                GatewayliteErrorCode.CONFIGURATION_LOAD_ERROR,
        //                "api config securityStrategyCode query from config center is blank");
        //            break;
        //        }
        //
        //        AssertUtil.isNotNull(result, GatewayliteErrorCode.CONFIGURATION_LOAD_ERROR,
        //            "APIConditionGroup query from config center is null");
        return null;
    }

    /**
     * @see InterfaceProdCenterQueryService#querySPIConditionGroup(String)
     */
    @Override
    public SPIConditionGroup querySPIConditionGroup(String integrationUrl) {
        //        SPIConditionGroup result = null;
        //        for (String productCode : Arrays.asList(Constant.TENANT_GATEWAY_PRODUCT_CODE,
        //            Constant.PLATFORM_GATEWAY_PRODUCT_CODE)) {
        //            ClientResponse<SPIConditionGroup> clientResponse = configDataQueryClientFacade
        //                .getConditionGroupConfig(
        //                    buildConditionGroupQueryRequest(null, integrationUrl, productCode),
        //                    SPIConditionGroup.class);
        //            checkClientResponse(clientResponse, "SPIConditionGroup");
        //
        //            result = clientResponse.getResultObj();
        //            if (result == null) {
        //                continue;
        //            }
        //            AssertUtil.isNotBlank(result.getIntegrationUrl(),
        //                GatewayliteErrorCode.CONFIGURATION_LOAD_ERROR,
        //                "spi config integrationUrl query from config center is blank");
        //            AssertUtil.isNotBlank(result.getSecurityStrategyCode(),
        //                GatewayliteErrorCode.CONFIGURATION_LOAD_ERROR,
        //                "api config securityStrategyCode query from config center is blank");
        //
        //            break;
        //        }
        //        AssertUtil.isNotNull(result, GatewayliteErrorCode.CONFIGURATION_LOAD_ERROR,
        //            "SPIConditionGroup query from config center is null");
        return null;
    }

    @Override
    public CommunicationPropertiesGroup queryCommunicationPropertiesGroup(String url) {
        //        if (StringUtils.isBlank(TenantUtil.getTenantId())) {
        //            return null;
        //        }
        //        CommunicationPropertiesGroup result = null;
        //        for (String productCode : Arrays.asList(Constant.TENANT_GATEWAY_PRODUCT_CODE,
        //                Constant.PLATFORM_GATEWAY_PRODUCT_CODE)) {
        //            ClientResponse<CommunicationPropertiesGroup> clientResponse = configDataQueryClientFacade
        //                    .getConditionGroupConfig(
        //                            buildConditionGroupQueryRequest(url, null, productCode),
        //                            CommunicationPropertiesGroup.class);
        //            checkClientResponse(clientResponse, "CommunicationPropertiesGroup");
        //            result = clientResponse.getResultObj();
        //
        //            if (result == null) {
        //                continue;
        //            }
        //            break;
        //        }
        return null;
    }

    /**
     * build query request
     *
     */
    //    private ClientConditionGroupRequest buildConditionGroupQueryRequest(String url,
    //                                                                        String integrationUrl,
    //                                                                        String productCode) {
    //
    //        ClientConditionGroupRequest clientConditionGroupRequest = new ClientConditionGroupRequest();
    //
    //        clientConditionGroupRequest.setTntInstId(TenantUtil.getTenantId());
    //        LinkedHashMap<String, String> indexMap = new LinkedHashMap<>();
    //        if (StringUtils.isNotBlank(url)) {
    //            indexMap.put(Constant.URL_INDEX_COLUMN, url);
    //        }
    //        if (StringUtils.isNotBlank(integrationUrl)) {
    //            indexMap.put(Constant.INTEGRATION_URL_INDEX_COLUMN, integrationUrl);
    //        }
    //        clientConditionGroupRequest.setCustomIndexMap(indexMap);
    //
    //        clientConditionGroupRequest.setMainProductCode(productCode);
    //        clientConditionGroupRequest.setProductCode(productCode);
    //        return clientConditionGroupRequest;
    //    }
    //
    //    /**
    //     * validate response
    //
    //
    //
    //     */
    //    private <T> void checkClientResponse(ClientResponse<T> clientResponse,
    //                                         String resultObjectName) {
    //        AssertUtil.isNotNull(clientResponse, GatewayliteErrorCode.CONFIGURATION_LOAD_ERROR,
    //            MessageFormat.format("ClientResponse<{0}> query from config center is null",
    //                resultObjectName));
    //        AssertUtil.isTrue(clientResponse.isSuccess(), GatewayliteErrorCode.CONFIGURATION_LOAD_ERROR,
    //            resultObjectName + " query from config center failed");
    //    }
}
