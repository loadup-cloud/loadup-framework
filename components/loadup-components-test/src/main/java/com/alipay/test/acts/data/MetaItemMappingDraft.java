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

import com.alipay.test.acts.data.enums.MatchDegree;

/**
 * 原子项映射草稿。
 * <p>
 * 根据算法自动生成匹配结果。
 * 
 * @author dasong.jds
 * @version $Id: MetaItemMappingDraft.java, v 0.1 2015年10月12日 下午5:55:18 dasong.jds Exp $
 */
public class MetaItemMappingDraft {

    /** 元数据项 */
    private MetaItem              metaItem;

    /** 候选 */
    private final List<Candidate> candidates = new ArrayList<Candidate>();

    /**
     * 增加候选项。
     * 
     * @param matchDegree
     * @param initItem
     */
    public void addCandidate(MatchDegree matchDegree, MetaInitItem initItem) {
        if (matchDegree == null) {
            return;
        }

        Candidate candidate = null;
        for (Candidate c : candidates) {
            if (c.matchDegree.equals(matchDegree)) {
                candidate = c;
                break;
            }
        }
        if (candidate == null) {
            candidate = new Candidate();
            candidate.matchDegree = matchDegree;
            candidate.initItems = new ArrayList<MetaInitItem>();
            candidates.add(candidate);
        }

        candidate.initItems.add(initItem);
    }

    /**
     * 候选原始项。
     */
    public static class Candidate {

        /** 匹配度 */
        public MatchDegree        matchDegree;

        /** 原始项 */
        public List<MetaInitItem> initItems;

        /** 
         * @see Object#toString()
         */
        @Override
        public String toString() {
            return "Candidate [matchDegree=" + matchDegree + ", initItems=" + initItems + "]";
        }

    }

    /**
     * Getter method for property <tt>metaItem</tt>.
     * 
     * @return property value of metaItem
     */
    public MetaItem getMetaItem() {
        return metaItem;
    }

    /**
     * Setter method for property <tt>metaItem</tt>.
     * 
     * @param metaItem value to be assigned to property metaItem
     */
    public void setMetaItem(MetaItem metaItem) {
        this.metaItem = metaItem;
    }

    /**
     * Getter method for property <tt>candidates</tt>.
     * 
     * @return property value of candidates
     */
    public List<Candidate> getCandidates() {
        return candidates;
    }

    /** 
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return "MetaItemMappingDraft [metaItem=" + metaItem + ", candidates=" + candidates + "]";
    }

}
