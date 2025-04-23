/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.alipay.test.acts.component.event;

/*-
 * #%L
 * loadup-components-test
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 事件内容holder
 * @author jie.peng
 * @version $Id: EventContextHolder.java, v 0.1 2015-6-29 下午06:00:25 jie.peng Exp $
 */
public class EventContextHolder {

    /**
     * 业务消息
     */
    private static Map<String, List<Object>> bizEvent = new HashMap<String, List<Object>>();




    /**
     * 获取线程变量中持有的event内容，获取的同时清除线程变量内的值，一个线程只使用一次，避免一个线程多个测试用例同时执行时数据窜掉
     * 
     * @return
     */
    public static Map<String, List<Object>> getBizEvent() {
        return bizEvent;
    }

    public static void setEvent(String eventCode, String topicId, Object payLoad) {
        if (bizEvent == null) {
            bizEvent = new HashMap<String, List<Object>>();
        }
        String key = eventCode + "|" + topicId;
        if (bizEvent.containsKey(key)) {
            bizEvent.get(key).add(payLoad);
        } else {
            List<Object> payLoads = new ArrayList<Object>();
            payLoads.add(payLoad);
            bizEvent.put(key, payLoads);
        }
    }

    /**
     * 线程变量清理
     */
    public static void clear() {
        bizEvent=new HashMap<String, List<Object>>();
    }
}
