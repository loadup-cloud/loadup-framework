package com.github.loadup.components.gateway.certification.util.commonAlgo;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.MessageDigest;
import java.security.Security;

/**
 * MD系类摘要算法, 支持算法：MD2，MD4，MD5
 */
public class MDUtil {

    static {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    /**
     *
     */
    public static byte[] digest(byte[] data, String algorithm) throws Exception {

        MessageDigest md = MessageDigest.getInstance(algorithm);
        return md.digest(data);
    }

}