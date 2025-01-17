package com.github.loadup.components.gateway.plugin;

/*-
 * #%L
 * certification-keystore-plugin
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

import com.alibaba.cola.extension.Extension;
import com.github.loadup.components.gateway.facade.enums.CertTypeEnum;
import com.github.loadup.components.gateway.facade.extpoint.CertificationAccessExt;
import com.github.loadup.components.gateway.facade.util.LogUtil;
import org.apache.commons.lang3.StringUtils;
import org.jasypt.util.text.BasicTextEncryptor;
import org.jasypt.util.text.StrongTextEncryptor;
import org.jasypt.util.text.TextEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Base64;

/**
 * Certification Access service extension to fetch the certitication content from keystore
 */
@Extension(bizId = "CERT_JKS_EXT")
@Component
public class KeystoreCertExt implements ApplicationContextAware,
        CertificationAccessExt {

    /**
     * logger
     */
    private static final Logger logger = LoggerFactory.getLogger(KeystoreCertExt.class);

    private ApplicationContext applicationContext;

    @Value("${jasypt.encryptor.password}")
    private String encryptSalt;

    /**
     * PBEWithMD5AndDES or PBEWithMD5AndTripleDES
     */
    @Value("${jasypt.encryptor.algorithm:PBEWithMD5AndDES}")
    private String encryptAlgorithm;

    @Value("${keystore.file.path}")
    private String keystorePath;

    @Value("${keystore.user.password}")
    private String keystorePassword;

    /**
     * get the certification content extension
     * TODO
     */
    @Override
    public String getCertContent(String certAliasName, CertTypeEnum certType) throws IOException {
        LogUtil.info(logger, "KeystoreCertExt is executed");
        // The actual environment variable need to be set in configurations.
        //omitted in test
        //this test case is to ensure different extension point can be loaded.

        //1. get keystore file
        URL uri = this.getClass().getResource("/");

        File keyFile = new File(uri.getPath().concat(keystorePath));
        if (!keyFile.exists()) {
            return null;
        }
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(keyFile);
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());

            //2. get keystore password
            TextEncryptor textEncryptor = getEncryptor(encryptAlgorithm, encryptSalt);

            String pwd = textEncryptor.decrypt(keystorePassword);

            keyStore.load(fis, pwd.toCharArray());

            Key key = keyStore.getKey(certAliasName, pwd.toCharArray());

            if (null == key) {
                LogUtil.error(logger, "Can't fetch the key for certAliasName=" + certAliasName);
                return "";
            }

            if (key instanceof PrivateKey) {
                Certificate cert = keyStore.getCertificate(certAliasName);
                PublicKey publicKey = cert.getPublicKey();

                switch (certType) {
                    case PRIVATE_KEY:
                        return Base64.getEncoder().encodeToString(key.getEncoded());
                    case PUBLIC_KEY:
                        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
                    default:
                        return "";
                }
            }
        } catch (FileNotFoundException e) {
            LogUtil.error(logger, e, "invalid keystore path=", keystorePath);
        } catch (KeyStoreException e) {
            LogUtil.error(logger, e, "failed reading keystore path=", keystorePath);
        } catch (NoSuchAlgorithmException e) {
            LogUtil.error(logger, e, "couldn't support algorithm=", encryptAlgorithm);
        } catch (CertificateException e) {
            LogUtil.error(logger, e, "failed featching certification certAliasName=", certAliasName);
        } catch (UnrecoverableKeyException e) {
            LogUtil.error(logger, e, "failed featching key keyAliasName=", certAliasName);
        } catch (IOException e) {
            LogUtil.error(logger, e, "failed reading keystore file path=", keystorePath);
        } finally {
            if (null != fis) {
                fis.close();
            }
        }

        return "";
    }

    /**
     * choose the encryptor by algorithm
     */
    private TextEncryptor getEncryptor(String encryptAlgorithm, String encryptSalt) {
        if (StringUtils.equals(encryptAlgorithm, "PBEWithMD5AndTripleDES")) {
            StrongTextEncryptor textEncryptor = new StrongTextEncryptor();
            textEncryptor.setPassword(encryptSalt);
            return textEncryptor;
        } else {
            BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
            textEncryptor.setPassword(encryptSalt);
            return textEncryptor;
        }
    }

    /**
     * Setter method for property <tt>applicationContext</tt>.
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * Setter method for property <tt>encryptAlgorithm</tt>.
     */
    public void setEncryptAlgorithm(String encryptAlgorithm) {
        this.encryptAlgorithm = encryptAlgorithm;
    }

    /**
     * Setter method for property <tt>keystorePassword</tt>.
     */
    public void setKeystorePassword(String keystorePassword) {
        this.keystorePassword = keystorePassword;
    }

    /**
     * Setter method for property <tt>keystorePath</tt>.
     */
    public void setKeystorePath(String keystorePath) {
        this.keystorePath = keystorePath;
    }
}
