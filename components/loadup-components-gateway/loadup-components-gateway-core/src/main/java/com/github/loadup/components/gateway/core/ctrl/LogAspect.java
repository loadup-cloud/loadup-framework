package com.github.loadup.components.gateway.core.ctrl;

import com.github.loadup.components.gateway.common.util.CommonUtil;
import com.github.loadup.components.gateway.core.communication.http.util.SensitivityUtil;
import com.github.loadup.components.gateway.core.ctrl.context.GatewayRuntimeProcessContext;
import com.github.loadup.components.gateway.core.model.ShieldConfig;
import com.github.loadup.components.gateway.core.model.ShieldType;
import com.github.loadup.components.gateway.facade.util.LogUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * <p>
 * LogAspect.java
 * </p>
 */
@Aspect
@Component
@Lazy(false)
public class LogAspect {

    private static final Logger logger = LoggerFactory
            .getLogger("DIGEST-MESSAGE-LOGGER");

    @Pointcut("@annotation(com.github.loadup.components.gateway.core.common.annotation.LogTraceId)")
    public void logPointCut() {
    }

    @Around("logPointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        long timeCost = System.currentTimeMillis();
        Object result = point.proceed();
        try {
            Object[] objects = point.getArgs();
            String className = point.getTarget().getClass().getName();
            GatewayRuntimeProcessContext context = (GatewayRuntimeProcessContext) objects[0];
            timeCost = System.currentTimeMillis() - timeCost;
            String traceId = context.getTraceId();
            String resultContent = CommonUtil.getMsgContent(context.getResultMessage());

            Map<String, ShieldType> shieldRules = ShieldConfig
                    .getShieldRules(ShieldConfig.KEY_DEFAULT, null);

            resultContent = SensitivityUtil.mask(resultContent, shieldRules,
                    SensitivityUtil.matchProcessTypeByString(resultContent));

            String requestContent = CommonUtil.getMsgContent(context.getRequestMessage());

            requestContent = SensitivityUtil.mask(requestContent, shieldRules,
                    SensitivityUtil.matchProcessTypeByString(requestContent));

            String responseContent = CommonUtil.getMsgContent(context.getResponseMessage());

            responseContent = SensitivityUtil.mask(responseContent, shieldRules,
                    SensitivityUtil.matchProcessTypeByString(responseContent));

            LogUtil.info(logger, "className:", className, ", traceId:", traceId, ", time cost is ",
                    timeCost, "ms");

            if (logger.isDebugEnabled()) {
                LogUtil.debug(logger, "className:", className, ", traceId:", traceId,
                        ", request args:", requestContent, ", response:", responseContent, ", result:",
                        resultContent, ", time cost is ", timeCost, "ms");
            }
        } catch (Throwable e) {
            LogUtil.error(logger, e, "Print message log fail.");
        }
        return result;
    }

}