package com.github.loadup.components.gateway.facade.enums;

/*-
 * #%L
 * loadup-components-gateway-facade
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

import org.apache.commons.lang3.StringUtils;

/**
 * <p>Certification Type Enum</p>
 */
public enum CertTypeEnum {

    /**
     * public key
     */
    PUBLIC_KEY("PUBLIC_KEY", "publicKey"),

    /**
     * Id of public key，PGP only  note: it should not be cached in certificationFactor with byte[]
     */
    PUBLIC_KEY_ID("PUBLIC_KEY_ID", "publicKeyId"),

    /**
     * private key
     */
    PRIVATE_KEY("PRIVATE_KEY", "privateKey"),

    /**
     * password of private key  note: it should not be cached in certificationFactor with byte[]
     */
    PRIVATE_KEY_PWD("PRIVATE_KEY_PWD", "privateKeyPwd"),

    /**
     * Id of private key  note: it should not be cached in certificationFactor with byte[]
     */
    PRIVATE_KEY_ID("PRIVATE_KEY_ID", "privateKeyId"),

    /**
     * digest key which is required for HMAC algorithm
     */
    DIGEST_KEY("DIGEST_KEY", "digestKey"),

    /**
     * digest salt
     */
    DIGEST_SALT("DIGEST_SALT", "digestSalt"),

    /**
     * symmetric key
     */
    SYMMETRIC_KEY("SYMMETRIC_KEY", "symmetricKey"),

    /**
     * public cert which is used for storage
     */
    PUBLIC_CERT("PUBLIC_CERT", "publicCert"),

    /**
     * public and private cert
     */
    PUBLIC_PRIVATE_CERT("PUBLIC_PRIVATE_CERT", "publicPrivateCert"),

    /**
     * password of private key for cert  note: it should not be cached in certificationFactor with byte[]
     */
    CERT_PRIVATE_KEY_PWD("CERT_PRIVATE_KEY_PWD", "certPrivateKeyPwd"),

    /**
     * SSL connection cert
     */
    SSL_CONN_CERT("SSL_CONN_CERT", "SSLConnectionCert"),

    /**
     * SSL private key
     */
    SSL_PRIVATE_KEY("SSL_PRIVATE_KEY", "SSLPrivateKey"),

    /**
     * SSL private key password
     */
    SSL_PRIVATE_KEY_PWD("SSL_PRIVATE_KEY_PWD", "SSLPrivateKeyPwd"),

    /**
     * SSL root cert
     */
    SSL_ROOT_CERT("SSL_ROOT_CERT", "SSLRootCert"),

    /**
     * SSL root cert password
     */
    SSL_ROOT_CERT_PWD("SSL_ROOT_CERT_PWD", "SSLRootCertPwd"),

    /**
     * SSL root cert key password
     */
    SSL_ROOT_CERT_KEY_PWD("SSL_ROOT_CERT_KEY_PWD", "SSLRootCertKeyPwd"),

    /**
     * SSL_CLIENT_SUBJECT_CN
     */
    SSL_CLIENT_SUBJECT_CN("SSL_CLIENT_SUBJECT_CN", "Mutual TLS Client Subject Common Name");

    /**
     * cert type
     */
    private final String certType;

    /**
     * cert name
     */
    private final String certName;

    /**
     *
     */
    private CertTypeEnum(String certType, String certName) {
        this.certType = certType;
        this.certName = certName;
    }

    /**
     * <pre>
     * get CertTypeEnum by certType
     * </pre>
     */
    public static CertTypeEnum getEnumByType(String certType) {
        CertTypeEnum[] enums = CertTypeEnum.values();
        for (CertTypeEnum codeEnum : enums) {
            if (StringUtils.equals(codeEnum.getCertType(), certType)) {
                return codeEnum;
            }
        }

        return null;
    }

    /**
     * <pre>
     * get CertTypeEnum by certName
     * </pre>
     */
    public static CertTypeEnum getEnumByName(String certName) {
        CertTypeEnum[] enums = CertTypeEnum.values();
        for (CertTypeEnum codeEnum : enums) {
            if (StringUtils.equals(codeEnum.getCertName(), certName)) {
                return codeEnum;
            }
        }

        return null;
    }

    /**
     * <pre>
     * get certName by certType
     * </pre>
     */
    public static String getNameByType(String certType) {
        return getEnumByType(certType).getCertName();
    }

    /**
     *
     */
    public String getCertType() {
        return certType;
    }

    /**
     *
     */
    public String getCertName() {
        return certName;
    }
}
