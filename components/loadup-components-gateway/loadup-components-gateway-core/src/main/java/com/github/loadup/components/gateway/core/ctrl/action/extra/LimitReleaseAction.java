package com.github.loadup.components.gateway.core.ctrl.action.extra;

import com.github.loadup.components.gateway.core.ctrl.action.AbstractBusinessAction;
import com.github.loadup.components.gateway.core.ctrl.action.BusinessAction;
import com.github.loadup.components.gateway.core.ctrl.context.GatewayRuntimeProcessContext;
import com.github.loadup.components.gateway.facade.enums.LimitTimeRuleEnum;
import com.github.loadup.components.gateway.facade.model.LimitConfig;
import com.github.loadup.components.gateway.facade.spi.LimitRuleService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * <p>
 * LimitReleaseAction.java
 * </p>
 */
@Component("limitReleaseAction")
public class LimitReleaseAction extends AbstractBusinessAction {

    @Resource
    private LimitRuleService limitRuleService;

    @Override
    protected void doBusiness(GatewayRuntimeProcessContext context) {
        List<LimitConfig> limitConfigList = context.getLimitConfigList();
        if (!CollectionUtils.isEmpty(limitConfigList)) {
            for (LimitConfig limitConfig : limitConfigList) {
                if (limitConfig.getLimitTimeRule() == LimitTimeRuleEnum.CONCURRENCY) {
                    limitRuleService.resetToken(limitConfig);
                }
            }
        }
    }

    @Override
    @Resource
    @Qualifier("responseAssembleAction")
    public void setNextAction(BusinessAction responseAssembleAction) {
        this.nextAction = responseAssembleAction;
    }
}