
package com.github.loadup.components.testify.compatibility;

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

import com.github.loadup.components.testify.model.PrepareData;
import com.github.loadup.components.testify.model.VirtualEventObject;
import com.github.loadup.components.testify.util.BaseDataUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 消息兼容性对外api
 *
 * 
 *
 */
public class MessageCompatibilityApi {
    /**
     * logger
     */
    private static final Logger logger = LoggerFactory.getLogger(MessageCompatibilityApi.class);

    /***
     * 对外开发的比较消息的入口
     *
     * @param oldYamlPath
     * @param oldCaseId
     * @param newYamlPath
     * @param newCaseId
     * @return
     */
    public static boolean compareMessage(String oldYamlPath, String oldCaseId, String newYamlPath,
                                         String newCaseId) {

        ClassLoader cl = getCustomClassLoader();
        //获取两个拦截到的消息
        List<VirtualEventObject> oldEventObjects = getEventObjects(cl, oldYamlPath, oldCaseId);

        List<VirtualEventObject> newEventObjects = getEventObjects(cl, newYamlPath, newCaseId);

        //开始比较消息体

        boolean result = compareEventObjects(oldEventObjects, newEventObjects);

        return result;

    }

    /***
     * 比较消息体
     *
     * @param oldEventObjects
     * @param newEventObjects
     * @return
     */
    public static boolean compareEventObjects(List<VirtualEventObject> oldEventObjects,
                                              List<VirtualEventObject> newEventObjects) {

        boolean result = true;
        if (oldEventObjects.size() != newEventObjects.size()) {
            logger.error("消息体数目不一致");
            logger.error("老的消息topic-eventcode:" + getTopicAndCodes(oldEventObjects));
            logger.error("新的消息topic-eventcode:" + getTopicAndCodes(newEventObjects));
            return false;
        }

        for (VirtualEventObject ev : oldEventObjects) {

            String topic = ev.getTopicId();
            String eventCode = ev.getEventCode();
            VirtualEventObject find = findSpecialEvent(newEventObjects, topic, eventCode);

            boolean oneResult = compareEventObject(ev, find);

            //只要有一次返回false,就设置结果为false,否则结果就为true
            if (oneResult == false) {
                result = false;
            }
        }
        return result;

    }

    /***
     * 获取指定的消息
     *
     * @param eventObjects
     * @param topic
     * @param eventCode
     * @return
     */
    private static VirtualEventObject findSpecialEvent(List<VirtualEventObject> eventObjects,
                                                       String topic, String eventCode) {

        for (VirtualEventObject ev : eventObjects) {
            if (ev.getTopicId().equals(topic) && ev.getEventCode().equals(eventCode)) {
                return ev;
            }
        }

        logger.error("topic=" + topic + ",eventCode=" + eventCode + ",在新的消息体中不存在");
        return null;

    }

    private static List<String> getTopicAndCodes(List<VirtualEventObject> eventObjects) {
        List<String> result = new ArrayList<String>();
        for (VirtualEventObject ev : eventObjects) {
            String topicAndCode = ev.getTopicId() + "-" + ev.getEventCode();
            result.add(topicAndCode);
        }
        return result;
    }

    /***
     * 比较单个消息
     *
     * @param oldEventObject
     * @param newEventObject
     * @return
     */
    public static boolean compareEventObject(VirtualEventObject oldEventObject,
                                             VirtualEventObject newEventObject) {
        boolean result = true;
        // 消息对比
        logger.info("开始校验消息");

        if (oldEventObject == null && newEventObject == null) {
            logger.warn("新老消息都为空");
            return false;
        }
        if (oldEventObject != null && newEventObject == null) {
            logger.warn("新消息都为空,老消息是:" + oldEventObject);
            return false;
        }
        if (newEventObject != null && oldEventObject == null) {
            logger.warn("老消息都为空,新消息是:" + newEventObject);
            return false;
        }

        String topic = oldEventObject.getTopicId();
        String eventCode = oldEventObject.getEventCode();
        Object oldpayLoad = oldEventObject.getEventObject().getObject();
        Object newpayLoad = newEventObject.getEventObject().getObject();
        logger.info("topic=" + topic + ",eventCode=" + eventCode);
        result = ObjectCompareWraper.compareMessageWithFlag(oldpayLoad, newpayLoad, null, null);

        String str = ObjectCompareWraper.getReportResult();

        logger.warn("消息比对结果" + str);
        return result;

    }

    /***
     * 获取classLoader
     *
     * @return
     */
    private static ClassLoader getCustomClassLoader() {
        ClassLoader cl = MessageCompatibilityApi.class.getClassLoader();
        return cl;

    }

    /***
     * 加载yaml中的消息体
     *
     * @param cl
     * @param yamlPath
     * @param caseId
     * @return
     */
    private static List<VirtualEventObject> getEventObjects(ClassLoader cl, String yamlPath,
                                                            String caseId) {

        Map<String, PrepareData> caseMap = BaseDataUtil.loadFromYaml(yamlPath, cl);
        PrepareData prepareData = caseMap.get(caseId);
        List<VirtualEventObject> eventObjects = prepareData.getExpectEventSet()
                .getVirtualEventObjects();
        return eventObjects;

    }
}
