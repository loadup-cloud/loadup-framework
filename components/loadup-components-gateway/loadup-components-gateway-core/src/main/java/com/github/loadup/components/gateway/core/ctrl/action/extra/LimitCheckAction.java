package com.github.loadup.components.gateway.core.ctrl.action.extra;

import com.github.loadup.components.gateway.common.error.LimitRuleErrorCode;
import com.github.loadup.components.gateway.common.exception.GatewayException;
import com.github.loadup.components.gateway.common.exception.util.AssertUtil;
import com.github.loadup.components.gateway.core.common.util.LimitUtil;
import com.github.loadup.components.gateway.core.ctrl.action.AbstractBusinessAction;
import com.github.loadup.components.gateway.core.ctrl.action.BusinessAction;
import com.github.loadup.components.gateway.core.ctrl.context.GatewayRuntimeProcessContext;
import com.github.loadup.components.gateway.core.model.InterfaceConfig;
import com.github.loadup.components.gateway.core.prototype.util.DigestLoggerUtil;
import com.github.loadup.components.gateway.facade.model.LimitConfig;
import com.github.loadup.components.gateway.facade.spi.LimitRuleService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * <p>
 * LimitCheckAction.java
 * </p>
 */
@Component("limitCheckAction")
public class LimitCheckAction extends AbstractBusinessAction {

    /**
     * limit product center query service
     */
    @Resource
    private LimitUtil limitUtil;

    @Resource
    private LimitRuleService limitRuleService;

    @Override
    public void doBusiness(GatewayRuntimeProcessContext context) {

        InterfaceConfig interfaceConfig = context.getIntegratorInterfaceConfig();

        List<LimitConfig> limitConfigList =
                limitUtil.getLimitConfig(interfaceConfig, context.getTransactionType());

        int size = limitConfigList.size();

        for (int i = 0; i < size; i++) {
            LimitConfig limitConfig = limitConfigList.get(i);
            if (limitConfig.isEnableLimit()) {
                try {
                    applyLimitToken(limitConfig);
                } catch (GatewayException ex) {
                    DigestLoggerUtil.printLimitDigestLog(interfaceConfig.getInterfaceId(),
                            limitConfig.getLimitValue() + "", ex.getErrorCode());
                    throw ex;
                }
            }
        }

        context.setLimitConfigList(limitConfigList);
    }

    /**
     * apply the limit service
     */
    private void applyLimitToken(LimitConfig limitConfig) {

        AssertUtil.isTrue(limitConfig.getLimitValue() != 0, LimitRuleErrorCode.LIMIT_NO_TOKEN,
                "limit value is zero, no token:" + limitConfig.getEntryKeyId());
        if (limitConfig.getLimitValue() > 0) {
            boolean result = limitRuleService.applyToken(limitConfig);
            AssertUtil.isTrue(result, LimitRuleErrorCode.LIMIT_NO_TOKEN,
                    "limit result is not pass, no token:" + limitConfig.getEntryKeyId());
        }
    }

    @Override
    @Resource
    @Qualifier("sendToIntegratorAction")
    public void setNextAction(BusinessAction sendToIntegratorAction) {
        this.nextAction = sendToIntegratorAction;
    }

}