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

import java.util.List;

/**
 * 原子项存储。
 * <p>
 * 实际真实确定的内容。
 * 
 * @author dasong.jds
 * @version $Id: MetaItemStore.java, v 0.1 2015年10月13日 下午1:56:14 dasong.jds Exp $
 */
public interface MetaItemStore {

    /**
     * 获取原子项列表。
     * 
     * @param system
     * @return
     */
    public List<MetaItem> getMetaItemList(String system);

    /**
     * 获取原子项。
     * 
     * @param system
     * @param id
     * @return
     */
    public MetaItem getMetaItem(String system, String id);

    /**
     * 获取映射列表。
     * 
     * @param system
     * @return
     */
    public List<MetaItemMapping> getMetaItemMappingList(String system);

    /**
     * 获取某个原子项对应的映射。
     * 
     * @param metaItemId
     * @return
     */
    public MetaItemMapping getMetaItemMapping(MetaItem metaItem);

    /**
     * 获取某个初始项对应的映射。
     * 
     * @param initItem
     * @return
     */
    public MetaItemMapping getMetaItemMapping(MetaInitItem initItem);

    /**
     * 增加原子项映射。
     * 
     * @param metaItemMapping
     */
    public void addMetaItemMapping(MetaItemMapping metaItemMapping);

    /**
     * 增加原子项。
     * 
     * @param metaItem
     */
    public void addMetaItem(MetaItem metaItem);

    /**
     * 更新原子项。
     * 
     * @param metaItem
     */
    public int updateMetaItem(MetaItem metaItem);

}
