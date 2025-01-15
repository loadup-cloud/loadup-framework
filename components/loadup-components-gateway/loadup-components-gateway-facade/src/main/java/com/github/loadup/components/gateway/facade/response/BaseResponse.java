package com.github.loadup.components.gateway.facade.response;

/*-
 * #%L
 * loadup-components-gateway-facade
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

import com.github.loadup.components.gateway.facade.model.Result;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Map;

/**
 *
 */
public abstract class BaseResponse {

	/**
	 * capability result
	 */
	private Result result;

	/**
	 * tenant id
	 */
	private String tntInstId;

	/**
	 * success or not
	 */
	private boolean success;

	/**
	 * error message
	 */
	private String errorMessage;

	/**
	 * extend info
	 */
	private Map<String, String> extendInfo;

	/**
	 * Getter method for property <tt>success</tt>.
	 */
	public boolean isSuccess() {
		return success;
	}

	/**
	 * Setter method for property <tt>success</tt>.
	 */
	public void setSuccess(boolean success) {
		this.success = success;
	}

	/**
	 * Getter method for property <tt>errorMessage</tt>.
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

	/**
	 * Setter method for property <tt>errorMessage</tt>.
	 */
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	/**
	 * Getter method for property <tt>extendInfo</tt>.
	 */
	public Map<String, String> getExtendInfo() {
		return extendInfo;
	}

	/**
	 * Setter method for property <tt>extendInfo</tt>.
	 */
	public void setExtendInfo(Map<String, String> extendInfo) {
		this.extendInfo = extendInfo;
	}

	/**
	 * Getter method for property <tt>result</tt>.
	 */
	public Result getResult() {
		return result;
	}

	/**
	 * Setter method for property <tt>result</tt>.
	 */
	public void setResult(Result result) {
		this.result = result;
	}

	/**
	 * Getter method for property <tt>tntInstId</tt>.
	 */
	public String getTntInstId() {
		return tntInstId;
	}

	/**
	 * Setter method for property <tt>tntInstId</tt>.
	 */
	public void setTntInstId(String tntInstId) {
		this.tntInstId = tntInstId;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
