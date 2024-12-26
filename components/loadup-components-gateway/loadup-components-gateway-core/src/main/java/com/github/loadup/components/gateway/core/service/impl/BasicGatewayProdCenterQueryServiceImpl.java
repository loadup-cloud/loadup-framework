package com.github.loadup.components.gateway.core.service.impl;

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