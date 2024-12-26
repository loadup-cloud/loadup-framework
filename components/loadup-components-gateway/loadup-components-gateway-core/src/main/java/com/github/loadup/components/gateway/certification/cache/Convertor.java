package com.github.loadup.components.gateway.certification.cache;

import com.github.loadup.components.gateway.core.model.*;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * 转换工具类
 */
public class Convertor {

    /**
     * 将appConfig配置list转换为Map格式, key: instId-appId, value: Set<certCode>
     */
    public static Map<String, Set<String>> convertAppConf(List<AppConfig> appConfigs) {
        Map<String, Set<String>> rtn = new HashMap<String, Set<String>>();
        for (AppConfig appConfig : appConfigs) {
            String key = CacheUtil.generateKey(appConfig.getTntInstId(), appConfig.getAppId());
            if (StringUtils.isNotBlank(key)) {
                Set<String> currentSet = rtn.get(key);
                currentSet = initHashSet(currentSet);
                currentSet.add(appConfig.getCertCode());
                rtn.put(key, currentSet);
            }
        }

        return rtn;
    }

    private static Set<String> initHashSet(Set<String> currentSet) {
        if (currentSet == null) {
            currentSet = new HashSet<String>();
        }
        return currentSet;
    }

    /**
     * 将certAlgoMap进行转换为Map格式 <certCode < operateType, CertAlogMap>>
     */
    public static Map<String, Map<String, CertAlogMap>> convertCertAlgoMap(
            List<CertAlogMap> certAlogMaps) {
        Map<String, Map<String, CertAlogMap>> rtn = new HashMap<String, Map<String, CertAlogMap>>();
        if (null == certAlogMaps) {
            return rtn;
        }
        for (CertAlogMap certAlogMap : certAlogMaps) {
            if (StringUtils.isNotBlank(certAlogMap.getCertCode())) {
                Map<String, CertAlogMap> tmpMap = rtn.get(certAlogMap.getCertCode());
                tmpMap = initHashMap(tmpMap);
                tmpMap.put(certAlogMap.getOperateType(), certAlogMap);
                rtn.put(certAlogMap.getCertCode(), tmpMap);
            }
        }
        return rtn;
    }

    private static Map<String, CertAlogMap> initHashMap(Map<String, CertAlogMap> tmpMap) {
        if (tmpMap == null) {
            tmpMap = new HashMap<String, CertAlogMap>();
        }
        return tmpMap;
    }

    /**
     * 将certConfig转换为Map格式
     */
    public static Map<String, Map<String, CertConfig>> convertCertConf(List<CertConfig> certConfigs) {
        Map<String, Map<String, CertConfig>> rtn = new HashMap<String, Map<String, CertConfig>>();
        if (null == certConfigs) {
            return rtn;
        }
        for (CertConfig certConfig : certConfigs) {
            if (StringUtils.isBlank(certConfig.getCertCode())
                    || StringUtils.isBlank(certConfig.getCertType())) {
                continue;
            }

            Map<String, CertConfig> tmp = rtn.get(certConfig.getCertCode());
            if (tmp == null) {
                tmp = new HashMap<String, CertConfig>();
            }
            tmp.put(certConfig.getCertType(), certConfig);

            rtn.put(certConfig.getCertCode(), tmp);
        }
        return rtn;
    }
}