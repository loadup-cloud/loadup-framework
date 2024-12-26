package com.github.loadup.components.gateway.certification.util;

import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.util.encoders.UrlBase64;

/**
 * 注意具体的需求，是需要有补位的urlBase64还是变长的(无补位)的UrlBase64
 * <p>
 * urlBase64算法类
 */
public class UrlBase64Util {

    /**
     * 带有补位的URLbase64实现，普通base64补位为"="，此urlbase64补位替换为"."
     */
    public static byte[] encodeWithPadding(byte[] data) {
        return UrlBase64.encode(data);
    }

    /**
     * 带有补位的urlBase64实现，普通base64补位为"="，此urlbase64补位替换为"."
     */
    public static byte[] decodeWithPadding(byte[] data) {
        return UrlBase64.decode(data);
    }

    /**
     * 不应长的base64编码，抛弃了填充符，UrlBase64编码，比较实用
     */
    public static byte[] encode(byte[] data) {
        return Base64.encodeBase64URLSafe(data);
    }

    /**
     * 不应长的base64解码，抛弃了填充符，UrlBase64编码，比较实用
     */
    public static byte[] decode(byte[] data) {
        return Base64.decodeBase64(data);
    }
}