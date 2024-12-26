package com.github.loadup.components.gateway.message.unimsg;

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