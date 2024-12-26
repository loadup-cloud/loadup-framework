package com.github.loadup.components.gateway.certification.util.commonAlgo;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.MessageDigest;
import java.security.Security;

/**
 * SHA 系列摘要算法，支持算法, SHA-1,SHA-224,SHA-256,SHA-384,SHA-512
 */
public class SHAUtil {

    static {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    /**
     * 摘要算法
     */
    public static byte[] digest(byte[] data, String algorithm) throws Exception {

        MessageDigest md = MessageDigest.getInstance(algorithm);
        return md.digest(data);
    }

}