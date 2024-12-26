package com.github.loadup.components.gateway.cache;

import com.github.loadup.components.gateway.common.util.CacheLogUtil;
import com.github.loadup.components.gateway.core.model.CertAlgorithmConfig;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 * CertAlgorithmConfigCache.java
 * </p>
 */
public class CertAlgorithmConfigCache {

    /**
     * Key is message sender id
     */
    private static Map<String, CertAlgorithmConfig> certAlgorithmConfigMap = new ConcurrentHashMap();

    /**
     * Put all.
     */
    public static void putAll(boolean clear, List<CertAlgorithmConfig> certAlgorithmConfigList) {
        if (CollectionUtils.isEmpty(certAlgorithmConfigList)) {
            return;
        }
        Map<String, CertAlgorithmConfig> tempMap = new ConcurrentHashMap<>();
        certAlgorithmConfigList.forEach(certAlgorithmConfig -> {
            String certCode = certAlgorithmConfig.getCertCode();
            tempMap.put(certCode, certAlgorithmConfig);
        });
        if (clear) {
            certAlgorithmConfigMap = tempMap;
        } else {
            certAlgorithmConfigMap.putAll(tempMap);
        }
        CacheLogUtil.printLog("certAlgorithmConfigMap", certAlgorithmConfigMap);
    }

    /**
     * Gets get with cert code.
     */
    public static CertAlgorithmConfig getWithCertCode(String certCode) {
        return certAlgorithmConfigMap.get(certCode);
    }
}