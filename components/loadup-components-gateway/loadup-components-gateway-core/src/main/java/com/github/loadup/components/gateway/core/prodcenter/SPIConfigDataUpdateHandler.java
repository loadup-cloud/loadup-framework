package com.github.loadup.components.gateway.core.prodcenter;

import com.github.loadup.components.gateway.facade.config.model.SPIConditionGroup;
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
@Component("gatewaySPIConfigDataUpdateHandler")
public class SPIConfigDataUpdateHandler {

    private static final Logger              logger = LoggerFactory
            .getLogger(SPIConfigDataUpdateHandler.class);
    /**
     * groovy缓存加载器
     */
    @Resource
    @Qualifier("liteGroovyDynamicLoader")
    private              GroovyDynamicLoader groovyDynamicLoader;

    /**
     * @see BaseConfigDataUpdateHandler#process(String, String, Object)
     */
    protected void process(String tntInstId, String configName,
                           SPIConditionGroup spiConditionGroup) {
        LogUtil.info(logger, "update groovy script for spiConditionGroup:", spiConditionGroup);
        //编译groovy脚本
        groovyDynamicLoader
                .update(GroovyInfoConvertor.convertGroovyInfo(spiConditionGroup, tntInstId));
        LogUtil.info(logger, "update groovy script for spiConditionGroup end");
    }

}