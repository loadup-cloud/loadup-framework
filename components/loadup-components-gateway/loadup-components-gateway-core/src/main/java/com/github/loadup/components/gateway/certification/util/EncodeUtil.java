package com.github.loadup.components.gateway.certification.util;

import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;

import java.io.UnsupportedEncodingException;
import java.security.Security;

/**
 * 编码转换工具类
 */
public class EncodeUtil {

    static {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    /**
     * byte数组转换为十六进制的String形式， 采用默认编码
     */
    public static String bytes2Hex(byte[] data) {
        return new String(Hex.encode(data));
    }

    /**
     * 带有编码参数的byte数组转换为十六进制String函数
     *
     * @throws UnsupportedEncodingException
     */
    public static String bytes2Hex(byte[] data, String encode) throws UnsupportedEncodingException {
        return new String(Hex.encode(data), encode);
    }

    /**
     * 将hexString转换为byte数组
     *
     * @throws UnsupportedEncodingException
     */
    public static byte[] hex2Bytes(String data, String encode) throws UnsupportedEncodingException {
        if (StringUtils.isBlank(encode)) {
            return Hex.decode(data);
        }
        return Hex.decode(data.getBytes(encode));
    }

}