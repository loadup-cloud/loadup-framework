package com.alipay.test.acts.object.manager;

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

import java.util.HashMap;
import java.util.Map;

import com.alipay.test.acts.object.comparer.UnitComparer;
import com.alipay.test.acts.object.comparer.impl.AvailableComparer;
import com.alipay.test.acts.object.comparer.impl.ConditionComparer;
import com.alipay.test.acts.object.comparer.impl.CustomComparer;
import com.alipay.test.acts.object.comparer.impl.DateComparer;
import com.alipay.test.acts.object.comparer.impl.FileComparer;
import com.alipay.test.acts.object.comparer.impl.MapComparer;
import com.alipay.test.acts.object.comparer.impl.NoCheckComparer;
import com.alipay.test.acts.object.comparer.impl.RegexComparer;
import com.alipay.test.acts.object.comparer.impl.SimpleComparer;
import com.alipay.test.acts.object.enums.UnitFlagEnum;

/**
 * 单元比较器管理器
 * 
 * @author baishuo.lp
 * @version $Id: UnitComparerManager.java, v 0.1 2015年8月19日 上午10:57:04 baishuo.lp Exp $
 */
public class ObjectCompareManager {

    private static Map<UnitFlagEnum, UnitComparer> comparerManager = new HashMap<UnitFlagEnum, UnitComparer>();

    static {
        comparerManager.put(UnitFlagEnum.N, new NoCheckComparer());
        comparerManager.put(UnitFlagEnum.Y, new SimpleComparer());
        comparerManager.put(UnitFlagEnum.D, new DateComparer());
        comparerManager.put(UnitFlagEnum.R, new RegexComparer());
        comparerManager.put(UnitFlagEnum.C, new ConditionComparer());
        comparerManager.put(UnitFlagEnum.A, new AvailableComparer());
        comparerManager.put(UnitFlagEnum.M, new MapComparer());
        comparerManager.put(UnitFlagEnum.F, new FileComparer());
        comparerManager.put(UnitFlagEnum.CUSTOM, new CustomComparer());
    }

    public static Map<UnitFlagEnum, UnitComparer> getComparerManager() {
        return comparerManager;
    }

}
