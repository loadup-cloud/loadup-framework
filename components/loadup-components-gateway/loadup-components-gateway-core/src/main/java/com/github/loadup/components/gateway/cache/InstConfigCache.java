package com.github.loadup.components.gateway.cache;

import com.github.loadup.components.gateway.common.util.CacheLogUtil;
import com.github.loadup.components.gateway.core.model.InstConfig;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 * InstConfigCache.java
 * </p>
 */
public final class InstConfigCache {

    private static Map<String, InstConfig> instConfigMap = new ConcurrentHashMap<>();

    /**
     * build the cache
     */
    public static void putAll(boolean clear, List<InstConfig> instConfigList) {
        if (CollectionUtils.isEmpty(instConfigList)) {
            return;
        }

        Map<String, InstConfig> tempMap = new ConcurrentHashMap<>();
        instConfigList.forEach(instConfig -> tempMap.put(instConfig.getClientId(), instConfig));
        if (clear) {
            instConfigMap = tempMap;
        } else {
            instConfigMap.putAll(tempMap);
        }
        CacheLogUtil.printLog("instConfigMap", instConfigMap);
    }

    /**
     * get with client Id
     */
    public static InstConfig getWithClientId(String clientId) {
        return instConfigMap.get(clientId);
    }
}