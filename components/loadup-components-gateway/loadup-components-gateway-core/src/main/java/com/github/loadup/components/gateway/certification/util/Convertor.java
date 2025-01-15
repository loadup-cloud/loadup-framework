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

import com.github.loadup.components.gateway.certification.exception.CertificationErrorCode;
import com.github.loadup.components.gateway.certification.exception.CertificationException;
import com.github.loadup.components.gateway.certification.model.*;
import com.github.loadup.components.gateway.facade.util.LogUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * 转换工具类
 */
public class Convertor {

	/**
	 * 日志定义
	 */
	private static final Logger logger = LoggerFactory.getLogger("ALGO-MANAGER");

	/**
	 * todo 签名类算法支持添加盐
	 * 基于配置做输入处理，根据配置获取输入数据编码,没有配置使用默认编码
	 */
	public static byte[] converInput(String srcContent, CertificationFactor certificationFactor) {
		String encode = (null == certificationFactor.getAlgoParameter()) ? null
				: certificationFactor.getAlgoParameter().get(CommonParameter.INPUT_ENCODE);
		String inputFormat = (null == certificationFactor.getAlgoParameter()) ? null
				: certificationFactor.getAlgoParameter().get(CommonParameter.INPUT_FORMAT);

		FormatType formatType = null;
		if (inputFormat != null) {
			formatType = FormatType.getByName(inputFormat);
			if (formatType == null) {
				throw new CertificationException(CertificationErrorCode.UNSUPPORTED_FORMAT);
			}
		}
		try {
			LogUtil.debug(logger, "[convert input] encode=" + encode + ",format=" + inputFormat);

			return recoverByte(srcContent, formatType, encode);
		} catch (Exception e) {
			LogUtil.error(logger, e, CommonUtil.generateOperationDigest(certificationFactor) +
					"convert input error");
			throw new CertificationException(CertificationErrorCode.INPUT_CONVERT_ERROR,
					CommonUtil.generateOperationDigest(certificationFactor), e);
		}
	}

	/**
	 * 对于验签操作入参转换
	 */
	public static Map<String, byte[]> converInput(String unsignedContent, String signedContent,
												CertificationFactor certificationFactor) {
		String unSignedDataEncode = (null == certificationFactor.getAlgoParameter()) ? null
				: certificationFactor.getAlgoParameter().get(CommonParameter.UNSIGNED_DATA_ENCODE);
		String unSignedFormat = (null == certificationFactor.getAlgoParameter()) ? null
				: certificationFactor.getAlgoParameter().get(CommonParameter.UNSIGNED_DATA_FORMAT);
		FormatType unsignedFormatType = null;

		String signedDataEncode = (null == certificationFactor.getAlgoParameter()) ? null
				: certificationFactor.getAlgoParameter().get(CommonParameter.SIGNED_DATA_ENCODE);
		String signedFormat = (null == certificationFactor.getAlgoParameter()) ? null
				: certificationFactor.getAlgoParameter().get(CommonParameter.SIGNED_DATA_FORMAT);
		FormatType signedFormatType = null;

		if (unSignedFormat != null) {
			unsignedFormatType = FormatType.getByName(unSignedFormat);
			if (unsignedFormatType == null) {
				throw new CertificationException(CertificationErrorCode.UNSUPPORTED_FORMAT);
			}
		}
		if (signedFormat != null) {
			signedFormatType = FormatType.getByName(signedFormat);
			if (signedFormatType == null) {
				throw new CertificationException(CertificationErrorCode.UNSUPPORTED_FORMAT);
			}
		}

		LogUtil.debug(logger, "[convert input][unsigned data ] encode=" + unSignedDataEncode +
				",format=" + unsignedFormatType);
		LogUtil.debug(logger, "[convert input][signed data ] encode=" + signedDataEncode +
				",format=" + signedFormat);

		Map<String, byte[]> rtnMap = new HashMap<String, byte[]>();

		try {
			if (unsignedContent != null) {
				rtnMap.put(CommonParameter.UNSIGNED_DATA,
						recoverByte(unsignedContent, unsignedFormatType, unSignedDataEncode));
			} else {
				rtnMap.put(CommonParameter.UNSIGNED_DATA, null);
			}

			rtnMap.put(CommonParameter.SIGNED_DATA,
					recoverByte(signedContent, signedFormatType, signedDataEncode));

			return rtnMap;
		} catch (Exception e) {
			LogUtil.error(logger, e, CommonUtil.generateOperationDigest(certificationFactor) +
					"convert input error");
			throw new CertificationException(CertificationErrorCode.INPUT_CONVERT_ERROR,
					CommonUtil.generateOperationDigest(certificationFactor), e);
		}
	}

	/**
	 * 算法输出编码转换
	 */
	public static String convertOutput(byte[] inputByte, CertificationFactor certificationFactor) {

		String outputFormat = (null == certificationFactor.getAlgoParameter()) ? null
				: certificationFactor.getAlgoParameter().get(CommonParameter.OUTPUT_FORMAT);
		String outputEncode = (null == certificationFactor.getAlgoParameter()) ? null
				: certificationFactor.getAlgoParameter().get(CommonParameter.OUTPUT_ENCODE);

		LogUtil.debug(logger, "[convert output]encode=" + outputEncode + ",format=" + outputFormat);

		if (outputFormat != null) {
			FormatType formatType = FormatType.getByName(outputFormat);
			if (formatType == null) {
				throw new CertificationException(CertificationErrorCode.UNSUPPORTED_FORMAT);
			}

			try {
				switch (formatType) {
					case FORMAT_BASE64:
						if (outputEncode != null) {
							return new String(Base64Util.encode(inputByte), outputEncode);
						} else {
							return new String(Base64Util.encode(inputByte));
						}
					case FORMAT_URLBASE64:
						if (outputEncode != null) {
							return new String(UrlBase64Util.encode(inputByte), outputEncode);
						} else {
							return new String(UrlBase64Util.encode(inputByte));
						}
					case FORMAT_HEX:
						if (outputEncode != null) {
							return EncodeUtil.bytes2Hex(inputByte, outputEncode);
						} else {
							return EncodeUtil.bytes2Hex(inputByte);
						}
					default:
						if (outputEncode != null) {
							return new String(inputByte, outputEncode);
						} else {
							return new String(inputByte);
						}
				}
			} catch (Exception e) {
				LogUtil.error(logger, e, CommonUtil.generateOperationDigest(certificationFactor) +
						"convert output error");
				throw new CertificationException(CertificationErrorCode.OUTPUT_CONVERT_ERROR,
						CommonUtil.generateOperationDigest(certificationFactor), e);
			}

		} else {
			try {
				if (outputEncode != null) {
					return new String(inputByte, outputEncode);
				} else {
					return new String(inputByte);
				}
			} catch (Exception e) {
				LogUtil.error(logger, e, CommonUtil.generateOperationDigest(certificationFactor) +
						"convert output error:");
				throw new CertificationException(CertificationErrorCode.OUTPUT_CONVERT_ERROR,
						CommonUtil.generateOperationDigest(certificationFactor), e);
			}
		}
	}

	/**
	 * 根据配置的输入格式和输入编码进行编码转换，转换为对应的byte数组形式
	 */
	public static byte[] recoverByte(String strInput, FormatType format, String encode)
			throws Exception {
		if (null == format) {
			if (StringUtils.isBlank(encode)) {
				return strInput.getBytes();
			}
			return strInput.getBytes(encode);
		}

		switch (format) {
			case FORMAT_BASE64:
				if (StringUtils.isBlank(encode)) {
					return Base64Util.decode(strInput.getBytes());
				}
				return Base64Util.decode(strInput.getBytes(encode));

			case FORMAT_URLBASE64:
				if (StringUtils.isBlank(encode)) {
					return UrlBase64Util.decode(strInput.getBytes());
				}
				return UrlBase64Util.decode(strInput.getBytes(encode));

			case FORMAT_HEX:
				return EncodeUtil.hex2Bytes(strInput, encode);

			case DOUBLE_BASE64:
				if (StringUtils.isBlank(encode)) {
					return Base64Util.decode(Base64Util.decode(strInput.getBytes()));
				}
				return Base64Util.decode(Base64Util.decode(strInput.getBytes(encode)));

			default:
				if (StringUtils.isBlank(encode)) {
					return strInput.getBytes();
				}
				return strInput.getBytes(encode);
		}
	}

}
