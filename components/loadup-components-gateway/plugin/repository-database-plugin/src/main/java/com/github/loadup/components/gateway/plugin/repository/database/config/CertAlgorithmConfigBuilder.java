package com.github.loadup.components.gateway.plugin.repository.database.config;

import com.github.loadup.components.gateway.certification.cache.CacheUtil;
import com.github.loadup.components.gateway.certification.impl.AbstractCertAlgorithmConfigBuilder;
import com.github.loadup.components.gateway.certification.model.AlgorithmEnum;
import com.github.loadup.components.gateway.certification.util.CertUtil;
import com.github.loadup.components.gateway.facade.model.CertAlgorithmConfigDto;
import com.github.loadup.components.gateway.facade.util.LogUtil;
import com.github.loadup.components.gateway.plugin.repository.database.dal.dataobject.SecurityDO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * certification algorithm configuration builder
 */
@Component("certAlgorithmConfigBuilder")
public class CertAlgorithmConfigBuilder extends AbstractCertAlgorithmConfigBuilder<CertAlgorithmConfigDto> {

    /**
     * logger
     */
    private static Logger logger = LoggerFactory.getLogger(AbstractCertAlgorithmConfigBuilder.class);

    /**
     * build dto model by securityDO
     */
    public CertAlgorithmConfigDto build(SecurityDO securityDO) {
        CertAlgorithmConfigDto certAlgorithmConfigDto = new CertAlgorithmConfigDto();

        //security_strategy_operate_type.security_strategy_algorithm.index
        certAlgorithmConfigDto
                .setCertCode(CacheUtil.generateKey(securityDO.getSecurityStrategyCode(),
                        securityDO.getOperateType(), securityDO.getAlgoName(), securityDO.getClientId()));
        //security_strategy_operate_type
        certAlgorithmConfigDto.setOperateType(securityDO.getOperateType());
        //security_strategy_type
        certAlgorithmConfigDto.setAlgoType(securityDO.getAlgoName());
        //security_strategy_algorithm
        certAlgorithmConfigDto.setAlgoName(securityDO.getAlgoName());

        //cert_algorithm_properties
        certAlgorithmConfigDto.setAlgoProperties(securityDO.getAlgoProperties());

        //certAlgorithmConfig.setCertTypes(certConfigRepository.getCertType());
        certAlgorithmConfigDto.setCertType(securityDO.getCertType());
        return certAlgorithmConfigDto;
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
    public CertAlgorithmConfigDto build(String... fileColumns) {
        if (!validate(fileColumns)) {
            return null;
        }

        CertAlgorithmConfigDto certAlgorithmConfigDto = new CertAlgorithmConfigDto();
        //security_strategy_operate_type.security_strategy_algorithm.index
        certAlgorithmConfigDto.setCertCode(CertUtil.buildCertCode(fileColumns[0], fileColumns[1], fileColumns[2], fileColumns[6]));
        //security_strategy_operate_type
        certAlgorithmConfigDto.setOperateType(fileColumns[1]);
        //security_strategy_type
        certAlgorithmConfigDto.setAlgoType(buildAlgorithmType(fileColumns[2]));
        //security_strategy_algorithm
        certAlgorithmConfigDto.setAlgoName(fileColumns[2]);

        //cert_algorithm_properties
        certAlgorithmConfigDto.setAlgoProperties(fileColumns[8]);

        //certAlgorithmConfig.setCertTypes(fileColumns[4]);
        certAlgorithmConfigDto.setCertType(fileColumns[4]);
        return certAlgorithmConfigDto;
    }

    /**
     * build Alogorithm type by name {@link AlgorithmEnum}
     */
    private String buildAlgorithmType(String algorithmName) {
        AlgorithmEnum algorithm = AlgorithmEnum.getByName(algorithmName);
        if (null == algorithm) {
            LogUtil.error(logger, "Introduced a new algorithm=" + algorithmName);
        }
        return algorithmName;

    }

}
