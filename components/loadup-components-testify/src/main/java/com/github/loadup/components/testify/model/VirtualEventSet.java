/* Copyright (C) LoadUp Cloud 2022-2025 */
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

import java.util.ArrayList;
import java.util.List;

/**
 * 消息检查列表
 */
public class VirtualEventSet extends TestUnit {
    /**
     * 需要检查的一系列消息对象
     */
    private List<VirtualEventObject> virtualEventObjects = new ArrayList<VirtualEventObject>();

    public static VirtualEventSet getInstance() {
        return new VirtualEventSet();
    }

    /**
     * 新增消息检查对象
     *
     * @param virtualEventObject
     * @return
     */
    public VirtualEventSet addEventObject(VirtualEventObject virtualEventObject) {
        if (virtualEventObjects == null) {
            virtualEventObjects = new ArrayList<VirtualEventObject>();
        }
        virtualEventObjects.add(virtualEventObject);
        return this;
    }

    /**
     * 新增消息检查对象
     *
     * @param eventObject
     * @param eventCode
     * @param topicId
     * @return
     */
    public VirtualEventSet addEventObject(Object eventObject, String eventCode, String topicId) {
        if (virtualEventObjects == null) {
            virtualEventObjects = new ArrayList<VirtualEventObject>();
        }
        VirtualEventObject virtualEventObject = new VirtualEventObject();
        virtualEventObject.setEventCode(eventCode);
        virtualEventObject.setTopicId(topicId);
        virtualEventObject.setEventObject(new VirtualObject(eventObject));
        virtualEventObjects.add(virtualEventObject);
        return this;
    }

    public List<VirtualEventObject> getVirtualEventObjects() {
        return virtualEventObjects;
    }

    public void setVirtualEventObjects(List<VirtualEventObject> virtualEventObjects) {
        this.virtualEventObjects = virtualEventObjects;
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return "VirtualEventSet [virtualEventObjects=" + virtualEventObjects + "]";
    }
}
