package com.github.loadup.components.gateway.message.base.model;

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
 * 组装模板模型
 */
public class AssembleTemplate {

	/**
	 * 报文组件ID
	 */
	private String messageProcessId;

	/**
	 * 报文结构
	 */
	private MessageStruct messageStruct;

	/**
	 * 主模板，基础配置，必填
	 */
	private String mainTemplate;

	/**
	 * 子模版，可以为待签名数据模板
	 */
	private String subTemplate;

	/**
	 * 报文头模板
	 */
	private String headerTemplate;

	/**
	 * 附加模板， 业务需要扩展模板
	 */
	private String extraTemplate;

	/**
	 * 异常渲染模板
	 */
	private String errorTemplate;

	/**
	 * 异常渲染子模板
	 */
	private String errorSubTemplate;

	/**
	 * Getter method for property <tt>messageProcessId</tt>.
	 */
	public String getMessageProcessId() {
		return messageProcessId;
	}

	/**
	 * Setter method for property <tt>messageProcessId</tt>.
	 */
	public void setMessageProcessId(String messageProcessId) {
		this.messageProcessId = messageProcessId;
	}

	/**
	 * Getter method for property <tt>messageStruct</tt>.
	 */
	public MessageStruct getMessageStruct() {
		return messageStruct;
	}

	/**
	 * Setter method for property <tt>messageStruct</tt>.
	 */
	public void setMessageStruct(MessageStruct messageStruct) {
		this.messageStruct = messageStruct;
	}

	/**
	 * Getter method for property <tt>mainTemplate</tt>.
	 */
	public String getMainTemplate() {
		return mainTemplate;
	}

	/**
	 * Setter method for property <tt>mainTemplate</tt>.
	 */
	public void setMainTemplate(String mainTemplate) {
		this.mainTemplate = mainTemplate;
	}

	/**
	 * Getter method for property <tt>subTemplate</tt>.
	 */
	public String getSubTemplate() {
		return subTemplate;
	}

	/**
	 * Setter method for property <tt>subTemplate</tt>.
	 */
	public void setSubTemplate(String subTemplate) {
		this.subTemplate = subTemplate;
	}

	/**
	 * Getter method for property <tt>headerTemplate</tt>.
	 */
	public String getHeaderTemplate() {
		return headerTemplate;
	}

	/**
	 * Setter method for property <tt>headerTemplate</tt>.
	 */
	public void setHeaderTemplate(String headerTemplate) {
		this.headerTemplate = headerTemplate;
	}

	/**
	 * Getter method for property <tt>extraTemplate</tt>.
	 */
	public String getExtraTemplate() {
		return extraTemplate;
	}

	/**
	 * Setter method for property <tt>extraTemplate</tt>.
	 */
	public void setExtraTemplate(String extraTemplate) {
		this.extraTemplate = extraTemplate;
	}

	/**
	 * Getter method for property <tt>errorTemplate</tt>.
	 */
	public String getErrorTemplate() {
		return errorTemplate;
	}

	/**
	 * Setter method for property <tt>errorTemplate</tt>.
	 */
	public void setErrorTemplate(String errorTemplate) {
		this.errorTemplate = errorTemplate;
	}

	/**
	 * Getter method for property <tt>errorSubTemplate</tt>.
	 */
	public String getErrorSubTemplate() {
		return errorSubTemplate;
	}

	/**
	 * Setter method for property <tt>errorSubTemplate</tt>.
	 */
	public void setErrorSubTemplate(String errorSubTemplate) {
		this.errorSubTemplate = errorSubTemplate;
	}

}
