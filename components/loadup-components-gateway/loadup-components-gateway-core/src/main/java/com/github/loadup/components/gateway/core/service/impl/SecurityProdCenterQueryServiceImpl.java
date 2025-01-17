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

import com.github.loadup.components.gateway.common.exception.util.AssertUtil;
import com.github.loadup.components.gateway.core.common.GatewayErrorCode;
import com.github.loadup.components.gateway.core.service.SecurityProdCenterQueryService;
import com.github.loadup.components.gateway.facade.config.model.Constant;
import com.github.loadup.components.gateway.facade.config.model.SecurityConditionGroup;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 *
 */
@Component("gatewaySecurityProdCenterQueryServiceImpl")
public class SecurityProdCenterQueryServiceImpl implements SecurityProdCenterQueryService {

	/**
	 * 配置中心查询客户端
	 */
	//    @Resource
	//    private ConfigDataQueryClientFacade configDataQueryClientFacade;

	/**
	 * @see SecurityProdCenterQueryService#querySecurityConditionGroup(String, String, String, String)
	 */
	@Override
	public SecurityConditionGroup querySecurityConditionGroup(String securityStrategyCode,
															String operateType, String algorithm,
															String clientId) {
		SecurityConditionGroup result = null;
		for (String productCode : Arrays.asList(Constant.TENANT_GATEWAY_PRODUCT_CODE,
				Constant.PLATFORM_GATEWAY_PRODUCT_CODE)) {

			//            ClientResponse<SecurityConditionGroup> clientResponse = configDataQueryClientFacade
			//                .getConditionGroupConfig(buildConditionGroupQueryRequest(securityStrategyCode,
			//                    operateType, algorithm, productCode), SecurityConditionGroup.class);
			//            checkClientResponse(clientResponse, "SecurityConditionGroup");

			result = null;// clientResponse.getResultObj();
			if (result == null) {
				continue;
			}
			AssertUtil.isNotBlank(result.getSecurityStrategyKey(),
					GatewayErrorCode.CONFIGURATION_LOAD_ERROR,
					"security config key query from config center is blank");
			AssertUtil.isNotBlank(result.getCertType(),
					GatewayErrorCode.CONFIGURATION_LOAD_ERROR,
					"security config cert type query from config center is blank");
			break;
		}
		AssertUtil.isNotNull(result, GatewayErrorCode.CONFIGURATION_LOAD_ERROR,
				"SecurityConditionGroup query from config center is null");
		return result;
	}

	/**
	 * build query request

	 */
	//    private ClientConditionGroupRequest buildConditionGroupQueryRequest(String securityStrategyCode,
	//                                                                        String operateType,
	//                                                                        String algorithm,
	//                                                                        String productCode) {
	//
	//        ClientConditionGroupRequest clientConditionGroupRequest = new ClientConditionGroupRequest();
	//
	//        clientConditionGroupRequest.setTntInstId(TenantUtil.getTenantId());
	//        clientConditionGroupRequest.setMainProductCode(productCode);
	//        clientConditionGroupRequest.setProductCode(productCode);
	//
	//        LinkedHashMap<String, String> indexMap = new LinkedHashMap<>();
	//        indexMap.put(Constant.SECURITY_CONDITION_GROUP_INDEX_COLUMN_CODE, securityStrategyCode);
	//        indexMap.put(Constant.SECURITY_CONDITION_GROUP_INDEX_COLUMN_OPERATE_TYPE, operateType);
	//        indexMap.put(Constant.SECURITY_CONDITION_GROUP_INDEX_COLUMN_ALGO, algorithm);
	//        clientConditionGroupRequest.setCustomIndexMap(indexMap);
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
