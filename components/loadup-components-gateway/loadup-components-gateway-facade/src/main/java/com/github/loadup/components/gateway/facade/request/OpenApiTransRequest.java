package com.github.loadup.components.gateway.facade.request;

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

/**
 *
 */
public class OpenApiTransRequest {

	/**
	 * interface id
	 */
	private String interfaceId;

	/**
	 * message of rpc request
	 */
	private String message;

	/**
	 * Gets get message.
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Sets set message.
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * Gets get interface id.
	 */
	public String getInterfaceId() {
		return interfaceId;
	}

	/**
	 * Sets set interface id.
	 */
	public void setInterfaceId(String interfaceId) {
		this.interfaceId = interfaceId;
	}

	/**
	 * To string string.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer("OpenApiTransRequest{");
		sb.append("interfaceId=").append(interfaceId);
		sb.append(", message='").append(message);
		sb.append('}');
		return sb.toString();
	}
}
