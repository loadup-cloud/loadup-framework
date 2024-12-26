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
 * spi config data init callback handler for product center repository
 */
@Component("gatewaySPIConfigDataInitHandler")
public class SPIConfigDataInitHandler {

    private static final Logger logger = LoggerFactory
            .getLogger(SPIConfigDataInitHandler.class);

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
                           SPIConditionGroup spiConditionGroup) {
        LogUtil.info(logger, "init groovy script for spiConditionGroup:", spiConditionGroup);
        //编译groovy脚本
        groovyDynamicLoader.init(false,
                GroovyInfoConvertor.convertGroovyInfo(spiConditionGroup, tntInstId)[1]);
        LogUtil.info(logger, "init groovy script for spiConditionGroup end");
    }
}