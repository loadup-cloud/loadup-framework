package com.github.loadup.components.gateway.core.ctrl.action.origin;

import com.github.loadup.components.gateway.cache.CommunicationConfigCache;
import com.github.loadup.components.gateway.cache.InterfaceConfigCache;
import com.github.loadup.components.gateway.common.exception.GatewayException;
import com.github.loadup.components.gateway.core.common.GatewayliteErrorCode;
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
    public void doBusiness(GatewayRuntimeProcessContext gatewayRuntimeProcessContext) throws GatewayException {
        String integratorUrl = gatewayRuntimeProcessContext.getIntegratorUrl();
        //CommunicationConfig integratorCommunicationConfig = interfaceCacheManager.getCommunicationConfigByURI(integratorUrl);
        CommunicationConfig integratorCommunicationConfig = CommunicationConfigCache.getWithUrl(integratorUrl,
                gatewayRuntimeProcessContext.getTransactionType());
        if (integratorCommunicationConfig == null) {
            String integratorInterfaceId = gatewayRuntimeProcessContext.getIntegratorInterfaceId();
            //integratorCommunicationConfig = interfaceCacheManager.getCommunicationConfigByInterfaceId(integratorInterfaceId);
            // spi场景，prodcenter不会根据interfaceId查找配置，故如果是prodcenter则不用处理
            integratorCommunicationConfig = CommunicationConfigCache.getWithInterfaceId(integratorInterfaceId);
        }
        if (integratorCommunicationConfig == null) {
            GatewayException gatewayException = new GatewayException(GatewayliteErrorCode.PARAM_ILLEGAL);
            gatewayRuntimeProcessContext.setBusinessException(gatewayException);
            throw gatewayException;
        }
        //InterfaceConfig integratorInterfaceConfig = interfaceCacheManager.getInterfaceConfigById(integratorCommunicationConfig
        // .getInterfaceId());
        InterfaceConfig integratorInterfaceConfig = InterfaceConfigCache.getWithInterfaceId(
                integratorCommunicationConfig.getInterfaceId(), RoleType.RECEIVER,
                gatewayRuntimeProcessContext.getTransactionType());
        gatewayRuntimeProcessContext.setIntegratorUrl(integratorUrl);
        gatewayRuntimeProcessContext.setIntegratorCommunicationConfig(integratorCommunicationConfig);
        gatewayRuntimeProcessContext.setIntegratorInterfaceConfig(integratorInterfaceConfig);

        gatewayRuntimeProcessContext.setResultMessage(gatewayRuntimeProcessContext.getRequestMessage().clone());

    }

    @Override
    @Resource
    @Qualifier("requestAssembleAction")
    public void setNextAction(BusinessAction requestParseAction) {
        this.nextAction = requestParseAction;
    }
}