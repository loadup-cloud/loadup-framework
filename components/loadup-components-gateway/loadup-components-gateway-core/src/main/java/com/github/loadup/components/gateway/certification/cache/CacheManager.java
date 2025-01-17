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

import com.github.loadup.components.gateway.certification.exception.CertificationErrorCode;
import com.github.loadup.components.gateway.certification.exception.CertificationException;
import com.github.loadup.components.gateway.certification.model.CertContentType;
import com.github.loadup.components.gateway.certification.model.CertificationFactor;
import com.github.loadup.components.gateway.certification.spi.CertGetService;
import com.github.loadup.components.gateway.certification.util.CommonUtil;
import com.github.loadup.components.gateway.common.util.CacheLogUtil;
import com.github.loadup.components.gateway.core.common.facade.Refreshable;
import com.github.loadup.components.gateway.core.model.AppConfig;
import com.github.loadup.components.gateway.core.model.CertAlgoScript;
import com.github.loadup.components.gateway.core.model.CertAlogMap;
import com.github.loadup.components.gateway.core.model.CertConfig;
import com.github.loadup.components.gateway.facade.enums.CertTypeEnum;
import com.github.loadup.components.gateway.facade.util.LogUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 缓存管理
 */
@Component("certCacheManager")
public class CacheManager implements Refreshable {

    /**
     * log
     */
    private static final Logger logger = LoggerFactory
            .getLogger("CERT-CACHE");

    /**
     * 证书缓存  <certCode Map<certType, content>> 证书byte[]内容base64后的String形式存储
     */
    private Map<String, Map<String, String>> certMapCache = new HashMap<String, Map<String, String>>();

    /**
     *
     */
    private Map<String, Set<String>> bizKeyCertCodeMap = new HashMap<String, Set<String>>();

    /**
     * instId, appId, operateType 对应的操作要素缓存， 包含的证书内容以bytes[]形式存储
     */
    private Map<String, CertificationFactor> certificationFactorMap = new HashMap<String, CertificationFactor>();

    /**
     * 外部系统实现获取证书服务
     */
    private Map<String, CertGetService> certGetServiceMap = new HashMap<String, CertGetService>();

    /**
     * 外部系统实现获取证书服务扩展点名称
     */
    private String processComponent;

    /**
     * 是否初始化完毕
     */
    private boolean isInitOk = true;

    /**
     * 是否启动强依赖
     */
    private boolean isInitDependency = false;

    /**
     * 初始化缓存
     */
    @Override
    public void init(Object... obj) {
        LogUtil.info(logger, CommonUtil.decorateBySquareBrackets(this.getClass().getName()) +
                "init Cache begin");
        List<AppConfig> appConfigs = (List<AppConfig>) obj[0];
        List<CertAlogMap> certAlogMaps = (List<CertAlogMap>) obj[1];
        List<CertConfig> certConfigs = (List<CertConfig>) obj[2];
        List<CertAlgoScript> certAlgoScripts = new LinkedList<CertAlgoScript>();

        if (obj.length == 4) {
            certAlgoScripts = (List<CertAlgoScript>) obj[3];
        }

        LogUtil.debug(logger, "appconfig:" + appConfigs);
        LogUtil.debug(logger, "certAlgoMap:" + certAlogMaps);
        LogUtil.debug(logger, "certConfig:" + certConfigs);
        LogUtil.debug(logger, "certAlgoScripts:" + certAlgoScripts);

        isInitOk = buildCache(appConfigs, certAlogMaps, certConfigs, true);

        LogUtil.info(logger, CommonUtil.decorateBySquareBrackets(this.getClass().getName()) +
                "init Cache result:" + isInitOk);

        CacheLogUtil.printLog("certMapCache", certMapCache);
        CacheLogUtil.printLog("bizKeyCertCodeMap", bizKeyCertCodeMap);
        CacheLogUtil.printLog("certificationFactorMap", certificationFactorMap);

    }

    /**
     * 刷新缓存
     */
    @Override
    public void refresh(Object... obj) {
        LogUtil.info(logger, CommonUtil.decorateBySquareBrackets(this.getClass().getName()) +
                "refresh Cache begin");

        List<AppConfig> appConfigs = (List<AppConfig>) obj[0];
        List<CertAlogMap> certAlogMaps = (List<CertAlogMap>) obj[1];
        List<CertConfig> certConfigs = (List<CertConfig>) obj[2];

        boolean tmpResult = buildCache(appConfigs, certAlogMaps, certConfigs, false);

        LogUtil.info(logger, CommonUtil.decorateBySquareBrackets(this.getClass().getName()) +
                "refresh Cache result:" + tmpResult);

        CacheLogUtil.printLog("certMapCache", certMapCache);
        CacheLogUtil.printLog("bizKeyCertCodeMap", bizKeyCertCodeMap);
        CacheLogUtil.printLog("certificationFactorMap", certificationFactorMap);
    }

    /**
     * 根据appId刷新缓存内容
     */
    @Override
    public void refreshById(String id, Object... obj) {

        LogUtil.info(logger, CommonUtil.decorateBySquareBrackets(this.getClass().getName()) +
                "refresh by Id:" + id);

        List<AppConfig> appConfigs = (List<AppConfig>) obj[0];
        List<CertAlogMap> certAlogMaps = (List<CertAlogMap>) obj[1];
        List<CertConfig> certConfigs = (List<CertConfig>) obj[2];

        boolean tmpResult = buildCache(appConfigs, certAlogMaps, certConfigs, false);

        LogUtil.info(logger, CommonUtil.decorateBySquareBrackets(this.getClass().getName()) +
                "refresh result:" + tmpResult);

        CacheLogUtil.printLog("certMapCache", certMapCache);
        CacheLogUtil.printLog("bizKeyCertCodeMap", bizKeyCertCodeMap);
        CacheLogUtil.printLog("certificationFactorMap", certificationFactorMap);
    }

    /**
     * 基于instId， appId，certType获取对应证书内容
     */
    public String getCertContent(String instId, String appId, String certType) {

        String certRtn = null;

        String key = CacheUtil.generateKey(instId, appId);

        Set<String> certCodes = bizKeyCertCodeMap.get(key);

        if (CollectionUtils.isEmpty(certCodes)) {
            return null;
        }

        for (String certCode : certCodes) {
            Map<String, String> certMap = certMapCache.get(certCode);
            if (certMap != null && certMap.containsKey(certType)) {
                return certMap.get(certType);
            }
        }

        return certRtn;
    }

    /**
     * 基于 instId，appId，operationType获取对应操作要素
     */
    public CertificationFactor getOperationFactor(String instId, String appId,
                                                String operationType) {
        String key = CacheUtil.generateKey(instId, appId, operationType);
        return certificationFactorMap.get(key);
    }

    /**
     * 全量构建缓存
     */
    private boolean buildCache(List<AppConfig> appConfigs, List<CertAlogMap> certAlogMaps,
                            List<CertConfig> certConfigs, boolean isAll) {
        boolean buildResult = true;

        try {
            Map<String, Map<String, String>> addCert = new HashMap<String, Map<String, String>>();

            for (CertConfig certConfig : certConfigs) {
                Map<String, String> currentCerts = addCert.get(certConfig.getCertCode());
                if (currentCerts == null) {
                    currentCerts = new HashMap<String, String>();
                }
                String content = getCertContent(certConfig);
                if (StringUtils.isNotBlank(content)) {
                    currentCerts.put(certConfig.getCertType(), content);
                }

                addCert.put(certConfig.getCertCode(), currentCerts);
            }

            if (isAll) {
                this.certMapCache = addCert;
                LogUtil.debug(logger, "[init] certCache:" + addCert);
            } else {

                LogUtil.debug(logger, "[update] added certCache:" + addCert);

                for (Map.Entry<String, Map<String, String>> stringMapEntry : addCert.entrySet()) {
                    if (certMapCache.containsKey(stringMapEntry.getKey())) {
                        certMapCache.get(stringMapEntry.getKey()).putAll(stringMapEntry.getValue());
                    } else {
                        certMapCache.put(stringMapEntry.getKey(), stringMapEntry.getValue());
                    }
                }

                LogUtil.debug(logger, "[update] certCache:" + this.certMapCache);
            }

            LogUtil.debug(logger, "------------------begin build certification factor------------------");
            Map<String, Set<String>> addAppConfig = Convertor.convertAppConf(appConfigs);
            LogUtil.debug(logger, "converted appConfig:" + addAppConfig);

            Map<String, Map<String, CertAlogMap>> addCertAlogMap = Convertor
                    .convertCertAlgoMap(certAlogMaps);
            LogUtil.debug(logger, "added certAlgoMap:" + addCertAlogMap);

            Map<String, Map<String, CertConfig>> certConfigMap = Convertor
                    .convertCertConf(certConfigs);
            LogUtil.debug(logger, "certConfigMap:" + certConfigMap);

            Map<String, CertificationFactor> addCertificationFactor = new HashMap<String, CertificationFactor>();

            for (Map.Entry<String, Set<String>> certCodeSetEntry : addAppConfig.entrySet()) {
                String curBizKey = certCodeSetEntry.getKey();

                for (String addCertCode : certCodeSetEntry.getValue()) {

                    Map<String, String> relatedCert = this.certMapCache.get(addCertCode);
                    Map<String, CertAlogMap> relatedAlgo = addCertAlogMap.get(addCertCode);

                    if (relatedAlgo == null) {
                        continue;
                    }

                    for (Map.Entry<String, CertAlogMap> certAlogMapEntry : relatedAlgo.entrySet()) {
                        String curOpType = certAlogMapEntry.getKey();

                        try {

                            CertificationFactor curFactor = CacheUtil.generateCertFactor(curBizKey,
                                    certAlogMapEntry.getValue(), relatedCert,
                                    certConfigMap.get(addCertCode));

                            if (curFactor == null) {
                                continue;
                            }

                            String factorKey = CacheUtil.generateKey(curBizKey, curOpType);

                            addCertificationFactor.put(factorKey, curFactor);
                        } catch (Exception e) {
                            LogUtil.error(logger, e, CommonUtil.decorateBySquareBrackets(certAlogMapEntry) +
                                    "generate CertificationFactor error!");
                            continue;
                        }
                    }
                }
            }

            if (isAll) {
                this.certificationFactorMap = addCertificationFactor;
                this.bizKeyCertCodeMap = addAppConfig;
                LogUtil.info(logger, "[init] certificationFactorMap:" + addCertificationFactor);

            } else {
                LogUtil.debug(logger, "[update] added certificationFactorMap:" +
                        addCertificationFactor);
                this.certificationFactorMap.putAll(addCertificationFactor);
                LogUtil.info(logger, "[update] updated certificationFactorMap:" +
                        this.certificationFactorMap);

                LogUtil.info(logger, "[update] added bizKeyCertCodeMap:" + addAppConfig);
                for (Map.Entry<String, Set<String>> setEntry : addAppConfig.entrySet()) {
                    if (this.bizKeyCertCodeMap.containsKey(setEntry.getKey())) {
                        this.bizKeyCertCodeMap.get(setEntry.getKey()).addAll(setEntry.getValue());
                    } else {
                        this.bizKeyCertCodeMap.put(setEntry.getKey(), setEntry.getValue());
                    }
                }
                LogUtil.info(logger, "[update] added bizKeyCertCodeMap:" + addAppConfig);
            }
        } catch (Exception e) {
            LogUtil.error(logger, e, CommonUtil.decorateBySquareBrackets(this.getClass().getName()) +
                    "build cache error!");
            buildResult = false;
        }

        return buildResult;
    }

    /**
     * 获取对应的证书内容，如果证书内容本地存储，则已经进行过base64处理，
     * 别名存储则调用外部接口获取证书，默认返回的格式是证书byte[] base64encode处理
     */
    public String getCertContent(CertConfig certConfig) {
        try {
            CertContentType certContentType = CertContentType
                    .getByName(certConfig.getCertContentType());

            switch (certContentType) {
                case CERT_ALIAS_NAME:
                    for (int i = 0; i < 5; ++i) {
                        try {
                            String certContent = certGetServiceMap.get(CertContentType.CERT_ALIAS_NAME.getCertContentType())
                                    .getCert(certConfig.getCertContent(), CertTypeEnum.getEnumByType(certConfig
                                            .getCertType()), certConfig.getCertCode());
                            LogUtil.debug(logger, "get CertContent from outter service(CERT_ALIAS_NAME):" +
                                    certConfig + " result:" + certContent);

                            if (StringUtils.isBlank(certContent)) {
                                LogUtil.warn(logger, "unable to get cert Content, certConfig=" +
                                        certConfig);
                            }
                            return certContent;
                        } catch (Exception e) {
                            LogUtil.error(logger, e, CommonUtil.decorateBySquareBrackets(this.getClass().getName()) + certConfig +
                                    ",get certContent by aliasName error, time:" + i);
                        }
                    }
                    throw new CertificationException(CertificationErrorCode.GET_CERT_CONTENT_ERROR,
                            certConfig.toString());
                case CERT_OFFICIAL_CONTENT:
                    return certConfig.getCertContent();
                case KMI_KEY_ALIAS_NAME:
                    String certContent = certGetServiceMap.get(CertContentType.KMI_KEY_ALIAS_NAME.getCertContentType())
                            .getCert(certConfig.getCertContent(), CertTypeEnum.getEnumByType(certConfig
                                    .getCertType()), certConfig.getCertCode());
                    LogUtil.debug(logger, "get CertContent from outter service(KMI_KEY_ALIAS_NAME):" +
                            certConfig + " result:" + certContent);
                    if (StringUtils.isBlank(certContent)) {
                        LogUtil.warn(logger, "unable to get cert Content, certConfig=" +
                                certConfig);
                    }
                    return certContent;
                default:
                    return certConfig.getCertContent();
            }

        } catch (CertificationException e) {
            LogUtil.error(logger, e, "get certContent CertificationException,certConfig=" + certConfig);
            if (this.isInitDependency) {
                throw e;
            }
        } catch (Exception e) {
            LogUtil.error(logger, e, "get certContent Exception,certConfig=" + certConfig);
            if (this.isInitDependency) {
                throw new CertificationException(CertificationErrorCode.GET_CERT_CONTENT_ERROR, certConfig.toString());
            }
        }
        return null;
    }

    //@Override
    //public void registerExtension(Extension extension) throws Exception {
    //    Object[] contribs = extension.getContributions();
    //    if (contribs.length == 0) {
    //        LogUtil.error(logger, "[register extension]nothing to register, extension=", extension);
    //        return;
    //    }
    //
    //    String extensionPoint = extension.getExtensionPoint();
    //
    //    LogUtil.info(logger, "[register extension]get certificate extension extensionPoint=", extensionPoint,
    //            " processComponent= ", processComponent);
    //
    //    if (StringUtils.equals(extensionPoint, processComponent)) {
    //        for (Object contrib : contribs) {
    //            try {
    //                CertGetServiceDescriptor desc = (CertGetServiceDescriptor) contrib;
    //                certGetServiceMap.put(desc.getName(), desc.getCertGetService());
    //            } catch (Exception ex) {
    //                LogUtil.error(logger, ex, "[register extension]exception:contrib=", contrib);
    //            }
    //
    //            LogUtil.info(logger, "[register extension]get certificate descriptor:descriptor = ", contrib);
    //        }
    //    } else {
    //        // 没有找到CertGetServiceDescriptor对应的扩展点处理方式，descriptor=
    //        LogUtil.error(logger, "cannot find CertGetServiceDescriptor extension point process  ，descriptor=",
    //                contribs);
    //    }
    //}

    /**
     * @see Refreshable#isInitOk()
     */
    public boolean isInitOk() {
        if (isInitDependency) {
            return isInitOk;
        } else {
            return true;
        }
    }

    /**
     * Setter method for property <tt>isInitOk</tt>.
     */
    public void setInitOk(boolean isInitOk) {
        this.isInitOk = isInitOk;
    }

    /**
     * Setter method for property <tt>certGetServiceMap</tt>.
     */
    public void setCertGetServiceMap(
            Map<String, CertGetService> certGetServiceMap) {
        this.certGetServiceMap = certGetServiceMap;
    }

    /**
     * Getter method for property <tt>processComponent<tt>.
     */
    public String getProcessComponent() {
        return processComponent;
    }

    /**
     * Setter method for property <tt>processComponent<tt>.
     */
    public void setProcessComponent(String processComponent) {
        this.processComponent = processComponent;
    }

    /**
     * Getter method for property <tt>isInitDependency<tt>.
     */
    public boolean isInitDependency() {
        return isInitDependency;
    }

    /**
     * Setter method for property <tt>isInitDependency<tt>.
     */
    public void setIsInitDependency(boolean isInitDependency) {
        this.isInitDependency = isInitDependency;
    }
}
