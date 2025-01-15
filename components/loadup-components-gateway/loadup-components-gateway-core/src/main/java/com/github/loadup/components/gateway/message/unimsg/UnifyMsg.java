package com.github.loadup.components.gateway.message.unimsg;

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

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.Map.Entry;

/**
 * <p>
 * UnifyMessage.java
 * </p>
 */
public class UnifyMsg {

	private Map<String, String> messageMap;

	/**
	 * init json string
	 */
	public void initJson(JSONObject jsonObject) {
		Set<Entry<String, Object>> params = jsonObject.entrySet();
		for (Entry<String, Object> param : params) {
			String key = param.getKey();
			Object value = param.getValue();
			addField(key, value);
		}

	}

	/**
	 * Constructor.
	 */
	public UnifyMsg() {
		messageMap = new HashMap<>(16);
	}

	/**
	 * G string.
	 */
	public String g(String key) {
		return messageMap.get(key);
	}

	/**
	 * Add field.
	 */
	public void addField(String key, Object value) {
		if (value == null) {
			messageMap.put(key, null);
		} else {
			messageMap.put(key, String.valueOf(value));
		}
	}

	public void addMap(Map<String, Object> paramsMap) {
		if (!CollectionUtils.isEmpty(paramsMap)) {
			paramsMap.forEach((k, v) -> addField(k, v));
		}
	}

	/**
	 * Gets get message map.
	 */
	public Map<String, String> getMessageMap() {
		return messageMap;
	}

	/**
	 * Sets set message map.
	 */
	public void setMessageMap(Map<String, String> messageMap) {
		this.messageMap = messageMap;
	}

	/**
	 * To j son str string.
	 */
	public String toJSonStr() {
		return JSON.toJSONString(messageMap);
	}
}
