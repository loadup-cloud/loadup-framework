/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.alipay.test.acts.data.impl;

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

import com.alipay.test.acts.data.MetaInitItem;
import com.alipay.test.acts.data.MetaItem;
import com.alipay.test.acts.data.MetaItemMapping;
import com.alipay.test.acts.data.MetaItemStore;
import com.alipay.test.acts.data.db.MetaItemRepository;

/**
 * 原子项存储的默认实现。
 * <p>
 * 依赖DB
 * 
 * @author dasong.jds
 * @version $Id: DefaultMetaItemStore.java, v 0.1 2015年10月14日 下午3:23:09 dasong.jds Exp $
 */
public class DefaultMetaItemStore implements MetaItemStore {

    /** 原子项仓储 */
    private MetaItemRepository metaItemRepository = new MetaItemRepository();

    /** 
     * @see MetaItemStore#getMetaItemList(String)
     */
    @Override
    public List<MetaItem> getMetaItemList(String system) {
        List<String> ids = metaItemRepository.getMetaItemIds(system);
        List<MetaItem> metaItems = new ArrayList<MetaItem>();
        for (String id : ids) {
            metaItems.add(metaItemRepository.loadMetaItem(system, id));
        }
        return metaItems;
    }

    /** 
     * @see MetaItemStore#getMetaItem(String, String)
     */
    @Override
    public MetaItem getMetaItem(String system, String id) {

        return metaItemRepository.loadMetaItem(system, id);
    }

    /** 
     * @see MetaItemStore#getMetaItemMappingList(String)
     */
    @Override
    public List<MetaItemMapping> getMetaItemMappingList(String system) {
        List<String> ids = metaItemRepository.getMetaItemIds(system);
        List<MetaItemMapping> metaItemMappings = new ArrayList<MetaItemMapping>();
        for (String id : ids) {
            metaItemMappings.add(metaItemRepository.loadMetaItemMapping(system, id));
        }
        return metaItemMappings;
    }

    /** 
     * @see MetaItemStore#getMetaItemMapping(MetaItem)
     */
    @Override
    public MetaItemMapping getMetaItemMapping(MetaItem metaItem) {
        return metaItemRepository.loadMetaItemMapping(metaItem.getSystem(), metaItem.getId());
    }

    /** 
     * @see MetaItemStore#getMetaItemMapping(MetaInitItem)
     */
    @Override
    public MetaItemMapping getMetaItemMapping(MetaInitItem initItem) {
        return metaItemRepository.loadMetaItemMapping(initItem.getSystem(), initItem.getHost(),
            initItem.getField());
    }

    /** 
     * @see MetaItemStore#addMetaItemMapping(MetaItemMapping)
     */
    @Override
    public void addMetaItemMapping(MetaItemMapping metaItemMapping) {

        metaItemRepository.storeMetaItemMapping(metaItemMapping);

    }

    /** 
     * @see MetaItemStore#addMetaItem(MetaItem)
     */
    @Override
    public void addMetaItem(MetaItem metaItem) {

        metaItemRepository.storeMetaItem(metaItem);
    }

    /** 
     * @see MetaItemStore#updateMetaItem(MetaItem)
     */
    @Override
    public int updateMetaItem(MetaItem metaItem) {

        return metaItemRepository.reStoreMetaItem(metaItem);
    }

}
