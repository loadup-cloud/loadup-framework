package com.github.loadup.components.gateway.core.prodcenter;

import com.github.loadup.components.gateway.cache.CertConfigCache;
import com.github.loadup.components.gateway.common.convertor.CertConfigConvertor;
import com.github.loadup.components.gateway.core.model.CertConfig;
import com.github.loadup.components.gateway.facade.config.model.SecurityConditionGroup;
import com.github.loadup.components.gateway.facade.util.LogUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * Security config data init callback handler for product center repository
 */
@Component("gatewaySecurityConfigDataInitHandler")
public class SecurityConfigDataInitHandler {

    private static final Logger logger = LoggerFactory
            .getLogger(SecurityConfigDataInitHandler.class);

    /**
     *
     */
    protected void process(String tntInstId, String configName,
                           SecurityConditionGroup securityConditionGroup) {
        LogUtil.info(logger, "init security config for securityConditionGroup:",
                securityConditionGroup);
        CertConfig certConfig = CertConfigConvertor.convertToCertConfig(securityConditionGroup,
                tntInstId);
        CertConfigCache.putAll(false, Collections.singletonList(certConfig));
        LogUtil.info(logger, "init security config for securityConditionGroup end");
    }

}