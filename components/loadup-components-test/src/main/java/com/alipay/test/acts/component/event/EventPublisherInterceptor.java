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

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import org.apache.commons.lang3.StringUtils;

/**
 * acts消息发送器拦截器
 * @author zhiyuan.lzy
 * @version $Id: EventPublisherInterceptor.java, v 0.1 2015-6-29 下午06:04:58 jie.peng Exp $
 */
public class EventPublisherInterceptor implements MethodInterceptor {

    /**记录是否真正去调用消息中心来发送消息,默认为true,也就是要发送,一旦在xml中配置诚false,则不发送,直接返回成功*/
    private boolean sendToMsgbroker = true;

    /**
     * @see MethodInterceptor#invoke(MethodInvocation)
     */
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {

        String methodName = invocation.getMethod().getName();

        /**
         * 若当前方法不为发送事件，则直接放过
         */
        if (!StringUtils.equals(methodName, "publishUniformEvent")) {
            return invocation.proceed();
        }

        //获取当前方法执行参数
        Object[] params = invocation.getArguments();
        for (Object o : params) {
//            if (o instanceof UniformEvent) {
//                //拦截并报错事件
//                String topicId = ((UniformEvent) o).getTopic();
//                String eventCode = (((UniformEvent) o).getEventCode());
//                Object payLoad = (((UniformEvent) o).getEventPayload());
//                EventContextHolder.setEvent(eventCode, topicId, payLoad);
//            }
        }

        //然后开始判断是否需要mock掉消息中心,如果用户要求发送到,就给他发送
        if (sendToMsgbroker) {
            return invocation.proceed();
        }

        //否则就不发送了.直接返回true
        else {
            return true;
        }
    }

    /**
     * Getter method for property <tt>sendToMsgbroker</tt>.
     * 
     * @return property value of sendToMsgbroker
     */
    public boolean isSendToMsgbroker() {
        return sendToMsgbroker;
    }

    /**
     * Setter method for property <tt>sendToMsgbroker</tt>.
     * 
     * @param sendToMsgbroker value to be assigned to property sendToMsgbroker
     */
    public void setSendToMsgbroker(boolean sendToMsgbroker) {
        this.sendToMsgbroker = sendToMsgbroker;
    }

}
