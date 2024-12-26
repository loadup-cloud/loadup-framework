package com.github.loadup.components.gateway.message.script.parser.groovy;

import com.github.loadup.components.gateway.facade.util.LogUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * <p>
 * groovy脚本内部缓存
 */
public class GroovyInnerCache {

    /**
     * logger
     */
    private static final Logger logger = LoggerFactory
            .getLogger(GroovyInnerCache.class);

    /**
     * 脚本列表
     */
    private static ConcurrentMap<String, GroovyInfo> groovyMap = new ConcurrentHashMap<String, GroovyInfo>();

    /**
     * 把脚本缓存一下
     */
    public static void put2map(boolean clear, List<GroovyInfo> groovyList) {
        // 先清空
        if (clear && !groovyMap.isEmpty()) {
            groovyMap.clear();
        }
        for (GroovyInfo groovyInfo : groovyList) {
            String scriptName = groovyInfo.getClassName();
            if (!groovyMap.containsKey(scriptName)) {
                groovyMap.put(scriptName, groovyInfo);
            } else {
                LogUtil.warn(logger, "duplicate groovy script with same name:" + groovyInfo);
            }
        }
    }

    /**
     * 更新map
     */
    public static void update2map(List<GroovyInfo>[] groovyInfos) {
        List<GroovyInfo> addedGroovyInfos = groovyInfos[0];
        List<GroovyInfo> updatedGroovyInfos = groovyInfos[1];
        List<GroovyInfo> deletedGroovyInfos = groovyInfos[2];
        addMap(addedGroovyInfos);
        addMap(updatedGroovyInfos);
        removeMap(deletedGroovyInfos);
    }

    /**
     * 新增
     */
    private static void addMap(List<GroovyInfo> groovyList) {
        for (GroovyInfo groovyInfo : groovyList) {
            groovyMap.put(groovyInfo.getClassName(), groovyInfo);
        }
    }

    /**
     * 删除
     */
    private static void removeMap(List<GroovyInfo> groovyList) {
        for (GroovyInfo groovyInfo : groovyList) {
            groovyMap.remove(groovyInfo.getClassName());
        }
    }

    /**
     * 根据名称获取脚本信息
     */
    public static GroovyInfo getByName(String scriptName) {
        return groovyMap.get(scriptName);
    }

    /**
     * 获取已经加载的全部脚本类名
     */
    public static Map<String, GroovyInfo> getGroovyInfos() {
        return groovyMap;
    }
}
