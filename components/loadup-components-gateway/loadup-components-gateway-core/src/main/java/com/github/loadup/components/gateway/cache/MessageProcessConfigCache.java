package com.github.loadup.components.gateway.cache;

import com.github.loadup.components.gateway.common.util.CacheLogUtil;
import com.github.loadup.components.gateway.core.model.MessageProcessConfig;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 * MessageProcessCache.java
 * </p>
 */
public final class MessageProcessConfigCache {

    private static Map<String, MessageProcessConfig> messageProcessMap = new ConcurrentHashMap<>();

    /**
     * build the cache
     */
    public static void putAll(boolean clear, List<MessageProcessConfig> messageProcessConfigList) {
        if (CollectionUtils.isEmpty(messageProcessConfigList)) {
            return;
        }
        Map<String, MessageProcessConfig> tempMap = new ConcurrentHashMap<>();
        messageProcessConfigList.forEach(
                messageProcessConfig -> tempMap.put(messageProcessConfig.getMessageProcessId(), messageProcessConfig));
        if (clear) {
            messageProcessMap = tempMap;
        } else {
            messageProcessMap.putAll(tempMap);
        }
        CacheLogUtil.printLog("messageProcessConfigMap", messageProcessMap);
    }

    /**
     * get message process with message process id
     */
    public static MessageProcessConfig getWithMessageProcessId(String messageProcessId) {
        return messageProcessMap.get(messageProcessId);
    }

}