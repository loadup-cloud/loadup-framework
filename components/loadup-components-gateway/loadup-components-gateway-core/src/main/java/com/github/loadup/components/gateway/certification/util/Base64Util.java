package com.github.loadup.components.gateway.certification.util;

import com.github.loadup.components.gateway.certification.model.CharsetEnum;

import java.io.UnsupportedEncodingException;

/**
 * base64 编码、解码类，算法工具类统一输入、输出 byte[] 类型
 */
public class Base64Util {

    /**
     * base64 编码
     */
    public static byte[] encode(byte[] data) {
        return org.apache.commons.codec.binary.Base64.encodeBase64(data);
    }

    /**
     * base64 编码
     */
    public static byte[] decode(byte[] data) {
        return org.apache.commons.codec.binary.Base64.decodeBase64(data);
    }

    /**
     * base64编码
     */
    public static String encode(String data, CharsetEnum inputEncode, CharsetEnum outputEncode)
            throws UnsupportedEncodingException {
        if (data == null) {
            return null;
        }

        byte[] input = data.getBytes(inputEncode.getCharSet());
        return new String(encode(input), outputEncode.getCharSet());
    }

    /**
     * base64解码
     */
    public static String decode(String data, CharsetEnum inputEncode, CharsetEnum outputEncode)
            throws UnsupportedEncodingException {
        if (data == null) {
            return null;
        }

        byte[] input = data.getBytes(inputEncode.getCharSet());
        return new String(decode(input), outputEncode.getCharSet());
    }

}