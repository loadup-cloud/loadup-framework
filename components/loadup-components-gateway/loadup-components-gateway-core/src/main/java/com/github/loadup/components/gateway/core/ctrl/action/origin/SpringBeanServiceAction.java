package com.github.loadup.components.gateway.core.ctrl.action.origin;

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
import com.github.loadup.components.gateway.cache.CommunicationConfigCache;
import com.github.loadup.components.gateway.cache.InterfaceConfigCache;
import com.github.loadup.components.gateway.core.common.GatewayErrorCode;
import com.github.loadup.components.gateway.core.common.annotation.LogTraceId;
import com.github.loadup.components.gateway.core.common.enums.RoleType;
import com.github.loadup.components.gateway.core.ctrl.action.AbstractBusinessAction;
import com.github.loadup.components.gateway.core.ctrl.action.BusinessAction;
import com.github.loadup.components.gateway.core.ctrl.context.GatewayRuntimeProcessContext;
import com.github.loadup.components.gateway.core.model.CommunicationConfig;
import com.github.loadup.components.gateway.core.model.InterfaceConfig;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * <p>
 * DoNotingAction.java
 * </p>
 * SpringBeanServiceAction -> RequestAssembleAction -> SendToIntegratorAction -> ResponseParseAction -> ResponseAssembleAction
 */
@Component("springBeanServiceAction")
public class SpringBeanServiceAction extends AbstractBusinessAction {

    @Override
    @LogTraceId
    public void doBusiness(GatewayRuntimeProcessContext gatewayRuntimeProcessContext) throws CommonException {
        String integratorUrl = gatewayRuntimeProcessContext.getIntegratorUrl();
        // CommunicationConfig integratorCommunicationConfig =
        // interfaceCacheManager.getCommunicationConfigByURI(integratorUrl);
        CommunicationConfig integratorCommunicationConfig =
                CommunicationConfigCache.getWithUrl(integratorUrl, gatewayRuntimeProcessContext.getTransactionType());
        if (integratorCommunicationConfig == null) {
            String integratorInterfaceId = gatewayRuntimeProcessContext.getIntegratorInterfaceId();
            // integratorCommunicationConfig =
            // interfaceCacheManager.getCommunicationConfigByInterfaceId(integratorInterfaceId);
            // spi场景，prodcenter不会根据interfaceId查找配置，故如果是prodcenter则不用处理
            integratorCommunicationConfig = CommunicationConfigCache.getWithInterfaceId(integratorInterfaceId);
        }
        if (integratorCommunicationConfig == null) {
            CommonException commonException = new CommonException(GatewayErrorCode.PARAM_ILLEGAL);
            gatewayRuntimeProcessContext.setBusinessException(commonException);
            throw commonException;
        }
        // InterfaceConfig integratorInterfaceConfig =
        // interfaceCacheManager.getInterfaceConfigById(integratorCommunicationConfig
        // .getInterfaceId());
        InterfaceConfig integratorInterfaceConfig = InterfaceConfigCache.getWithInterfaceId(
                integratorCommunicationConfig.getInterfaceId(),
                RoleType.RECEIVER,
                gatewayRuntimeProcessContext.getTransactionType());
        gatewayRuntimeProcessContext.setIntegratorUrl(integratorUrl);
        gatewayRuntimeProcessContext.setIntegratorCommunicationConfig(integratorCommunicationConfig);
        gatewayRuntimeProcessContext.setIntegratorInterfaceConfig(integratorInterfaceConfig);

        gatewayRuntimeProcessContext.setResultMessage(
                gatewayRuntimeProcessContext.getRequestMessage().clone());
    }

    @Override
    @Resource
    @Qualifier("requestAssembleAction")
    public void setNextAction(BusinessAction requestParseAction) {
        this.nextAction = requestParseAction;
    }
}
