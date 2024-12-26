package com.github.loadup.components.gateway.cache;

import com.github.loadup.components.gateway.common.util.CacheLogUtil;
import com.github.loadup.components.gateway.core.model.MessageReceiverConfig;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 * MessageReceiverCache.java
 * </p>
 */
public final class MessageReceiverConfigCache {

    /**
     * Key is message receiver id
     */
    private static Map<String, MessageReceiverConfig> messageReceiverMap = new ConcurrentHashMap<>();

    /**
     * Put all.
     */
    public static void putAll(boolean clear, List<MessageReceiverConfig> messageReceiverConfigList) {
        if (CollectionUtils.isEmpty(messageReceiverConfigList)) {
            return;
        }

        Map<String, MessageReceiverConfig> tempMap = new ConcurrentHashMap<>();
        messageReceiverConfigList.forEach(
                messageReceiverConfig -> tempMap.put(messageReceiverConfig.getMessageReceiverId(), messageReceiverConfig));
        if (clear) {
            messageReceiverMap = tempMap;
        } else {
            messageReceiverMap.putAll(tempMap);
        }
        CacheLogUtil.printLog("messageReceiverMap", messageReceiverMap);
    }

    /**
     * Gets get with message receiver id.
     */
    public static MessageReceiverConfig getWithMessageReceiverId(String messageReceiverId) {
        return messageReceiverMap.get(messageReceiverId);

    }
}