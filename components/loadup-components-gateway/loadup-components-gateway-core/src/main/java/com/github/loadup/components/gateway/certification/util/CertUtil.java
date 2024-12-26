package com.github.loadup.components.gateway.certification.util;

import com.github.loadup.components.gateway.certification.cache.CacheUtil;
import com.github.loadup.components.gateway.certification.model.CommonParameter;
import com.github.loadup.components.gateway.core.model.CertConfig;
import com.github.loadup.components.gateway.facade.util.LogUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.cert.*;
import java.util.Map;

/**
 * 证书相关工具类
 */
public class CertUtil {

    /**
     * 缓存日志
     */
    private static Logger logger = LoggerFactory.getLogger("CERT-CACHE");

    /**
     * 将X509Certificate格式的证书转换为key的形式
     */
    public static byte[] publicCert2KeyBytes(byte[] certBytes) {
        try {
            InputStream certIn = new ByteArrayInputStream(certBytes);
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509Certificate certificate = (X509Certificate) cf.generateCertificate(certIn);
            return certificate.getPublicKey().getEncoded();
        } catch (CertificateException e) {
            LogUtil.error(logger, e, "publicCert2KeyBytes error: ");
        }
        return null;
    }

    /**
     * 根据certSpecial字段恢复出对应的key内容
     */
    public static byte[] getRealKeyBytes(String keyContent, CertConfig certConfig) {

        Map<String, String> certSpecial = CommonUtil.Str2Kv(certConfig.getCertSpecial());
        String base64Times = certSpecial.get(CommonParameter.CERT_BASE64_FORM);
        if (StringUtils.equals(base64Times, "NONE")) {
            return keyContent.getBytes();
        } else if (StringUtils.equals(base64Times, "TWICE")) {
            return Base64Util.decode(Base64Util.decode(keyContent.getBytes()));
        } else {
            return Base64Util.decode(keyContent.getBytes());
        }
    }

    /**
     * build cert code
     */
    public static String buildCertCode(String securityStrategyCode, String operateType,
                                       String algorithm, String clientId) {
        return CacheUtil.generateKey(securityStrategyCode, operateType, algorithm, clientId);
    }
}