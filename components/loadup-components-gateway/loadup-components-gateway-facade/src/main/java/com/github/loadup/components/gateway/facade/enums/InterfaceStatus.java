package com.github.loadup.components.gateway.facade.enums;

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
public enum InterfaceStatus {

	/**
	 * valid status
	 */
	VALID("VALID", "online, valid status"),

	/**
	 * invalid status
	 */
	INVALID("INVALID", "online, valid status"),

	;

	/**
	 * code
	 */
	private String code;

	/**
	 * description
	 */
	private String desc;

	/**
	 * get by name
	 */
	public static InterfaceStatus getByName(String InterfaceStatus) {
		for (com.github.loadup.components.gateway.facade.enums.InterfaceStatus type : values()) {
			if (type.getCode().equals(InterfaceStatus)) {
				return type;
			}
		}
		return null;
	}

	/**
	 * 构造函数
	 */
	InterfaceStatus(String code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	/**
	 * Getter method for property <tt>desc</tt>.
	 */
	public String getDesc() {
		return desc;
	}

	/**
	 * Setter method for property <tt>desc</tt>.
	 */
	public void setDesc(String desc) {
		this.desc = desc;
	}

	/**
	 * Getter method for property <tt>code</tt>.
	 */
	public String getCode() {
		return code;
	}

	/**
	 * Setter method for property <tt>code</tt>.
	 */
	public void setCode(String code) {
		this.code = code;
	}
}
