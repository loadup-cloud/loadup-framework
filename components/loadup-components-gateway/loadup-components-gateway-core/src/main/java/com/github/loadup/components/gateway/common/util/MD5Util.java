package com.github.loadup.components.gateway.common.util;

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

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MD5文件摘要 适用于大文件。
 */
public class MD5Util {

	/**
	 * 日志
	 */
	private static final Logger log = LoggerFactory.getLogger(MD5Util.class);

	/**
	 * 默认的密码字符串组合，apache校验下载的文件的正确性用的就是默认的这个组合
	 */
	protected static char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
			'a', 'b', 'c', 'd', 'e', 'f'};

	/**
	 * MD5
	 */
	public final static String MD5 = "MD5";

	/**
	 * 缓冲大小
	 */
	private final static int BUFFER_SIZE = 8192;

	/**
	 * 生成信息摘要
	 *
	 * @throws Exception
	 */
	protected static MessageDigest digestInit() throws Exception {
		return digestInit(MD5);
	}

	/**
	 * 生成信息摘要
	 *
	 * @throws Exception
	 */
	protected static MessageDigest digestInit(String digestType) throws Exception {
		MessageDigest messagedigest = null;
		try {
			messagedigest = MessageDigest.getInstance(digestType);
		} catch (NoSuchAlgorithmException e) {
			// 初始化失败，MessageDigest不支持MD5Util
			log.warn(MD5Util.class.getName() + "ini failed，MessageDigest don't support MD5Util ", e);
			throw new Exception(e);
		}
		return messagedigest;
	}

	/**
	 * 适用于上G大的文件进行MD5进行MD5摘要
	 */
	public static String getFileMD5String(File file, String digestType) throws Exception {
		FileInputStream in = null;
		BufferedInputStream bufferInputStream = null;
		MessageDigest messagedigest = digestInit(digestType);

		try {
			//fileContent
			in = new FileInputStream(file);
			bufferInputStream = new BufferedInputStream(in);
			byte[] byet = new byte[BUFFER_SIZE];
			while (true) {
				int readLenth = bufferInputStream.read(byet);
				if (readLenth == -1) {
					break;
				}
				messagedigest.update(byet, 0, readLenth);
			}

			return bufferToHex(messagedigest.digest());
		} finally {
			IOUtils.closeQuietly(bufferInputStream);
			IOUtils.closeQuietly(in);
		}
	}

	/**
	 * 对传入的字符串做MD5摘要
	 *
	 * @throws Exception
	 */
	public static String getMD5String(String s) throws Exception {
		return getMD5String(s.getBytes());
	}

	/**
	 * 对传入的byte流做MD5摘要
	 *
	 * @throws Exception
	 */
	public static String getMD5String(byte[] bytes) throws Exception {
		MessageDigest messagedigest = digestInit();
		messagedigest.update(bytes);
		return bufferToHex(messagedigest.digest());
	}

	/**
	 * byte流转换成十六进制
	 */
	private static String bufferToHex(byte bytes[]) {
		return bufferToHex(bytes, 0, bytes.length);
	}

	/**
	 * byte流转换成十六进制
	 */
	private static String bufferToHex(byte bytes[], int m, int n) {
		StringBuffer stringbuffer = new StringBuffer(2 * n);
		int k = m + n;
		for (int l = m; l < k; l++) {
			appendHexPair(bytes[l], stringbuffer);
		}
		return stringbuffer.toString();
	}

	/**
	 *
	 */
	private static void appendHexPair(byte bt, StringBuffer stringbuffer) {
		char c0 = hexDigits[(bt & 0xf0) >> 4];
		char c1 = hexDigits[bt & 0xf];
		stringbuffer.append(c0);
		stringbuffer.append(c1);
	}

}
