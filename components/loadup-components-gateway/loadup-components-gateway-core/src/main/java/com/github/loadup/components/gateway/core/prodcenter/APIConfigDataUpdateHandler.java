package com.github.loadup.components.gateway.core.prodcenter;

import com.github.loadup.components.gateway.facade.config.model.APIConditionGroup;
import com.github.loadup.components.gateway.facade.util.LogUtil;
import com.github.loadup.components.gateway.message.script.parser.groovy.GroovyDynamicLoader;
import com.github.loadup.components.gateway.message.script.util.GroovyInfoConvertor;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component("gatewayAPIConfigDataUpdateHandler")
public class APIConfigDataUpdateHandler {

    private static final Logger logger = LoggerFactory
            .getLogger(APIConfigDataUpdateHandler.class);

    /**
     * groovy缓存加载器
     */
    @Resource
    @Qualifier("liteGroovyDynamicLoader")
    private GroovyDynamicLoader groovyDynamicLoader;

    /**
     *
     */
    protected void process(String tntInstId, String configName,
                           APIConditionGroup apiConditionGroup) {
        LogUtil.info(logger, "update groovy script for apiConditionGroup:", apiConditionGroup);
        //编译groovy脚本
        groovyDynamicLoader
                .update(GroovyInfoConvertor.convertGroovyInfo(apiConditionGroup, tntInstId));
        LogUtil.info(logger, "update groovy script for apiConditionGroup end");
    }

}