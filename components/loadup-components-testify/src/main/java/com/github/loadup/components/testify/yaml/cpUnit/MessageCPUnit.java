/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.testify.yaml.cpUnit;

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

import com.github.loadup.components.testify.cache.TestifyCacheData;
import com.github.loadup.components.testify.msg.model.MessageModel;
import com.github.loadup.components.testify.yaml.enums.CPUnitTypeEnum;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 消息CheckPoint单位
 *
 *
 *
 */
public class MessageCPUnit extends BaseCPUnit {

    private final List<ObjectCPUnit> attributeList = new ArrayList<ObjectCPUnit>();

    private final String messageKey;

    private String eventTopic;

    private String eventCode;

    @SuppressWarnings("unchecked")
    public MessageCPUnit(String unitName, List<Object> rawData) {
        MessageModel model = TestifyCacheData.getMessageModel(unitName);
        this.unitName = model.getMessageClass();
        this.unitType = CPUnitTypeEnum.MESSAGE;
        this.eventTopic = model.getEventTopic();
        this.eventCode = model.getEventCode();
        this.messageKey = model.getMessageKey();
        for (Object msg : rawData) {
            ObjectCPUnit unit = new ObjectCPUnit(this.unitName, (Map<String, Object>) msg);
            this.attributeList.add(unit);
        }
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return "MessageCPUnit [attributeList="
                + attributeList
                + ", messageKey="
                + messageKey
                + ", eventTopic="
                + eventTopic
                + ", eventCode="
                + eventCode
                + ", unitName="
                + unitName
                + ", unitType="
                + unitType
                + "]";
    }

    /**
     *
     *
     * @return property value of attributeMap
     */
    public List<ObjectCPUnit> getAttributeList() {
        return attributeList;
    }

    /**
     *
     *
     * @return property value of eventTopic
     */
    public String getEventTopic() {
        return eventTopic;
    }

    /**
     *
     *
     * @param eventTopic value to be assigned to property eventTopic
     */
    public void setEventTopic(String eventTopic) {
        this.eventTopic = eventTopic;
    }

    /**
     *
     *
     * @return property value of eventCode
     */
    public String getEventCode() {
        return eventCode;
    }

    /**
     *
     *
     * @param eventCode value to be assigned to property eventCode
     */
    public void setEventCode(String eventCode) {
        this.eventCode = eventCode;
    }

    /**
     *
     *
     * @return property value of messageKey
     */
    public String getMessageKey() {
        return messageKey;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Object dump() {
        List objList = new ArrayList();
        for (ObjectCPUnit cpUnit : this.attributeList) {
            objList.add(cpUnit.dump());
        }
        return objList;
    }
}
