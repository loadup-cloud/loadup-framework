package com.github.loadup.components.gateway.certification.model;

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

/**
 * 通用配置参数列举
 */
public class CommonParameter {

	/**
	 * 算法
	 */
	public static String ALGO = "ALGO";

	/**
	 * 输入编码 支持格式可见：CharsetEnum
	 */
	public static String INPUT_ENCODE = "INPUT_ENCODE";

	/**
	 * 输入格式,目前支持可查看：FormatType
	 */
	public static String INPUT_FORMAT = "INPUT_FORMAT";

	/**
	 * 签名数据编码
	 */
	public static String SIGNED_DATA_ENCODE = "SIGNED_DATA_ENCODE";

	/**
	 * 输入签名格式,目前支持可查看：FormatType
	 */
	public static String SIGNED_DATA_FORMAT = "SIGNED_DATA_FORMAT";

	/**
	 * 未签名数据编码
	 */
	public static String UNSIGNED_DATA_ENCODE = "UNSIGNED_DATA_ENCODE";

	/**
	 * 输入未签名数据格式,目前支持可查看：FormatType
	 */
	public static String UNSIGNED_DATA_FORMAT = "UNSIGNED_DATA_FORMAT";

	/**
	 * 签名数据
	 */
	public static String SIGNED_DATA = "SIGNED_DATA";

	/**
	 * 未签名数据
	 */
	public static String UNSIGNED_DATA = "UNSIGNED_DATA";

	/**
	 * 输出格式,目前支持可查看：FormatType
	 */
	public static String OUTPUT_FORMAT = "OUTPUT_FORMAT";

	/**
	 * 输出String编码, 支持格式可见：CharsetEnum
	 */
	public static String OUTPUT_ENCODE = "OUTPUT_ENCODE";

	/**
	 * PGP 签名方法
	 */
	public static String PGP_SIGN_METHOD = "PGP_SIGN_METHOD";

	/**
	 * PGP 解密是否需要验签标记 , Y, N
	 */
	public static String PGP_NEED_VERIFY = "PGP_NEED_VERIFY";

	/**
	 * PGP 解密输出的文件名
	 */
	public static String PGP_DECRYPT_OUTPUT_FILE = "PGP_DECRYPT_OUTPUT_FILE";

	/**
	 * xml签名添加模式, 详见：XmlSignatureAppendMode，默认值：1(子节点模式)
	 */
	public static String APPEND_MODE = "APPEND_MODE";

	/**
	 * 获取的证书base64形式, ONCE, TWICE, NONE, 不配置默认为:ONCE
	 */
	public static String CERT_BASE64_FORM = "CERT_BASE64_FORM";

	/**
	 * xml 签名部分tag名, 默认值：Message
	 */
	public static String XML_ELE_TAG_NAME = "XML_ELE_TAG_NAME";

	/**
	 * xml 签名部分tag名默认值
	 */
	public static String XML_ELE_TAG_DEFAULT = "Message";

	/**
	 * 是否需要签发证书信息-xml签名特定
	 */
	public static String NEED_ISSUER_SERIAL = "NEED_ISSUER_SERIAL";

	/**
	 * 签名是否附加报文
	 */
	public static String ATTACH = "ATTACH";

}
