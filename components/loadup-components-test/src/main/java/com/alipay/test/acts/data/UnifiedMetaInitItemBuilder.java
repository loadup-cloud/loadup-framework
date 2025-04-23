/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.alipay.test.acts.data;

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

import com.alipay.test.acts.data.biz.BizMetaInitItemBuilder;
import com.alipay.test.acts.data.db.DBMetaInitItemBuilder;

/**
 * 统一原子项构造器。
 * 
 * @author dasong.jds
 * @version $Id: UnifiedMetaInitItemBuilder.java, v 0.1 2015年10月14日 下午7:05:35 dasong.jds Exp $
 */
public class UnifiedMetaInitItemBuilder implements MetaInitItemBuilder {

    /** 构造器集合 */
    private List<MetaInitItemBuilder> builders = new ArrayList<MetaInitItemBuilder>();

    public UnifiedMetaInitItemBuilder() {
        super();
        builders.add(new DBMetaInitItemBuilder());
        builders.add(new BizMetaInitItemBuilder());
    }

    /** 
     * @see MetaInitItemBuilder#build(String)
     */
    @Override
    public List<MetaInitItem> build(String system, String projectPath) {
        List<MetaInitItem> initItems = new ArrayList<MetaInitItem>();
        for (MetaInitItemBuilder builder : builders) {
            initItems.addAll(builder.build(system, projectPath));
        }
        return initItems;
    }

}
