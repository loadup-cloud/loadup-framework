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
