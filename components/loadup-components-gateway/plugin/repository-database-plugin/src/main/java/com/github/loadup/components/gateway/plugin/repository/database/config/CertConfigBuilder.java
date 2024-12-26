package com.github.loadup.components.gateway.plugin.repository.database.config;

import com.github.loadup.components.gateway.certification.cache.CacheUtil;
import com.github.loadup.components.gateway.certification.exception.CertificationErrorCode;
import com.github.loadup.components.gateway.certification.exception.CertificationException;
import com.github.loadup.components.gateway.certification.impl.AbstractCertAlgorithmConfigBuilder;
import com.github.loadup.components.gateway.certification.model.CertContentType;
import com.github.loadup.components.gateway.certification.util.CertUtil;
import com.github.loadup.components.gateway.common.util.ExtensionPointLoader;
import com.github.loadup.components.gateway.core.common.enums.CertStatus;
import com.github.loadup.components.gateway.facade.enums.CertTypeEnum;
import com.github.loadup.components.gateway.facade.extpoint.CertificationAccessExt;
import com.github.loadup.components.gateway.facade.model.CertConfigDto;
import com.github.loadup.components.gateway.facade.util.LogUtil;
import com.github.loadup.components.gateway.plugin.repository.database.dal.dataobject.SecurityDO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * CertConfig builder
 */
@Component("databaseCertConfigBuilder")
public class CertConfigBuilder extends AbstractCertAlgorithmConfigBuilder<CertConfigDto> {

    /**
     * logger
     */
    private static final Logger logger = LoggerFactory
            .getLogger(CertConfigBuilder.class);

    public CertConfigDto buildDto(SecurityDO certConfigRepository) {

        CertConfigDto certConfigDto = new CertConfigDto();
        certConfigDto.setCertCode(CacheUtil.generateKey(
                certConfigRepository.getSecurityStrategyCode(), certConfigRepository.getOperateType(),
                certConfigRepository.getAlgoName(), certConfigRepository.getClientId()));
        //certType @CertTypeEnum
        String certType = certConfigRepository.getCertType();
        CertTypeEnum certTypeEnum = CertTypeEnum.getEnumByType(certType);
        if (null == certTypeEnum) {
            LogUtil.error(logger, "Cert Type: ", certType, " is abnormal");
            return null;
        } else {
            certConfigDto.setCertType(certType);
        }
        //securityStrategyKeyType should support ext point
        String keyType = certConfigRepository.getKeyType();
        CertContentType certContentTypeEnum = CertContentType.getByName(keyType);
        if (null == certContentTypeEnum) {
            LogUtil.info(logger, "Cert Content Type: ", keyType, " is abnormal");
        }
        //certConfig.setCertContentType(keyType);
        certConfigDto.setCertType(keyType);
        CertStatus certStatus = CertStatus.VALID;
        certStatus = CertStatus.getEnumByCode(certConfigRepository.getStatus());
        certConfigDto.setCertStatus(certStatus.getMessage());
        //cert_algorithm_properties
        //TODO date convertion
        //certConfig.setGmtValid(values[6]);
        //certConfig.setGmtInValid(values[6]);

        String keyContent = certConfigRepository.getCertContent();
        certConfigDto.setCertContent(getCertContent(keyType, certTypeEnum, keyContent));
        //certConfig.setCertSpecial(certConfigRepository.getCertProperties());
        certConfigDto.setProperties(certConfigRepository.getProperties());
        return certConfigDto;
    }

    /**
     * generic config build
     * <p>
     * format should follow the template which is defined in SECURITY_STRATEGY_CONF
     * 0 security_strategy_code, Mandatory
     * 1 security_strategy_operate_type, Mandatory
     * 2 security_strategy_algorithm, Mandatory
     * 3 securityStrategyKeyType, Mandatory
     * 4 certType, Mandatory
     * 5 security_strategy_key, Mandatory
     * 6 client_id,
     * 7 cert_properties,
     * 8 algorithm_properties
     */
    public CertConfigDto build(String... fileColumns) {
        String securityStrategyKeyType = fileColumns[3];
        String certType = fileColumns[4];
        String security_strategy_key = fileColumns[5];

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
        certConfigDto.setCertContent(
                getCertContent(securityStrategyKeyType, certTypeEnum, security_strategy_key));
        certConfigDto.setProperties(fileColumns[7]);

        return certConfigDto;
    }

    /**
     * 获取对应的证书内容，如果证书内容本地存储，则已经进行过base64处理，
     * 别名存储则调用外部接口获取证书，默认返回的格式是证书byte[] base64encode处理
     */
    //TODO refactor parameter
    public String getCertContent(String bizScene, CertTypeEnum certType, String certContentString) {
        try {
            if (StringUtils.equalsIgnoreCase(
                    CertContentType.CERT_OFFICIAL_CONTENT.getCertContentType(), bizScene)) {
                return certContentString;
            } else {
                CertificationAccessExt certificationAccessService = ExtensionPointLoader
                        .get(CertificationAccessExt.class, bizScene);

                if (null == certificationAccessService) {
                    LogUtil.error(logger, "no ext for bizScene=", bizScene, ";certType=",
                            certType.getCertType());
                    throw new CertificationException(CertificationErrorCode.GET_CERT_CONTENT_ERROR,
                            "no ext for bizScene=" + bizScene);
                }
                return certificationAccessService.getCertContent(certContentString, certType);
            }
        } catch (CertificationException e) {
            LogUtil.error(logger, "get certContent CertificationException,bizScene=", e, bizScene);
        } catch (Exception e) {
            LogUtil.error(logger, "get certContent Exception,bizScene=", e, bizScene);
        }
        return null;
    }
}
