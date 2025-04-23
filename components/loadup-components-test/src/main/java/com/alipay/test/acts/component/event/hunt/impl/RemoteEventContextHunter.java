/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.alipay.test.acts.component.event.hunt.impl;

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

import java.util.Set;

import com.alipay.test.acts.utils.config.ConfigrationFactory;
import org.apache.commons.lang3.StringUtils;
import com.alipay.test.acts.component.event.hunt.EventContextHunter;
import com.alipay.test.acts.component.event.hunt.HuntResult;
import com.alipay.test.acts.component.event.ssh.AppServerLogFetcher;

/**
 * 
 * @author jie.peng
 * @version $Id: RemoteEventContextHunter.java, v 0.1 2015-6-29 下午06:35:04 jie.peng Exp $
 */
public class RemoteEventContextHunter implements EventContextHunter {

    /**打印应用变更事件的日志文件*/
    private final static String EVENT_LOGGER_NAME = ConfigrationFactory
                                                      .getSofaConfig("event_log_name");

    /** 
     * @see EventContextHunter#hunt(String, String, Set)
     */
    @Override
    public HuntResult hunt(String topic, String eventCode, Set<String> targets) {
        HuntResult result = new HuntResult();
        AppServerLogFetcher fecther = new AppServerLogFetcher();
        //组装日期提取命令
        String command = assembleCommand(topic, eventCode);
        //获取hunt的事件内容
        result.setEventContext(StringUtils.trimToNull(fecther.getLog(command)));
        //若期望获取的事件目标属性为空,则直接返回
        if (targets == null || targets.isEmpty()) {
            return result;
        }
        //设置获取的事件的实际属性值
        result.setActual(fecther.getInfoFromLog(command, targets.toArray(new String[] {})));
        return result;
    }

    /**
     * 组装提取应用变更事件日志的grep命令
     * 
     * @param topic 
     * @param eventCode 应用变更事件的事件码
     * @return
     */
    private String assembleCommand(String topic, String eventCode) {
        return "grep " + topic + " " + EVENT_LOGGER_NAME + "|grep " + eventCode;

    }

}
