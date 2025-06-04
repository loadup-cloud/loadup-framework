/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.testify.data;

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
 * 原子项映射关系。
 *
 *
 *
 */
public class MetaItemMapping {

    /**
     * 元数据项
     */
    private MetaItem metaItem;

    /**
     * 原始定义项
     */
    private List<MetaInitItem> initItems = new ArrayList<MetaInitItem>();

    /**
     * 添加初始项。
     *
     * @param initItem
     */
    public void addMetaInitItem(MetaInitItem initItem) {
        for (MetaInitItem item : initItems) {
            if (item.equals(initItem)) {
                return;
            }
        }

        initItems.add(initItem);
    }

    /**
     * 获取mapping中的初始项。
     *
     * @param initItem
     * @return
     */
    public MetaInitItem getMetaInitItemOfMapping(MetaInitItem initItem) {
        for (MetaInitItem item : this.initItems) {
            if (item.equals(initItem)) {
                return item;
            }
        }
        return null;
    }

    /**
     * 删除初始项。
     *
     * @param initItem
     */
    public boolean removeMetaInitItem(MetaInitItem initItem) {
        return initItems.remove(initItem);
    }

    /**
     *
     *
     * @return property value of metaItem
     */
    public MetaItem getMetaItem() {
        return metaItem;
    }

    /**
     *
     *
     * @param metaItem value to be assigned to property metaItem
     */
    public void setMetaItem(MetaItem metaItem) {
        this.metaItem = metaItem;
    }

    /**
     *
     *
     * @return property value of initItems
     */
    public List<MetaInitItem> getInitItems() {
        return initItems;
    }

    /**
     *
     *
     * @param initItems value to be assigned to property initItems
     */
    public void setInitItems(List<MetaInitItem> initItems) {
        this.initItems = initItems;
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return "MetaItemMapping [metaItem=" + metaItem + ", initItems=" + initItems + "]";
    }
}
