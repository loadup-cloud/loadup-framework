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

import com.github.loadup.components.testify.data.enums.MatchDegree;
import com.github.loadup.components.testify.data.impl.DefaultMetaItemStore;
import com.github.loadup.components.testify.data.matcher.MetaItemNameMatcher;
import com.github.loadup.components.testify.data.matcher.MetaItemRegexMatcher;
import java.util.*;

/**
 * 自动智能匹配器。
 *
 *
 *
 */
public class AutoMatcher {

    /**
     * 原子存储
     */
    private MetaItemStore metaItemStore = new DefaultMetaItemStore();

    /**
     * 匹配器
     */
    private List<MetaItemMatcher> metaItemMatchers = new ArrayList<MetaItemMatcher>();

    /**
     *
     */
    public AutoMatcher() {
        super();
        metaItemMatchers.add(new MetaItemNameMatcher());
        metaItemMatchers.add(new MetaItemRegexMatcher());
    }

    /**
     * 只返回变动不明确部分。
     *
     * @param system
     * @param initItems
     * @return
     */
    public Collection<MetaItemMappingDraft> match(String system, List<MetaInitItem> initItems) {

        Map<MetaItem, MetaItemMappingDraft> drafts = new HashMap<MetaItem, MetaItemMappingDraft>();

        List<MetaItem> metaItems = metaItemStore.getMetaItemList(system);

        List<MetaItem> newMetaItems = new ArrayList<MetaItem>();

        for (MetaInitItem initItem : initItems) {

            if (!system.equals(initItem.getSystem())) {
                continue;
            }

            // 已存在不处理
            MetaItemMapping itemMapping = metaItemStore.getMetaItemMapping(initItem);
            if (itemMapping != null) {
                continue;
            }

            boolean isMatched = false;

            // 优先匹配已有原子项
            for (MetaItem metaItem : metaItems) {
                isMatched = addToDraft(drafts, initItem, metaItem) || isMatched;
            }

            // 再匹配本次新建的
            for (MetaItem metaItem : newMetaItems) {
                isMatched = addToDraft(drafts, initItem, metaItem) || isMatched;
            }

            // 最后新建原子项
            if (!isMatched) {
                MetaItem metaItem = createNewMetaItem(initItem);
                newMetaItems.add(metaItem);

                addToDraft(drafts, initItem, metaItem);
            }
        }

        return drafts.values();
    }

    /**
     * @param initItem
     * @return
     */
    private MetaItem createNewMetaItem(MetaInitItem initItem) {
        MetaItem metaItem = new MetaItem(initItem.getSystem(), initItem.getField());
        String keyWord = initItem.getField();
        if (keyWord == null) {
            keyWord = "";
        }
        metaItem.addKeyword(keyWord);
        return metaItem;
    }

    /**
     * 添加到草稿。
     *
     * @param drafts
     * @param initItem
     * @param metaItem
     * @return
     */
    private boolean addToDraft(Map<MetaItem, MetaItemMappingDraft> drafts, MetaInitItem initItem, MetaItem metaItem) {
        boolean isAdded = false;
        MatchDegree degree = getMatchDegree(initItem, metaItem);

        if (degree != null) {
            MetaItemMappingDraft draft = getDraft(drafts, metaItem);
            draft.addCandidate(degree, initItem);
            isAdded = true;
        }
        return isAdded;
    }

    /**
     * 获取草稿。
     *
     * @param drafts
     * @param metaItem
     * @return
     */
    private MetaItemMappingDraft getDraft(Map<MetaItem, MetaItemMappingDraft> drafts, MetaItem metaItem) {
        MetaItemMappingDraft draft = drafts.get(metaItem);
        if (draft == null) {
            draft = new MetaItemMappingDraft();
            draft.setMetaItem(metaItem);
            drafts.put(metaItem, draft);
        }
        return draft;
    }

    /**
     * 获取与原子项的匹配度。
     *
     * @param initItem
     * @param metaItem
     * @return
     */
    private MatchDegree getMatchDegree(MetaInitItem initItem, MetaItem metaItem) {

        int degree = 0;
        for (String keyword : metaItem.getKeywords()) {
            for (MetaItemMatcher m : metaItemMatchers) {
                degree += m.match(initItem, keyword);
            }
        }

        if (degree > 70) {
            return MatchDegree.RECOMMEND;
        }

        if (degree > 20) {
            return MatchDegree.RELEVANCE;
        }

        return null;
    }
}
