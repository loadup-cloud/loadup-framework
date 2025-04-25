/**

 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.github.loadup.components.testify.model;

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

/**
 * @author tantian.wc
 * 
 */
public class VirtualEventObject extends TestNode {

    /**
     * 消息payload中的对象
     */
    public VirtualObject eventObject;

    /**
     * eventCode
     */
    public String eventCode;

    /**
     * topicId
     */
    public String topicId;

    /**
     * flag
     */
    public String isExist = "Y";

    public String getIsExist() {
        return isExist;
    }

    public void setFlag(String isExist) {
        this.isExist = isExist;
    }

    public String getEventCode() {
        return eventCode;
    }

    public void setEventCode(String eventCode) {
        this.eventCode = eventCode;
    }

    public String getTopicId() {
        return topicId;
    }

    public void setTopicId(String topicId) {
        this.topicId = topicId;
    }

    public VirtualObject getEventObject() {
        return eventObject;
    }

    public void setEventObject(VirtualObject eventObject) {
        this.eventObject = eventObject;
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return "VirtualEventObject [eventObject=" + eventObject + ", eventCode=" + eventCode
                + ", topicId=" + topicId + ", isExist=" + isExist + "]";
    }

}
