/**
 * Copyright (c) 2008-2013, http://www.snakeyaml.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alipay.yaml.events;

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

import com.alipay.yaml.error.Mark;

/**
 * Base class for the start events of the collection nodes.
 */
public abstract class CollectionStartEvent extends NodeEvent {
    private final String  tag;
    // The implicit flag of a collection start event indicates if the tag may be
    // omitted when the collection is emitted
    private final boolean implicit;
    // flag indicates if a collection is block or flow
    private final Boolean flowStyle;

    public CollectionStartEvent(String anchor, String tag, boolean implicit, Mark startMark,
                                Mark endMark, Boolean flowStyle) {
        super(anchor, startMark, endMark);
        this.tag = tag;
        this.implicit = implicit;
        this.flowStyle = flowStyle;
    }

    /**
     * Tag of this collection.
     * 
     * @return The tag of this collection, or <code>null</code> if no explicit
     *         tag is available.
     */
    public String getTag() {
        return this.tag;
    }

    /**
     * <code>true</code> if the tag can be omitted while this collection is
     * emitted.
     * 
     * @return True if the tag can be omitted while this collection is emitted.
     */
    public boolean getImplicit() {
        return this.implicit;
    }

    /**
     * <code>true</code> if this collection is in flow style, <code>false</code>
     * for block style.
     * 
     * @return If this collection is in flow style.
     */
    public Boolean getFlowStyle() {
        return this.flowStyle;
    }

    @Override
    protected String getArguments() {
        return super.getArguments() + ", tag=" + tag + ", implicit=" + implicit;
    }
}
