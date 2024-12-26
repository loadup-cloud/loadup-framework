package com.github.loadup.components.gateway.message.script.util;

import com.github.loadup.components.gateway.core.common.Constant;
import com.github.loadup.components.gateway.core.common.enums.InterfaceType;
import com.github.loadup.components.gateway.core.common.enums.RoleType;
import com.github.loadup.components.gateway.core.model.DynamicScriptBeanConfig;
import com.github.loadup.components.gateway.core.model.MessageProcessConfig;
import com.github.loadup.components.gateway.facade.config.model.APIConditionGroup;
import com.github.loadup.components.gateway.facade.config.model.SPIConditionGroup;
import com.github.loadup.components.gateway.message.script.cache.GroovyScriptCache;
import com.github.loadup.components.gateway.message.script.parser.groovy.GroovyInfo;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class GroovyInfoConvertor {

    /**
     * 转换groovyInfo对象
     */
    public static GroovyInfo convertGroovyInfo(String beanName, String content) {
        GroovyInfo info = new GroovyInfo();
        info.setClassName(beanName);
        info.setGroovyContent(content);
        return info;
    }

    /**
     * 转换groovyInfo对象列表
     */
    public static List<GroovyInfo> convertGroovyInfo(List<MessageProcessConfig> processConfigs,
                                                     List<DynamicScriptBeanConfig> dynamicScriptBeanConfigs) {
        List<GroovyInfo> list = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(processConfigs)) {
            for (MessageProcessConfig processConfig : processConfigs) {
                list.add(GroovyInfoConvertor.convertGroovyInfo(GroovyScriptCache.generateParserBeanName(processConfig),
                        processConfig.getParserTemplate()));
                list.add(GroovyInfoConvertor.convertGroovyInfo(GroovyScriptCache.generateAssembleBeanName(processConfig),
                        processConfig.getAssembleTemplate()));
            }
        }
        if (CollectionUtils.isNotEmpty(dynamicScriptBeanConfigs)) {
            for (DynamicScriptBeanConfig dynamicScriptBeanConfig : dynamicScriptBeanConfigs) {
                list.add(GroovyInfoConvertor.convertGroovyInfo(dynamicScriptBeanConfig.getBeanName(),
                        dynamicScriptBeanConfig.getBeanContent()));
            }
        }
        return list;
    }

    /**
     * convert api sender request parser groovy script and api receiver response parser groovy script to GroovyInfos
     */
    public static List<GroovyInfo>[] convertGroovyInfo(APIConditionGroup apiConditionGroup, String tntIntId) {
        List<GroovyInfo>[] result = new List[3];
        List<GroovyInfo> addList = new ArrayList<>();
        List<GroovyInfo> updateList = new ArrayList<>();
        List<GroovyInfo> deleteList = new ArrayList<>();
        String requestParserBeanName = GroovyScriptCache.generateParserBeanName(apiConditionGroup.getUrl(), tntIntId,
                RoleType.SENDER.getCode(), InterfaceType.OPENAPI.getCode());

        // PARSE_TEMPLATE_NAME_PREFIX 前缀为网关自带的解析模板工厂，不需要额外注册groovyBean
        if (StringUtils.isNotBlank(apiConditionGroup.getInterfaceRequestParser()) && !StringUtils.startsWith(
                apiConditionGroup.getInterfaceRequestParser(), Constant.PARSE_TEMPLATE_NAME_PREFIX)) {
            GroovyInfo info = convertGroovyInfo(requestParserBeanName, apiConditionGroup.getInterfaceRequestParser());
            updateList.add(info);
        } else {
            GroovyInfo info = convertGroovyInfo(requestParserBeanName, null);
            deleteList.add(info);
        }
        String responseParserBeanName = GroovyScriptCache.generateParserBeanName(apiConditionGroup.getIntegrationUrl(), tntIntId,
                RoleType.RECEIVER.getCode(), InterfaceType.OPENAPI.getCode());
        if (StringUtils.isNotBlank(apiConditionGroup.getIntegrationResponseParser()) && !StringUtils.startsWith(
                apiConditionGroup.getInterfaceRequestParser(), Constant.PARSE_TEMPLATE_NAME_PREFIX)) {
            GroovyInfo info = convertGroovyInfo(responseParserBeanName, apiConditionGroup.getIntegrationResponseParser());
            updateList.add(info);
        } else {
            GroovyInfo info = convertGroovyInfo(responseParserBeanName, null);
            deleteList.add(info);
        }
        result[0] = addList;
        result[1] = updateList;
        result[2] = deleteList;
        return result;
    }

    /**
     * convert spi receiver response parser groovy script to GroovyInfos
     */
    public static List<GroovyInfo>[] convertGroovyInfo(SPIConditionGroup spiConditionGroup, String tntIntId) {
        List<GroovyInfo>[] result = new List[3];
        List<GroovyInfo> addList = new ArrayList<>();
        List<GroovyInfo> updateList = new ArrayList<>();
        List<GroovyInfo> deleteList = new ArrayList<>();
        String responseParserBeanName = GroovyScriptCache.generateParserBeanName(spiConditionGroup.getIntegrationUrl(), tntIntId,
                RoleType.RECEIVER.getCode(), InterfaceType.SPI.getCode());
        // PARSE_TEMPLATE_NAME_PREFIX 前缀为网关自带的解析模板工厂，不需要额外注册groovyBean
        if (StringUtils.isNotBlank(spiConditionGroup.getIntegrationResponseParser()) && !StringUtils.startsWith(
                spiConditionGroup.getInterfaceRequestParser(), Constant.PARSE_TEMPLATE_NAME_PREFIX)) {
            GroovyInfo info = convertGroovyInfo(responseParserBeanName, spiConditionGroup.getIntegrationResponseParser());
            updateList.add(info);
        } else {
            GroovyInfo info = convertGroovyInfo(responseParserBeanName, null);
            deleteList.add(info);
        }
        result[0] = addList;
        result[1] = updateList;
        result[2] = deleteList;
        return result;
    }
}