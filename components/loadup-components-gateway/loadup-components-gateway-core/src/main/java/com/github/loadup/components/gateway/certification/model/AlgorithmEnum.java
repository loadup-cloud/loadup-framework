package com.github.loadup.components.gateway.certification.model;

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
import org.apache.commons.lang3.StringUtils;

/**
 * 算法枚举类
 */
public enum AlgorithmEnum {

    /**
     * MD2摘要算法
     */
    MD2("MD2", "DIGEST", "SINGLE", "MD2摘要"),

    /**
     * MD4摘要
     */
    MD4("MD4", "DIGEST", "SINGLE", "MD4摘要"),

    /**
     * MD5摘要
     */
    MD5("MD5", "DIGEST", "SINGLE", "MD5摘要"),

    /**
     * SHA-1摘要算法
     */
    SHA_1("SHA", "DIGEST", "SINGLE", "SHA1摘要算法"),

    /**
     * SHA-224摘要算法
     */
    SHA_224("SHA-224", "DIGEST", "SINGLE", "SHA-224摘要算法"),

    /**
     * SHA-256摘要算法
     */
    SHA_256("SHA-256", "DIGEST", "SINGLE", "SHA-256摘要算法"),

    /**
     * SHA-384摘要算法
     */
    SHA_384("SHA-384", "DIGEST", "SINGLE", "SHA-384摘要算法"),

    /**
     * SHA-512摘要算法
     */
    SHA_512("SHA-512", "DIGEST", "SINGLE", "SHA-512摘要算法"),

    /**
     * HmacMD2 摘要算法
     */
    HmacMD2("HmacMD2", "DIGEST", "SINGLE", "HmacMD2 摘要算法:128"),

    /**
     * HmacMD4 摘要算法
     */
    HmacMD4("HmacMD4", "DIGEST", "SINGLE", "HmacMD4 摘要算法:128"),

    /**
     * HmacMD5 摘要算法
     */
    HmacMD5("HmacMD5", "DIGEST", "SINGLE", "HmacMD5 摘要算法:128"),

    /**
     * HmacSHA1 摘要算法
     */
    HmacSHA1("HmacSHA1", "DIGEST", "SINGLE", "HmacSHA1 摘要算法:160"),

    /**
     * HmacSHA224 摘要算法
     */
    HmacSHA224("HmacSHA224", "DIGEST", "SINGLE", "HmacSHA224 摘要算法:224"),

    /**
     * HmacSHA256 摘要算法
     */
    HmacSHA256("HmacSHA256", "DIGEST", "SINGLE", "HmacSHA256 摘要算法:256"),

    /**
     * HmacSHA384 摘要算法
     */
    HmacSHA384("HmacSHA384", "DIGEST", "SINGLE", "HmacSHA384 摘要算法:384"),

    /**
     * HmacSHA512 摘要算法
     */
    HmacSHA512("HmacSHA512", "DIGEST", "SINGLE", "HmacSHA512 摘要算法:512"),

    /**
     * DES算法，包含众多模式，此处不具体罗列
     */
    DES("DES", "SYMMETRIC_ENCRYPTION", "MULTIPLE", "DES对称加解密算法"),

    /**
     * DESede算法(3DES) 包含众多模式，此处不具体罗列
     */
    DESede("DESede", "SYMMETRIC_ENCRYPTION", "MULTIPLE", "DESede对称加解密算法"),

    /**
     * AES算法，包含众多模式，此处不具体罗列
     */
    AES("AES", "SYMMETRIC_ENCRYPTION", "MULTIPLE", "AES对称加解密算法"),

    /**
     * AES算法，包含众多模式，此处不具体罗列
     */
    IDEA("IDEA", "SYMMETRIC_ENCRYPTION", "MULTIPLE", "IDEA对称加解密算法"),

    /**
     * RSA算法，包含众多模式，此处不具体罗列
     */
    RSA("RSA", "ASYMMETRIC_ENCRYPTION", "MULTIPLE", "RSA对称加解密算法"),

    /**
     * DSA算法，包含众多模式，此处不具体罗列
     */
    DSA("DSA", "DIGITAL_SIGNATURE", "SINGLE", "DSA签名算法"),

    /**
     * NONEwithRSA签名算法
     */
    NONEwithRSA("NONEwithRSA", "DIGITAL_SIGNATURE", "SINGLE", "NONEwithRSA签名算法"),

    /**
     * MD2withRSA签名算法
     */
    MD2withRSA("MD2withRSA", "DIGITAL_SIGNATURE", "SINGLE", "MD2withRSA签名算法"),

    /**
     * MD5withRSA签名算法
     */
    MD5withRSA("MD5withRSA", "DIGITAL_SIGNATURE", "SINGLE", "MD5withRSA签名算法"),

    /**
     * SHA1withRSA签名算法
     */
    SHA1withRSA("SHA1withRSA", "DIGITAL_SIGNATURE", "SINGLE", "SHA1withRSA签名算法"),

    /**
     * SHA224withRSA签名算法
     */
    SHA224withRSA("SHA224withRSA", "DIGITAL_SIGNATURE", "SINGLE", "SHA224withRSA签名算法"),

    /**
     * SHA256withRSA签名算法
     */
    SHA256withRSA("SHA256withRSA", "DIGITAL_SIGNATURE", "SINGLE", "SHA256withRSA签名算法"),

    /**
     * SHA384withRSA签名算法
     */
    SHA384withRSA("SHA384withRSA", "DIGITAL_SIGNATURE", "SINGLE", "SHA384withRSA签名算法"),

    /**
     * SHA512withRSA签名算法
     */
    SHA512withRSA("SHA512withRSA", "DIGITAL_SIGNATURE", "SINGLE", "SHA512withRSA签名算法"),

    /**
     * RIPEMD128withRSA签名算法
     */
    RIPEMD128withRSA("RIPEMD128withRSA", "DIGITAL_SIGNATURE", "SINGLE", "RIPEMD128withRSA签名算法"),

    /**
     * RIPEMD160withRSA签名算法
     */
    RIPEMD160withRSA("RIPEMD160withRSA", "DIGITAL_SIGNATURE", "SINGLE", "RIPEMD160withRSA签名算法"),

    /**
     * SHA1withDSA签名算法
     */
    SHA1withDSA("SHA1withDSA", "DIGITAL_SIGNATURE", "SINGLE", "SHA1withDSA签名算法"),

    /**
     * SHA224withDSA签名算法
     */
    SHA224withDSA("SHA224withDSA", "DIGITAL_SIGNATURE", "SINGLE", "SHA224withDSA签名算法"),

    /**
     * SHA256withDSA签名算法
     */
    SHA256withDSA("SHA256withDSA", "DIGITAL_SIGNATURE", "SINGLE", "SHA256withDSA签名算法"),

    /**
     * SHA384withDSA签名算法
     */
    SHA384withDSA("SHA384withDSA", "DIGITAL_SIGNATURE", "SINGLE", "SHA384withDSA签名算法"),

    /**
     * SHA512withDSA签名算法
     */
    SHA512withDSA("SHA512withDSA", "DIGITAL_SIGNATURE", "SINGLE", "SHA512withDSA签名算法"),

    /**
     * RSA xml签名算法
     */
    ALGO_ID_SIGNATURE_RSA("ALGO_ID_SIGNATURE_RSA", "XML_SIGNATURE", "SINGLE", "RSA xml签名算法"),

    /**
     * RSA_SHA1 xml签名算法
     */
    ALGO_ID_SIGNATURE_RSA_SHA1("ALGO_ID_SIGNATURE_RSA_SHA1", "XML_SIGNATURE", "SINGLE", "RSA_SHA1 xml签名算法"),

    /**
     * RSA_SHA256 xml签名算法
     */
    ALGO_ID_SIGNATURE_RSA_SHA256("ALGO_ID_SIGNATURE_RSA_SHA256", "XML_SIGNATURE", "SINGLE", "RSA_SHA256 xml签名算法"),

    /**
     * RSA_SHA384 xml签名算法
     */
    ALGO_ID_SIGNATURE_RSA_SHA384("ALGO_ID_SIGNATURE_RSA_SHA384", "XML_SIGNATURE", "SINGLE", "RSA_SHA384 xml签名算法"),

    /**
     * RSA_SHA512 xml签名算法
     */
    ALGO_ID_SIGNATURE_RSA_SHA512("ALGO_ID_SIGNATURE_RSA_SHA512", "XML_SIGNATURE", "SINGLE", "RSA_SHA512 xml签名算法"),

    /**
     * ----------------------KATONG 特殊算法-----------------------------
     */
    ALGO_KT("ALGO_KT", "KATONG_ALGO", "SINGLE", "卡通类标准算法"),

    ALGO_KJ("ALGO_KJ", "KATONG_ALGO", "SINGLE", "快捷类标准算法"),

    /**
     * ----------------------GROOVY 类算法-----------------------------
     */
    GROOVY("Groovy", "GROOVY_ALGO", "MULTIPLE", "GROOVY实现的自定义算法"),

    /**
     * 国密算法
     */
    ALGO_SM2("SM2", "SELF_DEF_ALGO", "MULTIPLE", "国密算法应用层实现"),

    /**
     * ---------------------- PKCS7签名-----------------------------
     */
    PKCS7_SHA1("PKCS7_SHA1", "PKCS7_SHA1", "SINGLE", "PKCS7_SHA1签名"),

    /**
     * ---------------------- saas层实现算法-----------------------------
     */
    SELF_DEF_ALGO("SELF_DEF_ALGO", "SELF_DEF_ALGO", "MULTIPLE", "SAAS实现的自定义算法"),
    ;

    /**
     * 算法名
     */
    private String name;

    /**
     * 算法归类
     */
    private String type;

    /**
     * 算法属性：包含单个 or 多个
     */
    private String subAlgo;

    /**
     * 算法描述
     */
    private String desc;

    /**
     * 构造函数
     */
    AlgorithmEnum(String name, String type, String subAlgo, String desc) {
        this.name = name;
        this.type = type;
        this.subAlgo = subAlgo;
        this.desc = desc;
    }

    /**
     * 根据算法名称获取具体的算法定义
     * 对于输入：DES/ECB/NoPadding类似的算法，只返回开始的DES算法就可
     */
    public static AlgorithmEnum getByName(String algoName) {

        if (algoName == null) {
            throw new CertificationException(CertificationErrorCode.CONFIG_ERROR, "no algorithm name defined");
        }

        for (AlgorithmEnum algorithm : AlgorithmEnum.values()) {
            if (StringUtils.equals(algorithm.getName(), algoName)) {
                return algorithm;
            }
        }

        for (AlgorithmEnum algorithm : AlgorithmEnum.values()) {
            if ("MULTIPLE".equals(algorithm.getSubAlgo()) && algoName.startsWith(algorithm.getName())) {
                return algorithm;
            }
        }

        return AlgorithmEnum.SELF_DEF_ALGO;
    }

    /**
     * Getter method for property <tt>desc<tt>.
     */
    public String getDesc() {
        return desc;
    }

    /**
     * Setter method for property <tt>desc<tt>.
     */
    public void setDesc(String desc) {
        this.desc = desc;
    }

    /**
     * Getter method for property <tt>name<tt>.
     */
    public String getName() {
        return name;
    }

    /**
     * Setter method for property <tt>name<tt>.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter method for property <tt>type<tt>.
     */
    public String getType() {
        return type;
    }

    /**
     * Setter method for property <tt>type<tt>.
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Getter method for property <tt>subAlgo<tt>.
     */
    public String getSubAlgo() {
        return subAlgo;
    }

    /**
     * Setter method for property <tt>subAlgo<tt>.
     */
    public void setSubAlgo(String subAlgo) {
        this.subAlgo = subAlgo;
    }
}
