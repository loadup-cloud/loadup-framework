/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.gateway.plugin.repository.file.config;

/*-
 * #%L
 * repository-file-plugin
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

import com.alibaba.cola.extension.BizScenario;
import com.alibaba.cola.extension.ExtensionException;
import com.alibaba.cola.extension.ExtensionExecutor;
import com.github.loadup.components.gateway.certification.cache.CacheUtil;
import com.github.loadup.components.gateway.certification.exception.CertificationErrorCode;
import com.github.loadup.components.gateway.certification.exception.CertificationException;
import com.github.loadup.components.gateway.certification.impl.AbstractCertAlgorithmConfigBuilder;
import com.github.loadup.components.gateway.certification.model.CertContentType;
import com.github.loadup.components.gateway.certification.util.CertUtil;
import com.github.loadup.components.gateway.facade.enums.CertTypeEnum;
import com.github.loadup.components.gateway.facade.extpoint.CertificationAccessExt;
import com.github.loadup.components.gateway.facade.model.CertConfigDto;
import com.github.loadup.components.gateway.facade.util.LogUtil;
import com.github.loadup.components.gateway.plugin.repository.file.model.CertConfigRepository;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * CertConfig builder
 */
@Component("gatewayFileCertConfigBuilder")
public class CertConfigBuilder extends AbstractCertAlgorithmConfigBuilder<CertConfigDto> {

    /**
     * logger
     */
    private static final Logger logger = LoggerFactory.getLogger(CertConfigBuilder.class);

    @Resource
    private ExtensionExecutor extensionExecutor;

    /**
     * build dto
     */
    public CertConfigDto buildDto(CertConfigRepository certConfigRepository) {
        CertConfigDto certConfigDto = new CertConfigDto();
        certConfigDto.setCertCode(CacheUtil.generateKey(
                certConfigRepository.getSecurityStrategyCode(), certConfigRepository.getOperateType(),
                certConfigRepository.getAlgorithm(), certConfigRepository.getClientId()));
        String certType = certConfigRepository.getCertType();
        CertTypeEnum certTypeEnum = CertTypeEnum.getEnumByType(certType);
        if (null == certTypeEnum) {
            LogUtil.error(logger, "Cert Type: ", certType, " is abnormal");
            return null;
        } else {
            certConfigDto.setCertType(certType);
        }
        String keyType = certConfigRepository.getKeyType();
        CertContentType certContentTypeEnum = CertContentType.getByName(keyType);
        if (null == certContentTypeEnum) {
            LogUtil.info(logger, "Cert Content Type: ", keyType, " is abnormal");
        }
        certConfigDto.setCertType(keyType);
        certConfigDto.setCertStatus("Y");
        String keyContent = certConfigRepository.getKeyContent();
        certConfigDto.setCertContent(getCertContent(keyType, certTypeEnum, keyContent));
        certConfigDto.setCertContent(getCertContent(keyType, certTypeEnum, keyContent));
        certConfigDto.setProperties(certConfigRepository.getCertProperties());
        return certConfigDto;
    }

    /**
     * generic config build
     * <p>
     * format should follow the template which is defined in SECURITY_STRATEGY_CONF
     * 0 security_strategy_code, Mandatory
     * 1 security_strategy_operate_type, Mandatory
     * 2 security_strategy_algorithm, Mandatory
     * 3 security_strategy_key_type, Mandatory
     * 4 cert_type, Mandatory
     * 5 security_strategy_key, Mandatory
     * 6 client_id,
     * 7 cert_properties,
     * 8 algorithm_properties
     */
    public CertConfigDto build(String... fileColumns) {
        String securityStrategyKeyType = fileColumns[3];
        String certType = fileColumns[4];
        String securityStrategyKey = fileColumns[5];

        if (!validate(fileColumns)) {
            return null;
        }

        CertConfigDto certConfigDto = new CertConfigDto();
        certConfigDto.setCertCode(
                CertUtil.buildCertCode(fileColumns[0], fileColumns[1], fileColumns[2], fileColumns[6]));
        CertTypeEnum certTypeEnum = CertTypeEnum.getEnumByType(certType);
        if (null == certTypeEnum) {
            LogUtil.error(logger, "Cert Type: ", certType, " is abnormal");
            return null;
        } else {
            certConfigDto.setCertType(certType);
        }
        CertContentType certContentTypeEnum = CertContentType.getByName(securityStrategyKeyType);
        if (null == certContentTypeEnum) {
            LogUtil.info(logger, "Cert Content Type: ", securityStrategyKeyType, " is abnormal");
        }
        certConfigDto.setCertType(securityStrategyKeyType);
        certConfigDto.setCertStatus("Y");
        certConfigDto.setCertContent(getCertContent(securityStrategyKeyType, certTypeEnum, securityStrategyKey));
        certConfigDto.setProperties(fileColumns[7]);

        return certConfigDto;
    }

    /**
     * 获取对应的证书内容，如果证书内容本地存储，则已经进行过base64处理，
     * 别名存储则调用外部接口获取证书，默认返回的格式是证书byte[] base64encode处理
     */
    // TODO refactor parameter
    public String getCertContent(String bizScene, CertTypeEnum certType, String certContentString) {
        try {
            if (StringUtils.equalsIgnoreCase(CertContentType.CERT_OFFICIAL_CONTENT.getCertContentType(), bizScene)) {
                return certContentString;
            } else {
                return extensionExecutor.execute(CertificationAccessExt.class, BizScenario.valueOf(bizScene), ext -> {
                    try {
                        return ext.getCertContent(certContentString, certType);
                    } catch (Exception e) {
                        LogUtil.error(logger, e, "get certContent Exception,bizScene=", bizScene);
                    }
                    return null;
                });
            }
        } catch (ExtensionException e) {
            throw new CertificationException(
                    CertificationErrorCode.GET_CERT_CONTENT_ERROR, "no ext for bizScene=" + bizScene);
        } catch (CertificationException e) {
            LogUtil.error(logger, e, "get certContent CertificationException,bizScene=", bizScene);
        } catch (Exception e) {
            LogUtil.error(logger, e, "get certContent Exception,bizScene=", bizScene);
        }
        return null;
    }
}
