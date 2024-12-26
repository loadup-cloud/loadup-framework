package com.github.loadup.components.gateway.cache;

import com.github.loadup.components.gateway.common.util.CacheLogUtil;
import com.github.loadup.components.gateway.core.model.MessageSenderConfig;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 * MessageSenderCache.java
 * </p>
 */
public final class MessageSenderConfigCache {

    /**
     * Key is message sender id
     */
    private static Map<String, MessageSenderConfig> messageSenderMap = new ConcurrentHashMap<>();

    /**
     * Put all.
     */
    public static void putAll(boolean clear, List<MessageSenderConfig> messageSenderConfigList) {
        if (CollectionUtils.isEmpty(messageSenderConfigList)) {
            return;
        }

        Map<String, MessageSenderConfig> tempMap = new ConcurrentHashMap<>();
        messageSenderConfigList.forEach(messageSenderConfig -> tempMap.put(messageSenderConfig.getMessageSenderId(), messageSenderConfig));
        if (clear) {
            messageSenderMap = tempMap;
        } else {
            messageSenderMap.putAll(tempMap);
        }
        CacheLogUtil.printLog("messageSenderMap", messageSenderMap);
    }

    /**
     * Get with message sender id message sender config.
     */
    public static MessageSenderConfig getWithMessageSenderId(String messageSenderConfigId) {
        return messageSenderMap.get(messageSenderConfigId);
    }
}