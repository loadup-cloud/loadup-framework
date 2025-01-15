package com.github.loadup.components.gateway.certification.util;

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
