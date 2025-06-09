/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.testify.component.event.hunt.impl;

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

import com.github.loadup.components.testify.component.event.hunt.EventContextHunter;
import com.github.loadup.components.testify.component.event.hunt.HuntResult;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jie.peng
 *
 */
public class LocalEventContextHunter implements EventContextHunter {

    /**
     * 日志.
     */
    private static final Logger logger = LoggerFactory.getLogger(LocalEventContextHunter.class);

    /**
     * @see EventContextHunter#hunt(String, String, Set)
     */
    @Override
    public HuntResult hunt(String topic, String eventCode, Set<String> targets) {
        HuntResult result = new HuntResult();
        // UniformEvent uEvent = EventContextHolder.getBizEvent();
        // 若事件内容为空或者事件不为指定的事件码，则直接返回
        //        if (uEvent == null || !StringUtils.equals(uEvent.getTopic(), topic)
        //            || !StringUtils.equals(uEvent.getEventCode(), eventCode)) {
        //            return result;
        //        }
        //        Object eventObj = uEvent.getEventPayload();
        //        //若事件载体为空，则直接返回
        //        if (eventObj == null) {
        //            return result;
        //        }
        //        //设置抓取的事件内容
        //        result.setEventContext(eventObj);
        //        //若期望获取的事件属性列表为空，则直接返回
        //        if (targets == null || targets.isEmpty()) {
        //            return result;
        //        }
        //        //遍历提取内容
        //        for (String each : targets) {
        //            try {
        //                Object value = PropertyUtils.getProperty(eventObj, each);
        //                if (value instanceof String) {
        //                    result.addActual(each, (String) value);
        //                } else {
        //                    result.addActual(each, value.toString());
        //                }
        //            } catch (Exception e) {
        //                logger.error("获取消息体对象属性时出现异常，指定属性名称错误，名称：" + each, e);
        //            }
        //        }
        return result;
    }
}
