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
