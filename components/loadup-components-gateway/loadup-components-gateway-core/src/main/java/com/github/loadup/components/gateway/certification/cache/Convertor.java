/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.gateway.certification.cache;

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

import com.github.loadup.components.gateway.core.model.AppConfig;
import com.github.loadup.components.gateway.core.model.CertAlogMap;
import com.github.loadup.components.gateway.core.model.CertConfig;
import java.util.*;
import org.apache.commons.lang3.StringUtils;

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
    public static Map<String, Map<String, CertAlogMap>> convertCertAlgoMap(List<CertAlogMap> certAlogMaps) {
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
            if (StringUtils.isBlank(certConfig.getCertCode()) || StringUtils.isBlank(certConfig.getCertType())) {
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
