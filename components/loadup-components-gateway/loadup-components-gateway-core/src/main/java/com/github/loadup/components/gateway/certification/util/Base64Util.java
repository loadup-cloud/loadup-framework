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
