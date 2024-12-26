package com.github.loadup.components.gateway.certification.cache;

import com.github.loadup.components.gateway.certification.exception.CertificationErrorCode;
import com.github.loadup.components.gateway.certification.exception.CertificationException;
import com.github.loadup.components.gateway.certification.model.*;
import com.github.loadup.components.gateway.certification.util.CertUtil;
import com.github.loadup.components.gateway.certification.util.CommonUtil;
import com.github.loadup.components.gateway.core.model.CertAlogMap;
import com.github.loadup.components.gateway.core.model.CertConfig;
import com.github.loadup.components.gateway.facade.enums.CertTypeEnum;
import com.github.loadup.components.gateway.facade.util.LogUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 缓存相关工具
 */
public class CacheUtil {

    /**
     * 缓存日志
     */
    private static Logger logger = LoggerFactory.getLogger("CERT-CACHE");

    /**
     * 不同field构建key时的connector
     */
    private static String FIELD_CONNECTOR = "-";

    /**
     * 分号
     */
    private static String COMA = ";";

    /**
     * 多字段创建key, null 或空白字符自动剔除
     */
    public static String generateKey(String... strArray) {
        if (strArray.length == 0) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (String item : strArray) {
            if (StringUtils.isNotBlank(item)) {
                sb.append(item).append(FIELD_CONNECTOR);
            }
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    /**
     * split key with '-'. It is reverse method versus generateKey.
     */
    public static String[] splitKey(String key) {
        if (StringUtils.isBlank(key)) {
            return new String[0];
        }
        return StringUtils.split(key, FIELD_CONNECTOR);
    }

    /**
     * 生成安全组件操作要素
     */
    public static CertificationFactor generateCertFactor(String bizKey, CertAlogMap certAlogMap,
                                                         Map<String, String> certMap,
                                                         Map<String, CertConfig> certConfigMap) {

        CertificationFactor certificationFactor = new CertificationFactor();

        certificationFactor.setBizKey(bizKey);
        OperationType operationType = OperationType.getByName(certAlogMap.getOperateType());
        if (operationType == null) {
            throw new CertificationException(CertificationErrorCode.CONFIG_ERROR,
                    "unknown operation type:" + certAlogMap.getOperateType());
        }
        certificationFactor.setOperationType(operationType);
        certificationFactor.setAlgoString(certAlogMap.getAlgoName());
        certificationFactor.setAlgoParameter(CommonUtil.Str2Kv(certAlogMap.getAlgoProperties()));

        String[] certList = null;
        Map<String, Object> tmpCertMap = new HashMap<String, Object>();
        Map<String, String> tmpCertAliasName = new HashMap<String, String>();

        if (StringUtils.isNotBlank(certAlogMap.getCertTypes())) {
            certList = certAlogMap.getCertTypes().trim().split(COMA);
            for (String item : certList) {
                if (!CollectionUtils.isEmpty(certMap) && certMap.containsKey(item)) {
                    if (needGetByte(item)) {
                        tmpCertMap.put(item,
                                CertUtil.getRealKeyBytes(certMap.get(item), certConfigMap.get(item)));
                    } else {
                        tmpCertMap.put(item, certMap.get(item));
                    }
                    if (StringUtils.equals(item, CertTypeEnum.PUBLIC_CERT.getCertType())) {
                        tmpCertMap.put(CertTypeEnum.PUBLIC_KEY.getCertType(),
                                CertUtil.publicCert2KeyBytes((byte[]) tmpCertMap.get(item)));
                    }
                } else {
                    LogUtil.warn(logger, CommonUtil.decorateBySquareBrackets("certificationComponent") +
                            "No cert defined in certConfig:" + certAlogMap);
                    return null;
                }

                CertConfig curCertConfig = certConfigMap.get(item);
                if (curCertConfig != null
                        && StringUtils.equals(curCertConfig.getCertContentType(),
                        CertContentType.CERT_ALIAS_NAME.getCertContentType())) {
                    tmpCertAliasName.put(item, curCertConfig.getCertContent());
                }
            }
        }

        certificationFactor.setCertMap(tmpCertMap);
        certificationFactor.setCertAliasNameMap(tmpCertAliasName);

        return certificationFactor;
    }

    /**
     * 特殊逻辑，对于PUBLIC_KEY_ID， PRIVATE_KEY_PWD, PRIVATE_KEY_ID, CERT_PRIVATE_KEY_PWD， 直接返回String报文
     */
    public static boolean needGetByte(String certType) {

        if (StringUtils.isNotBlank(certType)
                && (StringUtils.equals(certType, CertTypeEnum.PUBLIC_KEY_ID.getCertType())
                || StringUtils.equals(certType, CertTypeEnum.PRIVATE_KEY_PWD.getCertType())
                || StringUtils.equals(certType, CertTypeEnum.PRIVATE_KEY_ID.getCertType()) || StringUtils
                .equals(certType, CertTypeEnum.CERT_PRIVATE_KEY_PWD.getCertType()))) {
            return false;
        }

        return true;
    }
}