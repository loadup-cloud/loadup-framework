package com.github.loadup.components.gateway.message.script.cache;

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

import com.github.loadup.components.gateway.core.common.cache.CacheName;
import com.github.loadup.components.gateway.core.common.facade.AbstractReshfreshableComponent;
import com.github.loadup.components.gateway.core.common.facade.Refreshable;
import com.github.loadup.components.gateway.core.model.DynamicScriptBeanConfig;
import com.github.loadup.components.gateway.core.model.InterfaceConfig;
import com.github.loadup.components.gateway.core.model.MessageProcessConfig;
import com.github.loadup.components.gateway.facade.util.LogUtil;
import com.github.loadup.components.gateway.message.script.parser.groovy.GroovyDynamicLoader;
import com.github.loadup.components.gateway.message.script.parser.groovy.GroovyInfo;
import com.github.loadup.components.gateway.message.script.parser.groovy.GroovyInnerCache;
import com.github.loadup.components.gateway.message.script.util.GroovyInfoConvertor;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
@Component
public class MessageComponentCacheManager extends AbstractReshfreshableComponent {

    /**
     * logger
     */
    private static final Logger logger = LoggerFactory.getLogger(MessageComponentCacheManager.class);
    /**
     * groovy缓存加载器
     */
    @Resource
    @Qualifier("liteGroovyDynamicLoader")
    private GroovyDynamicLoader groovyDynamicLoader;

    /**
     * @see Refreshable#init(Object[])
     */
    @Override
    public void init(Object... obj) {
        List<InterfaceConfig> interfaceConfigs = (List<InterfaceConfig>) obj[0];
        List<MessageProcessConfig> processConfig = (List<MessageProcessConfig>) obj[1];
        // dynamic script bean
        List<DynamicScriptBeanConfig> dynamicScriptBeanConfigs = (List<DynamicScriptBeanConfig>) obj[2];
        loadCache(interfaceConfigs, processConfig, dynamicScriptBeanConfigs);
        // refresh dynamic script bean in velocity
        //        refreshVelocityContext(dynamicScriptBeanConfigs);
        isInitOk = true;
    }

    /**
     * @see Refreshable#refresh(Object[])
     */
    @Override
    public void refresh(Object... obj) {
        List<InterfaceConfig> interfaceConfigs = (List<InterfaceConfig>) obj[0];
        List<MessageProcessConfig> processConfig = (List<MessageProcessConfig>) obj[1];
        // dynamic script bean
        List<DynamicScriptBeanConfig> dynamicScriptBeanConfigs = (List<DynamicScriptBeanConfig>) obj[2];
        this.refreshCache(interfaceConfigs, processConfig, dynamicScriptBeanConfigs);
        // refresh dynamic script bean in velocity
        //        refreshVelocityContext(dynamicScriptBeanConfigs);
    }

    /**
     * init loadCache
     */
    private void loadCache(
            List<InterfaceConfig> interfaceConfigs,
            List<MessageProcessConfig> processConfig,
            List<DynamicScriptBeanConfig> dynamicScriptBeanConfigs) {
        // 初始化
        try {
            Map<String, MessageProcessConfig> processConfigMap = convert(processConfig);
            // 加载groovy脚本名称缓存
            GroovyScriptCache.putAll(false, interfaceConfigs, processConfigMap);
            // 加载velocity缓存
            //            VelocityScriptCache.putAll(false, interfaceConfigs, processConfigMap);
            // 从processconfig和dynamic_script_bean中加载groovy脚本转换成GroovyInfo
            List<GroovyInfo> groovyList =
                    GroovyInfoConvertor.convertGroovyInfo(processConfig, dynamicScriptBeanConfigs);
            // 编译groovy脚本
            groovyDynamicLoader.init(false, groovyList);
        } catch (Exception e) {
            LogUtil.error(logger, e, "script message init cache error!");
        }
    }

    /**
     * refreshCache
     */
    private void refreshCache(
            List<InterfaceConfig> interfaceConfigs,
            List<MessageProcessConfig> processConfig,
            List<DynamicScriptBeanConfig> dynamicScriptBeanConfigs) {
        // 初始化
        Map<String, MessageProcessConfig> processConfigMap = convert(processConfig);
        // 加载groovy脚本名称缓存
        GroovyScriptCache.putAll(true, interfaceConfigs, processConfigMap);
        // 加载velocity缓存
        VelocityScriptCache.putAll(true, interfaceConfigs, processConfigMap);
        // 从processconfig和dynamic_script_bean中加载groovy脚本转换成GroovyInfo
        List<GroovyInfo> groovyList = GroovyInfoConvertor.convertGroovyInfo(processConfig, dynamicScriptBeanConfigs);
        // 编译groovy脚本
        groovyDynamicLoader.refreshAll(true, groovyList);
        // 加载校验模板
        /*TODO validator removed
        ValidatorCache.putAll(interfaceConfigs, processConfigMap);*/
    }

    /**
     * convert
     */
    private Map<String, MessageProcessConfig> convert(List<MessageProcessConfig> processConfigs) {
        Map<String, MessageProcessConfig> map = new HashMap<String, MessageProcessConfig>();
        if (CollectionUtils.isEmpty(processConfigs)) {
            return map;
        }
        for (MessageProcessConfig processConfig : processConfigs) {
            map.put(processConfig.getMessageProcessId(), processConfig);
        }
        return map;
    }

    /**
     * @see Refreshable#refreshById(String, Object[])
     */
    @Override
    public void refreshById(String id, Object... obj) {
        List<InterfaceConfig> interfaceConfigs = (List<InterfaceConfig>) obj[0];
        List<MessageProcessConfig> processConfig = (List<MessageProcessConfig>) obj[1];
        List<DynamicScriptBeanConfig> dynamicScriptBeanConfigs = (List<DynamicScriptBeanConfig>) obj[2];

        Map<String, MessageProcessConfig> processConfigMap = convert(processConfig);
        // 先更新groovy脚本，需要筛选出新增和更新的
        List<GroovyInfo>[] groovyInfos =
                convertUpdateGroovyInfo(interfaceConfigs, processConfigMap, dynamicScriptBeanConfigs);
        // 编译groovy脚本
        groovyDynamicLoader.update(groovyInfos);
        GroovyScriptCache.putPart(interfaceConfigs, processConfigMap);
        VelocityScriptCache.putPart(interfaceConfigs, processConfigMap);
        /*TODO validator removed
        ValidatorCache.putPart(interfaceConfigs, processConfigMap);*/

        // refresh velocity dynamic script util bean
        //        refreshVelocityContext(dynamicScriptBeanConfigs);

    }

    /**
     * convertUpdateGroovyInfo
     */
    private List<GroovyInfo>[] convertUpdateGroovyInfo(
            List<InterfaceConfig> interfaceConfigs,
            Map<String, MessageProcessConfig> processConfigMap,
            List<DynamicScriptBeanConfig> dynamicScriptBeanConfigs) {
        List<GroovyInfo> addedList = new ArrayList<GroovyInfo>();
        List<GroovyInfo> updatedList = new ArrayList<GroovyInfo>();
        List<GroovyInfo> deleteddList = new ArrayList<GroovyInfo>();
        for (InterfaceConfig interfaceConfig : interfaceConfigs) {
            String interfaceId = interfaceConfig.getInterfaceId();
            MessageProcessConfig processConfig = processConfigMap.get(interfaceConfig.getMessageProcessorId());
            if (processConfig != null) {
                // 新增脚本
                if (GroovyScriptCache.getBeanName(interfaceId, null, null) == null) {
                    addedList.add(GroovyInfoConvertor.convertGroovyInfo(
                            GroovyScriptCache.generateParserBeanName(processConfig),
                            processConfig.getParserTemplate()));
                } else {
                    updatedList.add(GroovyInfoConvertor.convertGroovyInfo(
                            GroovyScriptCache.generateParserBeanName(processConfig),
                            processConfig.getParserTemplate()));
                }
            }
        }

        // dynamic script util bean
        for (DynamicScriptBeanConfig dynamicScriptBeanConfig : dynamicScriptBeanConfigs) {
            // 新增脚本
            if (GroovyInnerCache.getByName(dynamicScriptBeanConfig.getBeanName()) == null) {
                addedList.add(GroovyInfoConvertor.convertGroovyInfo(
                        dynamicScriptBeanConfig.getBeanName(), dynamicScriptBeanConfig.getBeanContent()));
            } else {
                updatedList.add(GroovyInfoConvertor.convertGroovyInfo(
                        dynamicScriptBeanConfig.getBeanName(), dynamicScriptBeanConfig.getBeanContent()));
            }
        }

        return new List[]{addedList, updatedList, deleteddList};
    }

    /**
     * @see AbstractReshfreshableComponent#getCacheDomains()
     */
    @Override
    public CacheName[] getCacheDomains() {
        return new CacheName[]{CacheName.INTERFACE, CacheName.MESSAGE_PROCESS, CacheName.DYNAMIC_SCRIPT_BEAN_CONFIG};
    }

    /**
     * Setter method for property <tt>groovyDynamicLoader</tt>.
     */
    public void setGroovyDynamicLoader(GroovyDynamicLoader groovyDynamicLoader) {
        this.groovyDynamicLoader = groovyDynamicLoader;
    }
}
