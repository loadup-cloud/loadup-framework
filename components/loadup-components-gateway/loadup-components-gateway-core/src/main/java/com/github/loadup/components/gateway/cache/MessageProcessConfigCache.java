package com.github.loadup.components.gateway.cache;

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
