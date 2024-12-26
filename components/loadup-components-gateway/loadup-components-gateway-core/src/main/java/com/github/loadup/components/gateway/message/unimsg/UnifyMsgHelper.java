package com.github.loadup.components.gateway.message.unimsg;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;

import java.util.Map;

/**
 * <p>
 * UnifyMessageHelper.java
 * </p>
 */
public class UnifyMsgHelper {

    public static UnifyMsg fromJSonStr(String jsonStr) {
        Map<String, String> messageMap = JSONObject.parseObject(jsonStr, new TypeReference<>() {
        });
        UnifyMsg unifyMsg = new UnifyMsg();
        unifyMsg.setMessageMap(messageMap);
        return unifyMsg;

    }
}